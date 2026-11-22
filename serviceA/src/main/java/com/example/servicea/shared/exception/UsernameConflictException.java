package com.example.servicea.shared.exception;

public class UsernameConflictException extends RuntimeException {
    public UsernameConflictException(String username) {
        super("Username already exists: " + username + ".");
    }
}
