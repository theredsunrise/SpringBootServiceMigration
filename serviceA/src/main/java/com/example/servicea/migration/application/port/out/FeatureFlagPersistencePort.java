package com.example.servicea.migration.application.port.out;

import com.example.servicea.migration.domain.entity.FeatureFlag;

import java.util.Optional;

public interface FeatureFlagPersistencePort {
    Optional<FeatureFlag> findSingleton();

    FeatureFlag save(FeatureFlag featureFlag);
}
