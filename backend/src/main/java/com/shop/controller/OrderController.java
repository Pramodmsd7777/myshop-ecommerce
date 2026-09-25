package com.shop.controller;

import com.shop.dto.OrderDtos.*;
import com.shop.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> create(Authentication auth, @Valid @RequestBody CheckoutRequest req) {
        OrderResponse created = orderService.checkout(auth.getName(), req);
        return ResponseEntity.created(URI.create("/api/orders/" + created.id())).body(created);
    }

    @GetMapping
    public List<OrderResponse> list(Authentication auth) {
        return orderService.list(auth.getName());
    }

    @GetMapping("/{id}")
    public OrderResponse get(Authentication auth, @PathVariable Long id) {
        return orderService.get(auth.getName(), id);
    }
}
