package com.shop.service;

import com.shop.dto.OrderDtos.*;
import com.shop.model.*;
import com.shop.repository.CartRepository;
import com.shop.repository.OrderRepository;
import com.shop.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    @Transactional
    public OrderResponse checkout(String email, CheckoutRequest req) {
        Cart cart = cartRepository.findByUserEmailForUpdate(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your cart is empty"));
        if (cart.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your cart is empty");
        }

        Order order = new Order();
        order.setUser(cart.getUser());
        order.setShippingAddress(req.shippingAddress().trim());
        order.setStatus(OrderStatus.PENDING);

        // process in a fixed order (by product id), so two concurrent checkouts can't deadlock on the same rows
        List<CartItem> cartItems = cart.getItems().stream()
                .sorted(Comparator.comparing(i -> i.getProduct().getId()))
                .toList();

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem ci : cartItems) {
            Product p = ci.getProduct();

            if (productRepository.decreaseStock(p.getId(), ci.getQuantity()) == 0) {
                // throwing rolls back the whole transaction, including earlier items' stock changes
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Not enough stock for '" + p.getName() + "'. Update your cart and try again.");
            }

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(p);
            oi.setQuantity(ci.getQuantity());
            oi.setPriceAtPurchase(p.getPrice());   // snapshot: later price changes don't touch this order
            order.getItems().add(oi);

            total = total.add(p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
        }
        order.setTotalAmount(total);
        orderRepository.save(order);               // cascades to the OrderItems

        cart.getItems().clear();                   // orphanRemoval deletes the cart rows
        return OrderResponse.from(order);
    }

    @Transactional
    public List<OrderResponse> list(String email) {
        return orderRepository.findByUserEmailOrderByCreatedAtDesc(email).stream()
                .map(OrderResponse::from).toList();
    }

    @Transactional
    public OrderResponse get(String email, Long id) {
        return orderRepository.findByIdAndUserEmail(id, email)
                .map(OrderResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }
}
