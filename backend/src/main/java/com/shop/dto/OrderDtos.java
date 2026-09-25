package com.shop.dto;

import com.shop.model.Order;
import com.shop.model.OrderItem;
import com.shop.model.OrderStatus;
import com.shop.model.Product;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDtos {
    public record CheckoutRequest(
        @NotBlank(message = "Shipping address is required")
        @Size(max = 255, message = "Address must be at most 255 characters") String shippingAddress) {}

    public record OrderItemResponse(Long productId, String name, String imageUrl,
                                    BigDecimal price, Integer quantity, BigDecimal subtotal) {
        public static OrderItemResponse from(OrderItem i) {
            Product p = i.getProduct();
            return new OrderItemResponse(p.getId(), p.getName(), p.getImageUrl(),
                    i.getPriceAtPurchase(), i.getQuantity(),
                    i.getPriceAtPurchase().multiply(BigDecimal.valueOf(i.getQuantity())));
        }
    }

    public record OrderResponse(Long id, OrderStatus status, LocalDateTime createdAt,
                                String shippingAddress, BigDecimal totalAmount,
                                int totalItems, List<OrderItemResponse> items) {
        public static OrderResponse from(Order o) {
            List<OrderItemResponse> items = o.getItems().stream().map(OrderItemResponse::from).toList();
            int count = items.stream().mapToInt(OrderItemResponse::quantity).sum();
            return new OrderResponse(
        o.getId(),
        o.getStatus(),
        o.getCreatedAt(),
        o.getShippingAddress(),
        o.getTotalAmount(),
        count,
        items
);
        }
    }
}
