package com.shop.service;

import com.shop.dto.AdminDtos.*;
import com.shop.model.Order;
import com.shop.model.OrderStatus;
import com.shop.repository.OrderRepository;
import com.shop.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class AdminOrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Transactional
    public Page<AdminOrderSummary> list(OrderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50),
                Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        Page<Order> result = (status == null)
                ? orderRepository.findAllBy(pageable)
                : orderRepository.findByStatus(status, pageable);
        return result.map(AdminOrderSummary::from);
    }

    @Transactional
    public AdminOrderResponse get(Long id) {
        return orderRepository.findWithDetailsById(id)
                .map(AdminOrderResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    @Transactional
    public AdminOrderResponse updateStatus(Long id, OrderStatus next) {
        Order order = orderRepository.findForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        OrderStatus current = order.getStatus();

        if (current == next) return AdminOrderResponse.from(order);   // a double click changes nothing
        if (!current.canMoveTo(next)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot change status from " + current + " to " + next);
        }

        if (next == OrderStatus.CANCELLED) {
            // give the stock back, in the same order checkout took it, so concurrent cancels can't deadlock
            order.getItems().stream()
                    .sorted(Comparator.comparing(i -> i.getProduct().getId()))
                    .forEach(i -> productRepository.increaseStock(i.getProduct().getId(), i.getQuantity()));
        }
        order.setStatus(next);
        return AdminOrderResponse.from(order);
    }
}
