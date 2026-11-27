package com.example.servicea.shared.infrastructure.adapter.in.web.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Paginated response wrapper containing items and pagination metadata.")
public record PageWrapperResponse<T>(
        @Schema(description = "List of items for the current page.")
        List<T> items,

        @Schema(description = "Pagination metadata.")
        PageInfo pagination
) {

    @Schema(description = "Pagination metadata details.")
    public record PageInfo(
            @Schema(description = "Current page number (0-based).", example = "0")
            int currentPage,

            @Schema(description = "Total number of available pages.", example = "5")
            int totalPages,

            @Schema(description = "Number of items per page.", example = "20")
            int pageSize,

            @Schema(description = "Total number of items across all pages.", example = "95")
            long totalItems
    ) {
    }
}
