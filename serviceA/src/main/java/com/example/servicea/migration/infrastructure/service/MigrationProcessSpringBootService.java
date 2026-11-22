package com.example.servicea.migration.infrastructure.service;

import com.example.servicea.migration.application.port.in.MigrationProcessUseCases;
import com.example.servicea.migration.domain.model.BackFillStatus;
import com.example.servicea.migration.domain.model.ValidationJobStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class MigrationProcessSpringBootService implements MigrationProcessUseCases {
    private final MigrationProcessUseCases service;

    public MigrationProcessSpringBootService(MigrationProcessServiceFactory factory) {
        service = factory.create();
    }

    @Override
    @Transactional
    public Optional<BackFillStatus> execute(int batchSize, Instant startedAt, String instanceId) {
        return service.execute(batchSize, startedAt, instanceId);
    }

    @Override
    public ValidationJobStatus startValidationJob() {
        return service.startValidationJob();
    }
}
