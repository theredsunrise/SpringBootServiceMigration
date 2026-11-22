package com.example.servicea.userV2.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(example = """
        {
          "username": "alice",
          "name": "Alice",
          "surname": "Smith"
        }
        """)
public record UserV2WriteRequest(
        @Schema(description = "Unique, case-sensitive username for V2 users.", example = "alice")
        @NotBlank @Size(max = 40) String username,
        @Schema(description = "User's display name.", example = "Alice")
        @NotBlank @Size(max = 100) String name,
        @Schema(description = "User's surname.", example = "Smith")
        @NotBlank @Size(max = 100) String surname) {
}
