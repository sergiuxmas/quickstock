package com.quickstock.core.controller;

import com.quickstock.core.dto.product.ProductListResponse;
import com.quickstock.core.dto.product.ProductResponse;
import com.quickstock.core.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductControllerTest {

    @Test
    void mapsPagedResultFromService() {
        ProductService productService = mock(ProductService.class);
        ProductController controller = new ProductController(productService);

        ProductResponse p1 = new ProductResponse(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "SKU-APPLE-IPHONE-15",
                "Apple iPhone 15 128GB",
                new BigDecimal("999.00"),
                "USD",
                true
        );

        when(productService.listProducts(
                eq(true),
                eq("iphone"),
                eq("apple"),
                eq("usd"),
                eq(new BigDecimal("900")),
                eq(new BigDecimal("1200")),
                any()
        )).thenReturn(new PageImpl<>(List.of(p1), PageRequest.of(0, 10), 1));

        ProductListResponse response = controller.listProducts(
                true,
                "iphone",
                "apple",
                "usd",
                new BigDecimal("900"),
                new BigDecimal("1200"),
                PageRequest.of(0, 10)
        );

        assertEquals(0, response.page());
        assertEquals(10, response.size());
        assertEquals(1, response.totalElements());
        assertEquals("SKU-APPLE-IPHONE-15", response.items().get(0).sku());
    }
}
