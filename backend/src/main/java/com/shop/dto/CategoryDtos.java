package com.shop.dto;

import com.shop.model.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoryDtos {
    public record CategoryRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 255) String description) {}

    public record CategoryResponse(Long id, String name, String description) {
        public static CategoryResponse from(Category c) {
            return new CategoryResponse(c.getId(), c.getName(), c.getDescription());
        }
    }
}
