package com.example.servicea.migration.application.exception;

public class ValidationJobStatusUnknownException extends RuntimeException {
    public ValidationJobStatusUnknownException() {
        super("Validation job status unknown. Try again please.");
    }
}
