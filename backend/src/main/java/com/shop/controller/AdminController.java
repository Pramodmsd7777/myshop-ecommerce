package com.shop.controller;

import com.shop.dto.AdminDtos.*;
import com.shop.model.OrderStatus;
import com.shop.service.AdminOrderService;
import com.shop.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")   // second lock behind the URL rule in SecurityConfig
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final AdminOrderService adminOrderService;

    @GetMapping("/stats")
    public AdminStats stats() {
        return adminService.stats();
    }

    @GetMapping("/orders")
    public Page<AdminOrderSummary> orders(@RequestParam(required = false) OrderStatus status,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        return adminOrderService.list(status, page, size);
    }

    @GetMapping("/orders/{id}")
    public AdminOrderResponse order(@PathVariable Long id) {
        return adminOrderService.get(id);
    }

    @PatchMapping("/orders/{id}/status")
    public AdminOrderResponse updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest req) {
        return adminOrderService.updateStatus(id, req.status());
    }

    @GetMapping("/users")
    public Page<AdminUserResponse> users(@RequestParam(required = false) String search,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        return adminService.users(search, page, size);
    }
}
