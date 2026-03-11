package com.quickstock.core.service;

import com.quickstock.core.domain.Product;
import com.quickstock.core.dto.product.ProductResponse;
import com.quickstock.core.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    private static final Pageable DEFAULT_PAGEABLE = PageRequest.of(0, 10);

    @Mock
    private ProductRepository productRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository);
    }

    @Test
    void listProducts_mapsEntityPageToResponsePage() {
        Product product = product(
                "11111111-1111-1111-1111-111111111111",
                "SKU-APPLE-IPHONE-15",
                "Apple iPhone 15 128GB",
                "999.00",
                "USD",
                true
        );
        whenFindAllReturn(List.of(product), DEFAULT_PAGEABLE, 1);

        Page<ProductResponse> result = productService.listProducts(
                true, "iphone", "apple", "usd", new BigDecimal("900"), new BigDecimal("1200"), DEFAULT_PAGEABLE
        );

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        ProductResponse response = result.getContent().get(0);
        assertEquals(UUID.fromString("11111111-1111-1111-1111-111111111111"), response.id());
        assertEquals("SKU-APPLE-IPHONE-15", response.sku());
        assertEquals("Apple iPhone 15 128GB", response.name());
        assertEquals(new BigDecimal("999.00"), response.price());
        assertEquals("USD", response.currency());
        assertTrue(response.active());
    }

    @Test
    void listProducts_passesSpecificationAndPageableToRepository() {
        Pageable pageable = PageRequest.of(1, 5);
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(Page.empty(pageable));

        productService.listProducts(null, null, null, null, null, null, pageable);

        verify(productRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("returns empty page when repository returns no products")
    void listProducts_returnsEmptyPageWhenRepositoryIsEmpty() {
        when(productRepository.findAll(any(Specification.class), eq(DEFAULT_PAGEABLE)))
                .thenReturn(Page.empty(DEFAULT_PAGEABLE));

        Page<ProductResponse> result = productService.listProducts(
                null, null, null, null, null, null, DEFAULT_PAGEABLE
        );

        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
        assertEquals(0, result.getContent().size());
        assertEquals(0, result.getNumber());
        assertEquals(10, result.getSize());
    }

    @Test
    @DisplayName("handles blank sku/name/currency inputs without throwing")
    void listProducts_handlesBlankStringFilters() {
        Product product = product(
                "11111111-1111-1111-1111-111111111111",
                "SKU-APPLE-IPHONE-15",
                "Apple iPhone 15 128GB",
                "999.00",
                "USD",
                true
        );
        whenFindAllReturn(List.of(product), DEFAULT_PAGEABLE, 1);

        Page<ProductResponse> result = productService.listProducts(
                true, " ", " ", " ", null, null, DEFAULT_PAGEABLE
        );

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        verify(productRepository, times(1)).findAll(any(Specification.class), eq(DEFAULT_PAGEABLE));
    }

    @Test
    @DisplayName("accepts only minPrice filter and queries repository successfully")
    void listProducts_handlesOnlyMinPriceFilter() {
        Product product = product(
                "22222222-2222-2222-2222-222222222222",
                "SKU-SAMSUNG-S24-256",
                "Samsung Galaxy S24 256GB",
                "1099.00",
                "USD",
                true
        );
        whenFindAllReturn(List.of(product), DEFAULT_PAGEABLE, 1);

        Page<ProductResponse> result = productService.listProducts(
                null, null, null, null, new BigDecimal("1000"), null, DEFAULT_PAGEABLE
        );

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().get(0).price().compareTo(new BigDecimal("1000")) >= 0);
        verify(productRepository, times(1)).findAll(any(Specification.class), eq(DEFAULT_PAGEABLE));
    }

    @Test
    @DisplayName("normalizes currency input (e.g., usd) before repository query setup")
    void listProducts_normalizesCurrencyInput() {
        Product product = product(
                "33333333-3333-3333-3333-333333333333",
                "SKU-SONY-WH1000XM5",
                "Sony WH-1000XM5 Headphones",
                "399.00",
                "USD",
                true
        );
        whenFindAllReturn(List.of(product), DEFAULT_PAGEABLE, 1);

        Page<ProductResponse> result = productService.listProducts(
                null, null, null, "usd", null, null, DEFAULT_PAGEABLE
        );

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals("USD", result.getContent().get(0).currency());
        verify(productRepository, times(1)).findAll(any(Specification.class), eq(DEFAULT_PAGEABLE));
    }

    @Test
    @DisplayName("propagates repository runtime exception to caller")
    void listProducts_propagatesRepositoryException() {
        RuntimeException expected = new RuntimeException("db failure");
        when(productRepository.findAll(any(Specification.class), eq(DEFAULT_PAGEABLE))).thenThrow(expected);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> productService.listProducts(
                null, null, null, null, null, null, DEFAULT_PAGEABLE
        ));

        assertSame(expected, thrown);
        verify(productRepository, times(1)).findAll(any(Specification.class), eq(DEFAULT_PAGEABLE));
    }

    private Product product(String id, String sku, String name, String price, String currency, boolean active) {
        Product product = new Product();
        product.setId(UUID.fromString(id));
        product.setSku(sku);
        product.setName(name);
        product.setPrice(new BigDecimal(price));
        product.setCurrency(currency);
        product.setActive(active);
        return product;
    }

    private void whenFindAllReturn(List<Product> products, Pageable pageable, long totalElements) {
        when(productRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(products, pageable, totalElements));
    }
}
