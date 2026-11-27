package com.example.servicea.migration.infrastructure.service.validation;

import com.example.servicea.migration.application.port.out.FeatureFlagPersistencePort;
import com.example.servicea.migration.application.service.FeatureFlagsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeatureFlagsServiceFactory {
    private final FeatureFlagPersistencePort persistence;

    public FeatureFlagsService create() {
        return new FeatureFlagsService(persistence);
    }
}
