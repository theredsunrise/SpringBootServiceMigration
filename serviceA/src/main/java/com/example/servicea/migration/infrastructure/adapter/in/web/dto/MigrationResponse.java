package com.example.servicea.migration.infrastructure.adapter.in.web.dto;

import com.example.servicea.migration.domain.entity.MigrationState;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(example = """
        {
          "id": 1,
          "state": "ActivatedV1",
          "updatedAt": "2026-11-16T20:00:00Z"
        }
        """)
public record MigrationResponse(
        @Schema(description = "Fixed singleton migration ID.", example = "1") int id,
        @Schema(description = "Current migration rollout state.", example = "ActivatedV1") MigrationState state,
        @Schema(description = "Time when the migration state was last changed.", example = "2026-11-16T20:00:00Z")
        Instant updatedAt) {
}
