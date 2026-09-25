package com.shop.service;

import com.shop.dto.AdminDtos.*;
import com.shop.model.OrderStatus;
import com.shop.model.Role;
import com.shop.model.User;
import com.shop.repository.OrderRepository;
import com.shop.repository.ProductRepository;
import com.shop.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminService {
    // revenue = orders that are confirmed and not cancelled
    private static final Set<OrderStatus> REVENUE_STATUSES =
            EnumSet.of(OrderStatus.PAID, OrderStatus.SHIPPED, OrderStatus.DELIVERED);

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public AdminStats stats() {
        Map<OrderStatus, Long> byStatus = new EnumMap<>(OrderStatus.class);
        for (OrderStatus s : OrderStatus.values()) byStatus.put(s, 0L);
        for (Object[] row : orderRepository.countByStatus()) {
            byStatus.put((OrderStatus) row[0], (Long) row[1]);
        }
        BigDecimal revenue = orderRepository.sumRevenue(REVENUE_STATUSES);
        return new AdminStats(
                userRepository.countByRole(Role.USER),
                productRepository.count(),
                orderRepository.count(),
                revenue == null ? BigDecimal.ZERO : revenue,
                byStatus);
    }

    @Transactional
    public Page<AdminUserResponse> users(String search, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50),
                Sort.by(Sort.Direction.DESC, "id"));
        Page<User> result = (search == null || search.isBlank())
                ? userRepository.findAll(pageable)
                : userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        search.trim(), search.trim(), pageable);
        return result.map(AdminUserResponse::from);
    }
}
