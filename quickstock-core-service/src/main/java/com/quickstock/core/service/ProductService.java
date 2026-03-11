package com.quickstock.core.service;

import com.quickstock.core.domain.Product;
import com.quickstock.core.dto.product.ProductResponse;
import com.quickstock.core.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Locale;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Page<ProductResponse> listProducts(Boolean active,
                                              String sku,
                                              String name,
                                              String currency,
                                              BigDecimal minPrice,
                                              BigDecimal maxPrice,
                                              Pageable pageable) {
        Specification<Product> spec = (root, query, cb) -> cb.conjunction();

        if (active != null) {

            spec = spec.and((root, query, cb) -> cb.equal(root.get("active"), active));
        }
        if (sku != null && !sku.isBlank()) {
            String pattern = "%" + sku.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("sku")), pattern));
        }
        if (name != null && !name.isBlank()) {
            String pattern = "%" + name.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern));
        }
        if (currency != null && !currency.isBlank()) {
            String normalized = currency.trim().toUpperCase(Locale.ROOT);
            spec = spec.and((root, query, cb) -> cb.equal(root.get("currency"), normalized));
        }
        if (minPrice != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        }
        if (maxPrice != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        }

        return productRepository.findAll(spec, pageable).map(this::toResponse);
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getPrice(),
                product.getCurrency(),
                product.isActive()
        );
    }
}
