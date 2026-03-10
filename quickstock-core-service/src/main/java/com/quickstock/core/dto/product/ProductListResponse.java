package com.quickstock.core.dto.product;

import java.util.List;

public record ProductListResponse(
        int page,
        int size,
        long totalElements,
        int totalPages,
        List<ProductResponse> items
) {
}

