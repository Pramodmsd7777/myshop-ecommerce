package com.shop.controller;

import com.shop.dto.ProductDtos.ProductResponse;
import com.shop.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {
    private final WishlistService wishlistService;

    @GetMapping
    public List<ProductResponse> list(Authentication auth) {
        return wishlistService.list(auth.getName());
    }

    @PostMapping("/{productId}")
    public List<ProductResponse> add(Authentication auth, @PathVariable Long productId) {
        return wishlistService.add(auth.getName(), productId);
    }

    @DeleteMapping("/{productId}")
    public List<ProductResponse> remove(Authentication auth, @PathVariable Long productId) {
        return wishlistService.remove(auth.getName(), productId);
    }
}
