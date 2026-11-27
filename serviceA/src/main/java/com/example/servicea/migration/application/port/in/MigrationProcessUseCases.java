package com.example.servicea.migration.application.port.in;

import com.example.servicea.migration.domain.model.BackFillStatus;
import com.example.servicea.migration.domain.model.ValidationJobAttributes;

import java.time.Instant;
import java.util.Optional;

public interface MigrationProcessUseCases {
    Optional<BackFillStatus> execute(int batchSize, Instant startedAt, String instanceId);

    ValidationJobAttributes startValidationJob();

    Optional<ValidationJobAttributes> getValidationJobStatus();
}
