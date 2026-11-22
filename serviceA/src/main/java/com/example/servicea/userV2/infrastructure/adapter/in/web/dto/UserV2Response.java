package com.example.servicea.userV2.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(example = """
        {
          "id": "8f2a9d72-5f46-4c54-9ea1-10ee21d227c5",
          "username": "alice",
          "name": "Alice",
          "surname": "Smith",
          "createdAt": "2026-01-01T12:00:00Z",
          "updatedAt": "2026-01-01T12:00:00Z"
        }
        """)
public record UserV2Response(
        @Schema(description = "V2 user identifier.", example = "8f2a9d72-5f46-4c54-9ea1-10ee21d227c5")
        UUID id,
        @Schema(description = "Unique V2 username.", example = "alice")
        String username,
        @Schema(description = "User's display name.", example = "Alice")
        String name,
        @Schema(description = "User's surname.", example = "Smith")
        String surname,
        @Schema(description = "Creation time in UTC.", example = "2026-01-01T12:00:00Z")
        Instant createdAt,
        @Schema(description = "Last update time in UTC.", example = "2026-01-01T12:00:00Z")
        Instant updatedAt) {
}
