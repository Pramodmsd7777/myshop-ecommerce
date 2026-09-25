package com.shop.dto;

import com.shop.model.CartItem;
import com.shop.model.Product;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public class CartDtos {
    public record CartAddRequest(@NotNull Long productId, @NotNull @Min(1) @Max(99) Integer quantity) {}

    public record CartUpdateRequest(@NotNull @Min(1) @Max(99) Integer quantity) {}

    public record CartItemResponse(Long id, Long productId, String name, String imageUrl,
                                   BigDecimal price, Integer quantity, BigDecimal subtotal, Integer stock) {
        public static CartItemResponse from(CartItem i) {
            Product p = i.getProduct();
            return new CartItemResponse(i.getId(), p.getId(), p.getName(), p.getImageUrl(), p.getPrice(),
                    i.getQuantity(), p.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())), p.getStock());
        }
    }

    public record CartResponse(List<CartItemResponse> items, int totalItems, BigDecimal total) {}
}
