package com.shop.dto;

import com.shop.model.Category;
import com.shop.model.Product;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ProductDtos {
    public record ProductRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 2000) String description,
        @NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal price,
        @NotNull @Min(0) Integer stock,
        String imageUrl,
        @NotNull Long categoryId) {}

    public record ProductResponse(Long id, String name, String description, BigDecimal price,
                                  Integer stock, String imageUrl, Long categoryId, String categoryName) {
        public static ProductResponse from(Product p) {
            Category c = p.getCategory();
            return new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getPrice(),
                    p.getStock(), p.getImageUrl(),
                    c != null ? c.getId() : null, c != null ? c.getName() : null);
        }
    }
}
