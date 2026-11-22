package com.example.servicea.migration.domain.model;

import com.example.servicea.userV1.domain.UserV1;
import com.example.servicea.userV2.domain.UserV2;

public record ValidationStatus(String message) {
    public ValidationStatus {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message cannot be null.");
        }
    }

    public static ValidationStatus UserV2DoesNotExist(UserV1 userV1) {
        return new ValidationStatus("User V2 does not exist along the user V1 with the id: %s.".formatted(userV1.id()));
    }

    public static ValidationStatus UserV1DoesNotExist(UserV2 userV2) {
        return new ValidationStatus("User V1 does not exist along the user V2 with the id: %s.".formatted(userV2.id()));
    }
}
