package com.quickstock.core.controller;

import com.quickstock.core.dto.product.ProductListResponse;
import com.quickstock.core.dto.product.ProductResponse;
import com.quickstock.core.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ProductListResponse listProducts(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        Page<ProductResponse> page = productService.listProducts(
                active,
                sku,
                name,
                currency,
                minPrice,
                maxPrice,
                pageable
        );

        return new ProductListResponse(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getContent()
        );
    }
}

