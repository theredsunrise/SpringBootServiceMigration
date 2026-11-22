package com.example.servicea.migration.infrastructure.adapter.out.persistence.repository;

import com.example.servicea.migration.application.exception.MigrationConflictException;
import com.example.servicea.migration.application.port.out.FeatureFlagPersistencePort;
import com.example.servicea.migration.domain.entity.FeatureFlag;
import com.example.servicea.migration.infrastructure.adapter.out.persistence.mapper.FeatureFlagPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.hibernate.StaleStateException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FeatureFlagPersistenceAdapter implements FeatureFlagPersistencePort {
    private final SpringDataFeatureFlagRepository repository;
    private final FeatureFlagPersistenceMapper mapper;

    @Override
    public Optional<FeatureFlag> findSingleton() {
        return repository.findById(FeatureFlag.SINGLETON_ID).map(mapper::toDomain);
    }

    @Override
    public FeatureFlag save(FeatureFlag featureFlag) {
        try {
            var entity = repository.findById(featureFlag.id()).map(current -> {
                mapper.updateEntity(featureFlag, current);
                return current;
            }).orElseGet(() -> mapper.toEntity(featureFlag));

            return mapper.toDomain(repository.saveAndFlush(entity));
        } catch (OptimisticLockingFailureException exception) {
            if (hasStaleStateCause(exception)) {
                throw new MigrationConflictException(featureFlag.id());
            }
            throw exception;
        }
    }

    private boolean hasStaleStateCause(Throwable exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof StaleStateException) {
                return true;
            }
        }
        return false;
    }
}
