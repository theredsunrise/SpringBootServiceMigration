package com.example.servicea.migration.application.port.in;

import com.example.servicea.migration.domain.entity.FeatureFlag;
import com.example.servicea.migration.domain.entity.MigrationState;

public interface FeatureFlagsUseCases {
    FeatureFlag getCurrent();

    FeatureFlag setState(MigrationState state);

    MigrationState getState();
}
