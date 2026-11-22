package com.example.servicea.migration.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(example = """
        {
          "instanceId": "service-a-1",
          "batchSize": 100,
          "startedAt": "2026-11-19T12:00:00Z",
          "durationMs": 1250,
          "fromId": "8f2a9d72-5f46-4c54-9ea1-10ee21d227c5",
          "toId": "e6bf3ad4-24f3-43c3-a127-8d230a11a42d",
          "status": "PROCESSED"
        }
        """)
public record BackFillStatusResponse(
        @Schema(description = "Identifier of the service instance.", example = "service-a-1")
        String instanceId,
        @Schema(description = "Number of users in the batch.", example = "100")
        int batchSize,
        @Schema(description = "When this batch started.", example = "2026-11-19T12:00:00Z")
        Instant startedAt,
        @Schema(description = "Batch duration in milliseconds.", example = "1250")
        long durationMs,
        @Schema(description = "First processed user ID, when available.", example = "8f2a9d72-5f46-4c54-9ea1-10ee21d227c5")
        UUID fromId,
        @Schema(description = "Last processed user ID, when available.", example = "e6bf3ad4-24f3-43c3-a127-8d230a11a42d")
        UUID toId,
        @Schema(description = "Batch result or failure description.", example = "PROCESSED")
        String status) {
}
