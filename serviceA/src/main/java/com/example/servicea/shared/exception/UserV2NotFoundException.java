package com.example.servicea.shared.exception;

import java.util.UUID;

public class UserV2NotFoundException extends RuntimeException {
    public UserV2NotFoundException(UUID id) {
        super("User not found: " + id + ".");
    }
}
