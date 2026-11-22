package com.example.servicea.migration.infrastructure.adapter.in.web.dto;

import com.example.servicea.migration.domain.entity.MigrationState;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(example = """
        {
          "state": "ActivatedV1"
        }
        """)
public record MigrationStateRequest(
        @Schema(
                description = "Migration rollout state.",
                example = "ActivatedV1")
        @NotNull MigrationState state) {
}
