package com.example.servicea.migration.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A recent migration validation error.")
public record ValidationStatusResponse(
        @Schema(description = "Validation error message.",
                example = "User V2 does not exist along the user V1 with the id: ...")
        String message) {
}
