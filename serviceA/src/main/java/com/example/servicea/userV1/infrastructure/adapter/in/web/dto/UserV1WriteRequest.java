package com.example.servicea.userV1.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(example = """
        {
          "username": "alice",
          "name": "Alice Smith"
        }
        """)
public record UserV1WriteRequest(
        @Schema(description = "Unique, case-sensitive username.", example = "alice")
        @NotBlank @Size(max = 40) String username,
        @Schema(description = "User's display name.", example = "Alice Smith")
        @NotBlank @Size(max = 100) String name) {
}
