package com.example.servicea.shared.exception;

import java.util.UUID;

public class UserV1ConflictException extends RuntimeException {
    public UserV1ConflictException(UUID id) {
        super("User has been modified: " + id + ".");
    }
}
