package com.example.servicea.migration.infrastructure.service;

import com.example.servicea.migration.application.port.in.FeatureFlagsUseCases;
import com.example.servicea.migration.application.port.in.ValidationJobLauncher;
import com.example.servicea.migration.application.service.MigrationProcessService;
import com.example.servicea.migration.domain.mapper.UserV1ToUserV2Mapper;
import com.example.servicea.userV1.application.port.out.UserV1PersistencePort;
import com.example.servicea.userV2.application.port.out.UserV2PersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MigrationProcessServiceFactory {
    private final UserV1PersistencePort userV1Persistence;
    private final UserV2PersistencePort userV2Persistence;
    private final UserV1ToUserV2Mapper v1toV2mapper;
    private final ValidationJobLauncher validationJobLauncher;
    private final FeatureFlagsUseCases featureFlags;

    public MigrationProcessService create() {
        return new MigrationProcessService(
                userV1Persistence,
                userV2Persistence,
                v1toV2mapper,
                validationJobLauncher,
                featureFlags);
    }
}
