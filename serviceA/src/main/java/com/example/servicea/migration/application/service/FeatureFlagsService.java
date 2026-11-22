package com.example.servicea.migration.application.service;

import com.example.servicea.migration.application.exception.MigrationNotFoundException;
import com.example.servicea.migration.application.port.in.FeatureFlagsUseCases;
import com.example.servicea.migration.application.port.out.FeatureFlagPersistencePort;
import com.example.servicea.migration.domain.entity.FeatureFlag;
import com.example.servicea.migration.domain.entity.MigrationState;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FeatureFlagsService implements FeatureFlagsUseCases {
    private final FeatureFlagPersistencePort featureFlagPersistence;

    @Override
    public FeatureFlag getCurrent() {
        return featureFlagPersistence.findSingleton().orElseThrow(MigrationNotFoundException::new);
    }

    @Override
    public MigrationState getState() {
        return getCurrent().getState();
    }

    @Override
    public FeatureFlag setState(MigrationState state) {
        FeatureFlag current = getCurrent();
        return featureFlagPersistence.save(current.changeState(state));
    }
}
