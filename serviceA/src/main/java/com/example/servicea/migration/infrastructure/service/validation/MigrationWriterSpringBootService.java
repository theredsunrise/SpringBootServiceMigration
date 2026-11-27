package com.example.servicea.migration.infrastructure.service.validation;

import com.example.servicea.migration.application.port.in.MigrationWriterUseCases;
import com.example.servicea.migration.domain.model.BackFillStatus;
import com.example.servicea.migration.domain.model.ValidationStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MigrationWriterSpringBootService implements MigrationWriterUseCases {
    private final MigrationWriterUseCases service;

    public MigrationWriterSpringBootService(MigrationWriterServiceFactory factory) {
        service = factory.create();
    }

    @Override
    public List<BackFillStatus> getBackFillStatuses() {
        return service.getBackFillStatuses();
    }

    @Override
    public void setBackFillStatus(BackFillStatus status) {
        service.setBackFillStatus(status);
    }

    @Override
    public List<ValidationStatus> getValidationStatuses() {
        return service.getValidationStatuses();
    }

    @Override
    public void setValidationStatuses(List<ValidationStatus> statuses) {
        service.setValidationStatuses(statuses);
    }

    @Override
    public void clearValidationStatuses() {
        service.clearValidationStatuses();
    }

    @Override
    public boolean lock(UUID id) {
        return service.lock(id);
    }

    @Override
    public void unlock(UUID id) {
        service.unlock(id);
    }
}
