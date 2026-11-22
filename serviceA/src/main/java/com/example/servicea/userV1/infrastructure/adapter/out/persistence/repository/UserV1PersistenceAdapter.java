package com.example.servicea.userV1.infrastructure.adapter.out.persistence.repository;

import com.example.servicea.shared.exception.UserV1ConflictException;
import com.example.servicea.shared.exception.UsernameConflictException;
import com.example.servicea.userV1.application.port.out.UserV1PersistencePort;
import com.example.servicea.userV1.domain.UserV1;
import com.example.servicea.userV1.infrastructure.adapter.out.persistence.mapper.UserV1PersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.hibernate.StaleStateException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserV1PersistenceAdapter implements UserV1PersistencePort {
    private final SpringDataUserV1Repository repository;
    private final UserV1PersistenceMapper mapper;

    @Override
    public List<UserV1> findFirstNotMigratedUsers(int limit) {
        if (limit <= 10) throw new IllegalArgumentException("limit must be greater than 10.");
        return repository.findNotMigratedUsers(limit).stream().map(mapper::toDomain).toList();
    }

    @Override
    public UserV1 create(UserV1 user) {
        try {
            var entity = mapper.toEntity(user);
            return mapper.toDomain(repository.saveAndFlush(entity));
        } catch (DataIntegrityViolationException ex) {
            throw new UsernameConflictException(user.username());
        }
    }

    @Override
    public UserV1 save(UserV1 user) {
        try {
            var entity = repository.findById(user.id()).map(current -> {
                mapper.updateEntity(user, current);
                return current;
            }).orElseGet(() -> mapper.toEntity(user));

            return mapper.toDomain(repository.saveAndFlush(entity));
        } catch (OptimisticLockingFailureException ex) {
            if (hasStaleStateCause(ex)) {
                throw new UserV1ConflictException(user.id());
            }
            throw ex;
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

    @Override
    public Optional<UserV1> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<UserV1> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public void delete(UserV1 user) {
        try {
            repository.deleteById(user.id());
            repository.flush();
        } catch (OptimisticLockingFailureException ex) {
            if (hasStaleStateCause(ex)) {
                throw new UserV1ConflictException(user.id());
            }
            throw ex;
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        return repository.existsByUsername(username);
    }
}
