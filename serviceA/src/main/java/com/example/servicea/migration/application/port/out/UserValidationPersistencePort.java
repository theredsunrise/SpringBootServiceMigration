package com.example.servicea.migration.application.port.out;

import com.example.servicea.migration.infrastructure.adapter.out.persistence.entity.UserJpaEntityPair;

import java.util.List;
import java.util.UUID;

public interface UserValidationPersistencePort {
    List<UserJpaEntityPair> findFirstBatch(int batchSize);

    List<UserJpaEntityPair> findNextBatch(int batchSize, UUID lastId);
}
