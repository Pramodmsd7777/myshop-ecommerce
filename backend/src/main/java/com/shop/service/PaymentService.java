package com.shop.service;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.shop.dto.OrderDtos.OrderResponse;
import com.shop.dto.PaymentDtos.*;
import com.shop.model.Order;
import com.shop.model.OrderStatus;
import com.shop.model.Payment;
import com.shop.model.PaymentStatus;
import com.shop.repository.OrderRepository;
import com.shop.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final RazorpayClient razorpay;

    @Value("${razorpay.key-id}") private String keyId;
    @Value("${razorpay.key-secret}") private String keySecret;
    @Value("${razorpay.webhook-secret}") private String webhookSecret;

    @Transactional
    public PaymentInitResponse create(String email, Long orderId) {
        Order order = orderRepository.findByIdAndUserEmail(orderId, email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This order is already " + order.getStatus().name().toLowerCase());
        }

        // reuse an unfinished attempt so clicking "Pay now" twice doesn't create duplicate Razorpay orders
        Payment payment = paymentRepository
                .findFirstByOrderIdAndStatusOrderByIdDesc(orderId, PaymentStatus.CREATED).orElse(null);

        if (payment == null) {
            long paise = order.getTotalAmount().movePointRight(2).longValueExact();   // Razorpay amounts are in paise
            JSONObject req = new JSONObject();
            req.put("amount", paise);
            req.put("currency", "INR");
            req.put("receipt", "order_" + order.getId());
            String razorpayOrderId;
            try {
                com.razorpay.Order rzpOrder = razorpay.orders.create(req);
                razorpayOrderId = rzpOrder.get("id");
            } catch (RazorpayException e) {
                log.error("Razorpay order creation failed", e);
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Payment provider is unavailable. Try again shortly.");
            }
            payment = new Payment();
            payment.setOrder(order);
            payment.setRazorpayOrderId(razorpayOrderId);
            payment.setAmount(order.getTotalAmount());
            payment.setCurrency("INR");
            paymentRepository.save(payment);
        }

        return new PaymentInitResponse(keyId, payment.getRazorpayOrderId(),
                payment.getAmount().movePointRight(2).longValueExact(), payment.getCurrency(), order.getId());
    }

    @Transactional
    public OrderResponse verify(String email, PaymentVerifyRequest req) {
        // only finds payments that belong to THIS user and were created by this server
        Payment payment = paymentRepository
                .findByRazorpayOrderIdAndOrderUserEmail(req.razorpayOrderId(), email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));

        JSONObject data = new JSONObject();
        data.put("razorpay_order_id", payment.getRazorpayOrderId());
        data.put("razorpay_payment_id", req.razorpayPaymentId());
        data.put("razorpay_signature", req.razorpaySignature());
        try {
            if (!Utils.verifyPaymentSignature(data, keySecret)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment could not be verified");
            }
        } catch (RazorpayException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment could not be verified");
        }

        markPaid(payment, req.razorpayPaymentId());
        return OrderResponse.from(payment.getOrder());
    }

    @Transactional
    public void handleWebhook(String payload, String signature) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Webhook is not configured");
        }
        try {
            if (signature == null || !Utils.verifyWebhookSignature(payload, signature, webhookSecret)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid signature");
            }
        } catch (RazorpayException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid signature");
        }

        JSONObject json = new JSONObject(payload);
        if (!"order.paid".equals(json.optString("event"))) return;   // ignore other events, still answer 200

        JSONObject pay = json.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
        paymentRepository.findByRazorpayOrderId(pay.getString("order_id"))
                .ifPresent(p -> markPaid(p, pay.getString("id")));
    }

    // called by both verify() and the webhook, and safe to run twice
    private void markPaid(Payment payment, String razorpayPaymentId) {
        Order order = orderRepository.findForUpdate(payment.getOrder().getId()).orElseThrow();
        if (payment.getStatus() == PaymentStatus.PAID) return;

        payment.setStatus(PaymentStatus.PAID);
        payment.setRazorpayPaymentId(razorpayPaymentId);

        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.PAID);
        } else if (order.getStatus() == OrderStatus.CANCELLED) {
            log.warn("Payment {} received for cancelled order {}: refund needed", razorpayPaymentId, order.getId());
        }
    }
}
