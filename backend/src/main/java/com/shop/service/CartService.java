package com.shop.service;

import com.shop.dto.CartDtos.*;
import com.shop.model.Cart;
import com.shop.model.CartItem;
import com.shop.model.Product;
import com.shop.model.User;
import com.shop.repository.CartItemRepository;
import com.shop.repository.CartRepository;
import com.shop.repository.ProductRepository;
import com.shop.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public CartResponse getCart(String email) {
        return toResponse(cartFor(email));
    }

    @Transactional
    public CartResponse add(String email, CartAddRequest req) {
        Cart cart = cartFor(email);
        Product product = productRepository.findById(req.productId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        CartItem existing = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(product.getId()))
                .findFirst().orElse(null);

        int newQty = (existing == null ? 0 : existing.getQuantity()) + req.quantity();
        checkStock(product, newQty);

        if (existing == null) {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(newQty);
            cart.getItems().add(cartItemRepository.save(item));
        } else {
            existing.setQuantity(newQty);
        }
        return toResponse(cart);
    }

    @Transactional
    public CartResponse updateQuantity(String email, Long itemId, int quantity) {
        CartItem item = findOwnedItem(email, itemId);
        checkStock(item.getProduct(), quantity);
        item.setQuantity(quantity);
        return toResponse(item.getCart());
    }

    @Transactional
    public CartResponse remove(String email, Long itemId) {
        CartItem item = findOwnedItem(email, itemId);
        Cart cart = item.getCart();
        cart.getItems().remove(item);        // orphanRemoval deletes the row
        return toResponse(cart);
    }

    private Cart cartFor(String email) {
        return cartRepository.findByUserEmail(email).orElseGet(() -> {
            User user = userRepository.findByEmail(email).orElseThrow();
            Cart c = new Cart();
            c.setUser(user);
            return cartRepository.save(c);
        });
    }

    private CartItem findOwnedItem(String email, Long itemId) {
        return cartItemRepository.findByIdAndCartUserEmail(itemId, email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));
    }

    private void checkStock(Product p, int qty) {
        if (p.getStock() < qty) {
            String msg = p.getStock() == 0
                    ? "'" + p.getName() + "' is out of stock"
                    : "Only " + p.getStock() + " of '" + p.getName() + "' in stock";
            throw new ResponseStatusException(HttpStatus.CONFLICT, msg);
        }
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .sorted(Comparator.comparing(CartItem::getId))
                .map(CartItemResponse::from)
                .toList();
        BigDecimal total = items.stream().map(CartItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int count = items.stream().mapToInt(CartItemResponse::quantity).sum();
        return new CartResponse(items, count, total);
    }
}
