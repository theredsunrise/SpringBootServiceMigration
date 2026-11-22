package com.example.servicea.shared.exception;

import java.util.UUID;

public class UserV2ConflictException extends RuntimeException {
    public UserV2ConflictException(UUID id) {
        super("User has been modified: " + id + ".");
    }
}
