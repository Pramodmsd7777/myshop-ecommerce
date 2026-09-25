package com.shop.model;

import java.util.EnumSet;
import java.util.Set;

public enum OrderStatus {
    PENDING, PAID, SHIPPED, DELIVERED, CANCELLED;

    public Set<OrderStatus> allowedNext() {
        return switch (this) {
            case PENDING -> EnumSet.of(PAID, SHIPPED, CANCELLED);
            case PAID -> EnumSet.of(SHIPPED, CANCELLED);
            case SHIPPED -> EnumSet.of(DELIVERED);
            case DELIVERED, CANCELLED -> EnumSet.noneOf(OrderStatus.class);
        };
    }

    public boolean canMoveTo(OrderStatus target) {
        return allowedNext().contains(target);
    }
}
