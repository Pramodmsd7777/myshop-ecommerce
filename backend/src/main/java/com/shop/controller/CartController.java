package com.shop.controller;

import com.shop.dto.CartDtos.*;
import com.shop.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping
    public CartResponse get(Authentication auth) {
        return cartService.getCart(auth.getName());
    }

    @PostMapping
    public CartResponse add(Authentication auth, @Valid @RequestBody CartAddRequest req) {
        return cartService.add(auth.getName(), req);
    }

    @PutMapping("/{itemId}")
    public CartResponse update(Authentication auth, @PathVariable Long itemId,
                               @Valid @RequestBody CartUpdateRequest req) {
        return cartService.updateQuantity(auth.getName(), itemId, req.quantity());
    }

    @DeleteMapping("/{itemId}")
    public CartResponse remove(Authentication auth, @PathVariable Long itemId) {
        return cartService.remove(auth.getName(), itemId);
    }
}
