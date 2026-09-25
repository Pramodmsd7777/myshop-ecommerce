package com.shop.service;

import com.shop.dto.ProductDtos.ProductResponse;
import com.shop.model.Product;
import com.shop.model.User;
import com.shop.model.WishlistItem;
import com.shop.repository.ProductRepository;
import com.shop.repository.UserRepository;
import com.shop.repository.WishlistItemRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistService {
    private final WishlistItemRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional
    public List<ProductResponse> list(String email) {
        return wishlistRepository.findByUserEmailOrderByIdDesc(email).stream()
                .map(w -> ProductResponse.from(w.getProduct())).toList();
    }

    @Transactional
    public List<ProductResponse> add(String email, Long productId) {
        if (!wishlistRepository.existsByUserEmailAndProductId(email, productId)) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
            User user = userRepository.findByEmail(email).orElseThrow();
            WishlistItem item = new WishlistItem();
            item.setUser(user);
            item.setProduct(product);
            wishlistRepository.save(item);
        }
        return list(email);
    }

    @Transactional
    public List<ProductResponse> remove(String email, Long productId) {
        wishlistRepository.deleteByUserEmailAndProductId(email, productId);
        return list(email);
    }
}
