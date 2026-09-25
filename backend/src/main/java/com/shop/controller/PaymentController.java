package com.shop.controller;

import com.shop.dto.OrderDtos.OrderResponse;
import com.shop.dto.PaymentDtos.*;
import com.shop.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/create")
    public PaymentInitResponse create(Authentication auth, @Valid @RequestBody PaymentCreateRequest req) {
        return paymentService.create(auth.getName(), req.orderId());
    }

    @PostMapping("/verify")
    public OrderResponse verify(Authentication auth, @Valid @RequestBody PaymentVerifyRequest req) {
        return paymentService.verify(auth.getName(), req);
    }

    // called by Razorpay's servers directly, so there's no JWT; the signature is the authentication
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody String payload,
                                        @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {
        paymentService.handleWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }
}
