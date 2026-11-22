package com.example.servicea.shared.exception;

import java.util.UUID;

public class UserV1NotFoundException extends RuntimeException {
    public UserV1NotFoundException(UUID id) {
        super("User not found: " + id + ".");
    }
}
