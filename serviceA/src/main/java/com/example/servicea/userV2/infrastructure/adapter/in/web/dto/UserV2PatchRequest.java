package com.example.servicea.userV2.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(example = """
        {
          "username": "alice",
          "name": "Alice",
          "surname": "Smith"
        }
        """)
public record UserV2PatchRequest(
        @Schema(description = "Replacement username; null leaves it unchanged.", example = "alice")
        @Size(max = 40) @Pattern(regexp = ".*\\S.*") String username,
        @Schema(description = "Replacement display name; null leaves it unchanged.", example = "Alice")
        @Size(max = 100) @Pattern(regexp = ".*\\S.*") String name,
        @Schema(description = "Replacement surname; null leaves it unchanged.", example = "Smith")
        @Size(max = 100) @Pattern(regexp = ".*\\S.*") String surname) {
}
