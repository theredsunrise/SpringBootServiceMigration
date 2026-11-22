package com.example.servicea.migration.infrastructure.adapter.out.persistence.repository;

import com.example.servicea.migration.application.port.out.UserValidationPersistencePort;
import com.example.servicea.migration.infrastructure.adapter.out.persistence.entity.UserJpaEntityPair;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserValidationPersistenceAdapter implements UserValidationPersistencePort {
    private final SpringDataUserValidationRepository repository;

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public List<UserJpaEntityPair> findFirstBatch(int batchSize) {
        return repository.findFirstBatch(Limit.of(batchSize));
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public List<UserJpaEntityPair> findNextBatch(int batchSize, UUID lastId) {
        return repository.findNextBatch(lastId, Limit.of(batchSize));
    }
}
