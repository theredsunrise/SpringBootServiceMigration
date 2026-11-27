package com.example.servicea.migration.application.port.in;

import com.example.servicea.migration.domain.model.ValidationJobAttributes;

import java.util.Optional;

public interface ValidationJobLauncher {
    ValidationJobAttributes start();
    Optional<ValidationJobAttributes> getStatus();
}
