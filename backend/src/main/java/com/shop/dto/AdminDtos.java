package com.shop.dto;

import com.shop.model.Order;
import com.shop.model.OrderStatus;
import com.shop.model.Role;
import com.shop.model.User;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.shop.dto.OrderDtos.OrderItemResponse;

public class AdminDtos {
    public record StatusUpdateRequest(@NotNull OrderStatus status) {}

    public record AdminStats(long totalUsers, long totalProducts, long totalOrders,
                             BigDecimal totalRevenue, Map<OrderStatus, Long> ordersByStatus) {}

    public record AdminUserResponse(Long id, String name, String email, Role role, LocalDateTime createdAt) {
        public static AdminUserResponse from(User u) {
            return new AdminUserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole(), u.getCreatedAt());
        }
    }

    public record AdminOrderSummary(Long id, Long userId, String userName, String userEmail,
                                    OrderStatus status, Set<OrderStatus> nextStatuses,
                                    BigDecimal totalAmount, LocalDateTime createdAt) {
        public static AdminOrderSummary from(Order o) {
            User u = o.getUser();
            return new AdminOrderSummary(o.getId(), u.getId(), u.getName(), u.getEmail(),
                    o.getStatus(), o.getStatus().allowedNext(), o.getTotalAmount(), o.getCreatedAt());
        }
    }

    public record AdminOrderResponse(Long id, String userName, String userEmail, OrderStatus status,
                                     Set<OrderStatus> nextStatuses, String shippingAddress,
                                     BigDecimal totalAmount, LocalDateTime createdAt,
                                     List<OrderItemResponse> items) {
        public static AdminOrderResponse from(Order o) {
            User u = o.getUser();
            return new AdminOrderResponse(o.getId(), u.getName(), u.getEmail(), o.getStatus(),
                    o.getStatus().allowedNext(), o.getShippingAddress(), o.getTotalAmount(), o.getCreatedAt(),
                    o.getItems().stream().map(OrderItemResponse::from).toList());
        }
    }
}
