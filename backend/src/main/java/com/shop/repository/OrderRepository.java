package com.shop.repository;

import com.shop.model.Order;
import com.shop.model.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"items", "items.product"})
    List<Order> findByUserEmailOrderByCreatedAtDesc(String email);

    @EntityGraph(attributePaths = {"items", "items.product"})
    Optional<Order> findByIdAndUserEmail(Long id, String email);

    @EntityGraph(attributePaths = "user")
    Page<Order> findAllBy(Pageable pageable);

    @EntityGraph(attributePaths = "user")
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "items", "items.product"})
    Optional<Order> findWithDetailsById(Long id);

    // locks the row so two requests can't change the same order (or restore stock) twice
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findForUpdate(@Param("id") Long id);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status IN :statuses")
    BigDecimal sumRevenue(@Param("statuses") Collection<OrderStatus> statuses);

    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countByStatus();
}
