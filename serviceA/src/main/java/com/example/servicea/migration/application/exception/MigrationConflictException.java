package com.example.servicea.migration.application.exception;

public class MigrationConflictException extends RuntimeException {
    public MigrationConflictException(int id) {
        super("Migration state has been modified: " + id + ".");
    }
}
