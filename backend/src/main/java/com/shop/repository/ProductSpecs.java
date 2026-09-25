package com.shop.repository;

import com.shop.model.Product;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;

public final class ProductSpecs {
    private ProductSpecs() {}

    // each filter returns null when its value is missing, which means "no restriction"
    public static Specification<Product> nameContains(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) return null;
            return cb.like(cb.lower(root.get("name")), "%" + escapeLike(search.trim().toLowerCase()) + "%", '\\');
        };
    }

    public static Specification<Product> categoryName(String name) {
        return (root, query, cb) -> (name == null || name.isBlank()) ? null
                : cb.equal(cb.lower(root.get("category").get("name")), name.trim().toLowerCase());
    }

    public static Specification<Product> categoryId(Long id) {
        return (root, query, cb) -> id == null ? null : cb.equal(root.get("category").get("id"), id);
    }

    public static Specification<Product> minPrice(BigDecimal min) {
        return (root, query, cb) -> min == null ? null : cb.greaterThanOrEqualTo(root.get("price"), min);
    }

    public static Specification<Product> maxPrice(BigDecimal max) {
        return (root, query, cb) -> max == null ? null : cb.lessThanOrEqualTo(root.get("price"), max);
    }

    private static String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
