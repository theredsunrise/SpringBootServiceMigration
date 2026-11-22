package com.example.servicea.migration.application.port.in;

import com.example.servicea.migration.domain.model.BackFillStatus;
import com.example.servicea.migration.domain.model.ValidationStatus;
import java.util.List;
import java.util.UUID;

public interface MigrationWriterUseCases {
    List<BackFillStatus> getBackFillStatuses();

    void setBackFillStatus(BackFillStatus status);

    List<ValidationStatus> getValidationStatuses();

    void setValidationStatuses(List<ValidationStatus> statuses);

    void clearValidationStatuses();

    boolean lock(UUID id);

    void unlock(UUID id);
}
