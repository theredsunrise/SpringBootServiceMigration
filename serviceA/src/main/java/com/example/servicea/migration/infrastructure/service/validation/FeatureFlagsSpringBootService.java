package com.example.servicea.migration.infrastructure.service.validation;

import com.example.servicea.migration.application.port.in.FeatureFlagsUseCases;

import com.example.servicea.migration.domain.entity.FeatureFlag;
import com.example.servicea.migration.domain.entity.MigrationState;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeatureFlagsSpringBootService implements FeatureFlagsUseCases {
    private static final String CACHE_NAME = "migration";
    private final FeatureFlagsUseCases service;

    public FeatureFlagsSpringBootService(FeatureFlagsServiceFactory factory) {
        service = factory.create();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CACHE_NAME, key = "'current'")
    public FeatureFlag getCurrent() {
        return service.getCurrent();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CACHE_NAME, key = "'state'")
    public MigrationState getState() {
        return service.getState();
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CACHE_NAME, allEntries = true)
    public FeatureFlag setState(MigrationState state) {
        return service.setState(state);
    }
}
