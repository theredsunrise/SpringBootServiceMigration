package com.example.servicea.shared.infrastructure.adapter.in.web.model;

import java.util.List;

public record PageWrapper<T>(
        List<T> items,
        PageInfo pagination
) {

    public record PageInfo(
            int currentPage,
            int totalPages,
            int pageSize,
            long totalItems
    ) {
    }
}

