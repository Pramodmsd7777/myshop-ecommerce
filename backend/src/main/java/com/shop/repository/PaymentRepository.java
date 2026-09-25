package com.shop.repository;

import com.shop.model.Payment;
import com.shop.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findFirstByOrderIdAndStatusOrderByIdDesc(Long orderId, PaymentStatus status);
    Optional<Payment> findByRazorpayOrderIdAndOrderUserEmail(String razorpayOrderId, String email);
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);
}
