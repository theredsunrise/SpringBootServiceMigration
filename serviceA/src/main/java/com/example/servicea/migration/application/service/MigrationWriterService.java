package com.example.servicea.migration.application.service;

import com.example.servicea.migration.application.port.in.MigrationWriterUseCases;
import com.example.servicea.migration.application.port.out.MigrationStatusPersistencePort;
import com.example.servicea.migration.domain.model.BackFillStatus;
import com.example.servicea.migration.domain.model.ValidationStatus;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class MigrationWriterService implements MigrationWriterUseCases {
    private final MigrationStatusPersistencePort migrationStatusPersistence;

    @Override
    public List<BackFillStatus> getBackFillStatuses() {
        return migrationStatusPersistence.findAllBackFillStatuses();
    }

    @Override
    public void setBackFillStatus(BackFillStatus status) {
        migrationStatusPersistence.save(status.instanceId(), status);
    }

    @Override
    public List<ValidationStatus> getValidationStatuses() {
        return migrationStatusPersistence.findAllValidationStatuses();
    }

    @Override
    public void setValidationStatuses(List<ValidationStatus> statuses) {
        migrationStatusPersistence.saveAll(statuses);
    }

    @Override
    public void clearValidationStatuses() {
        migrationStatusPersistence.deleteAllValidationStatuses();
    }

    @Override
    public boolean lock(UUID id) {
        return migrationStatusPersistence.lock(id);
    }

    @Override
    public void unlock(UUID id) {
        migrationStatusPersistence.unlock(id);
    }
}
