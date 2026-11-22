package com.example.servicea.migration.application.port.out;

import com.example.servicea.migration.domain.model.BackFillStatus;
import com.example.servicea.migration.domain.model.ValidationStatus;

import java.util.List;
import java.util.UUID;

public interface MigrationStatusPersistencePort {
    List<ValidationStatus> findAllValidationStatuses();

    List<BackFillStatus> findAllBackFillStatuses();

    void save(String instanceId, BackFillStatus status);

    void saveAll(List<ValidationStatus> statuses);

    void deleteAllValidationStatuses();

    boolean lock(UUID id);

    void unlock(UUID id);
}
