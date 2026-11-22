package com.example.servicea.migration.application.exception;

public class MigrationNotFoundException extends RuntimeException {
    public MigrationNotFoundException() {
        super("Migration state with ID 1 was not found.");
    }
}
