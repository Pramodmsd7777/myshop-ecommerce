package com.shop.repository;

import com.shop.model.WishlistItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    @EntityGraph(attributePaths = {"product", "product.category"})
    List<WishlistItem> findByUserEmailOrderByIdDesc(String email);

    boolean existsByUserEmailAndProductId(String email, Long productId);

    void deleteByUserEmailAndProductId(String email, Long productId);

    @Modifying
    @Query("DELETE FROM WishlistItem w WHERE w.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);
}
