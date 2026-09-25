package com.shop.service;

import com.shop.dto.ProductDtos.*;
import com.shop.model.Category;
import com.shop.model.Product;
import com.shop.repository.CategoryRepository;
import com.shop.repository.ProductRepository;
import com.shop.repository.ProductSpecs;
import com.shop.repository.WishlistItemRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductService {
    private static final Set<String> SORTABLE = Set.of("id", "name", "price");

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final WishlistItemRepository wishlistRepository;

    @Transactional
    public ProductResponse create(ProductRequest req) {
        Product p = new Product();
        apply(p, req);
        return ProductResponse.from(productRepository.save(p));
    }

    @Transactional
    public Page<ProductResponse> search(String search, String category, Long categoryId,
                                        BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "minPrice cannot be greater than maxPrice");
        }

        Sort sort = pageable.getSort();
        for (Sort.Order o : sort) {
            if (!SORTABLE.contains(o.getProperty())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Cannot sort by '" + o.getProperty() + "'. Allowed: id, name, price");
            }
        }
        if (sort.getOrderFor("id") == null) sort = sort.and(Sort.by("id"));   // stable tie-breaker for paging
        Pageable safe = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Specification<Product> spec = Specification.allOf(
                ProductSpecs.nameContains(search),
                ProductSpecs.categoryName(category),
                ProductSpecs.categoryId(categoryId),
                ProductSpecs.minPrice(minPrice),
                ProductSpecs.maxPrice(maxPrice));

        return productRepository.findAll(spec, safe).map(ProductResponse::from);
    }

    @Transactional
    public ProductResponse get(Long id) {
        return ProductResponse.from(find(id));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest req) {
        Product p = find(id);
        apply(p, req);
        return ProductResponse.from(p);
    }

    @Transactional
    public void delete(Long id) {
        wishlistRepository.deleteByProductId(id);   // wishlist rows aren't important; don't let them block a delete
        productRepository.delete(find(id));
        productRepository.flush();                  // surface FK errors (cart/order references) here
    }

    private Product find(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    private void apply(Product p, ProductRequest req) {
        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found"));
        p.setName(req.name().trim());
        p.setDescription(req.description());
        p.setPrice(req.price());
        p.setStock(req.stock());
        p.setImageUrl(req.imageUrl());
        p.setCategory(category);
    }
}
