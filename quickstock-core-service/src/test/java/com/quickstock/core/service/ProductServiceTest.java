package com.quickstock.core.service;

import com.quickstock.core.domain.Product;
import com.quickstock.core.dto.product.ProductResponse;
import com.quickstock.core.repository.ProductRepository;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    private static final Product DEFAULT_PRODUCT = product(
            "33333333-3333-3333-3333-333333333333",
            "SKU-SONY-WH1000XM5",
            "Sony WH-1000XM5 Headphones",
            "399.00",
            "USD",
            true
    );

    @Mock
    private ProductRepository productRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository);
    }

    @Test
    void listProducts_mapsEntityPageToResponsePage() {
        whenFindAllReturn(List.of(DEFAULT_PRODUCT), DEFAULT_PAGEABLE, 1);

        Page<ProductResponse> result = productService.listProducts(
                true, "iphone", "apple", "usd", new BigDecimal("900"), new BigDecimal("1200"), DEFAULT_PAGEABLE
        );

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        ProductResponse response = result.getContent().get(0);
        assertEquals(UUID.fromString("33333333-3333-3333-3333-333333333333"), response.id());
        assertEquals("SKU-SONY-WH1000XM5", response.sku());
        assertEquals("Sony WH-1000XM5 Headphones", response.name());
        assertEquals(new BigDecimal("399.00"), response.price());
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
        whenFindAllReturn(List.of(DEFAULT_PRODUCT), DEFAULT_PAGEABLE, 1);

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
        whenFindAllReturn(List.of(DEFAULT_PRODUCT), DEFAULT_PAGEABLE, 1);

        Page<ProductResponse> result = productService.listProducts(
                null, null, null, null, new BigDecimal("1000"), null, DEFAULT_PAGEABLE
        );

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().get(0).price().compareTo(new BigDecimal("399.00")) >= 0);
        verify(productRepository, times(1)).findAll(any(Specification.class), eq(DEFAULT_PAGEABLE));
    }

    @Test
    @DisplayName("normalizes currency input (e.g., usd) before repository query setup")
    void listProducts_normalizesCurrencyInput() {
        whenFindAllReturn(List.of(DEFAULT_PRODUCT), DEFAULT_PAGEABLE, 1);

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

    @Test
    @DisplayName("applies active=false filter predicate when active is provided")
    void listProducts_appliesActiveFalsePredicate() {
        whenFindAllReturn(List.of(DEFAULT_PRODUCT), DEFAULT_PAGEABLE, 1);

        @SuppressWarnings({"rawtypes", "unchecked"})
        ArgumentCaptor<Specification<Product>> captor =
                (ArgumentCaptor) ArgumentCaptor.forClass(Specification.class);

        productService.listProducts(false, null, null, null, null, null, DEFAULT_PAGEABLE);

        verify(productRepository).findAll(captor.capture(), eq(DEFAULT_PAGEABLE));
        Specification<Product> spec = captor.getValue();

        Root<Product> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Predicate conjunction = mock(Predicate.class);
        Predicate activePredicate = mock(Predicate.class);

        @SuppressWarnings("unchecked")
        Path<Object> activePath = (Path<Object>) mock(Path.class);

        when(cb.conjunction()).thenReturn(conjunction);
        when(root.get("active")).thenReturn(activePath);
        when(cb.equal(activePath, false)).thenReturn(activePredicate);
        when(cb.and(conjunction, activePredicate)).thenReturn(activePredicate);

        spec.toPredicate(root, query, cb);

        verify(root).get("active");
        verify(cb).equal(activePath, false);
    }

    @Test
    @DisplayName("applies sku LIKE predicate using trim and lowercase normalization")
    void listProducts_appliesSkuLikePredicate_withNormalization() {
        whenFindAllReturn(List.of(DEFAULT_PRODUCT), DEFAULT_PAGEABLE, 1);

        @SuppressWarnings({"rawtypes", "unchecked"})
        ArgumentCaptor<Specification<Product>> captor =
                (ArgumentCaptor) ArgumentCaptor.forClass(Specification.class);

        productService.listProducts(null, "  IpHoNe-15  ", null, null, null, null, DEFAULT_PAGEABLE);

        verify(productRepository).findAll(captor.capture(), eq(DEFAULT_PAGEABLE));
        Specification<Product> spec = captor.getValue();

        Root<Product> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Predicate conjunction = mock(Predicate.class);
        Predicate skuPredicate = mock(Predicate.class);

        @SuppressWarnings("unchecked")
        Path<String> skuPath = (Path<String>) mock(Path.class);
        @SuppressWarnings("unchecked")
        Expression<String> loweredSkuExpression = (Expression<String>) mock(Expression.class);

        when(cb.conjunction()).thenReturn(conjunction);
        when(root.get("sku")).thenReturn((Path) skuPath);
        when(cb.lower(skuPath)).thenReturn(loweredSkuExpression);
        when(cb.like(loweredSkuExpression, "%iphone-15%")).thenReturn(skuPredicate);
        when(cb.and(conjunction, skuPredicate)).thenReturn(skuPredicate);

        spec.toPredicate(root, query, cb);

        verify(root).get("sku");
        verify(cb).lower(skuPath);
        verify(cb).like(loweredSkuExpression, "%iphone-15%");
    }

    @Test
    @DisplayName("applies maxPrice inclusive predicate when only maxPrice is provided")
    void listProducts_appliesOnlyMaxPricePredicate() {
        //TODO:
        // Hint:
        // 1) Call service with maxPrice only.
        // 2) Capture Specification<Product> and execute toPredicate(...).
        // 3) Verify cb.lessThanOrEqualTo(root.get("price"), maxPrice) is used.
        // 4) Keep repository response minimal (empty page) to focus on predicate path.
    }

    private static Product product(String id, String sku, String name, String price, String currency, boolean active) {
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
