package com.example.servicea.migration.infrastructure.service.validation;

import com.example.servicea.migration.application.port.out.MigrationStatusPersistencePort;
import com.example.servicea.migration.application.service.MigrationWriterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MigrationWriterServiceFactory {
    private final MigrationStatusPersistencePort persistence;

    public MigrationWriterService create() {
        return new MigrationWriterService(persistence);
    }
}
