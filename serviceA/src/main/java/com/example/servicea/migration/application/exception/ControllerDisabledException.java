package com.example.servicea.migration.application.exception;

public class ControllerDisabledException extends RuntimeException {
    public ControllerDisabledException(String message) {
        super(message);
    }
}
