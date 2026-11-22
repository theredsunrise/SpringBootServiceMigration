package com.example.servicea.migration.application.exception;

public class UnsupportedMigrationStepException extends RuntimeException {
    public UnsupportedMigrationStepException(String message) {
        super(message);
    }
}
