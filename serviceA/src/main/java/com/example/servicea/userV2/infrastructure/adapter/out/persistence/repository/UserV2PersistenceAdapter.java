package com.example.servicea.userV2.infrastructure.adapter.out.persistence.repository;

import com.example.servicea.shared.exception.UserV2ConflictException;
import com.example.servicea.shared.exception.UsernameConflictException;
import com.example.servicea.shared.infrastructure.adapter.in.web.model.PageWrapper;
import com.example.servicea.userV2.application.port.out.UserV2PersistencePort;
import com.example.servicea.userV2.domain.UserV2;
import com.example.servicea.userV2.infrastructure.adapter.out.persistence.mapper.UserV2PersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.hibernate.StaleStateException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserV2PersistenceAdapter implements UserV2PersistencePort {
    private final SpringDataUserV2Repository repository;
    private final UserV2PersistenceMapper mapper;

    @Override
    public UserV2 create(UserV2 user) {
        try {
            var entity = mapper.toEntity(user);
            return mapper.toDomain(repository.saveAndFlush(entity));
        } catch (DataIntegrityViolationException ex) {
            throw new UsernameConflictException(user.username());
        }
    }

    @Override
    public UserV2 save(UserV2 user) {
        try {
            var entity = repository.findById(user.id()).map(current -> {
                mapper.updateEntity(user, current);
                return current;
            }).orElseGet(() -> mapper.toEntity(user));

            return mapper.toDomain(repository.saveAndFlush(entity));
        } catch (OptimisticLockingFailureException exception) {
            if (hasStaleStateCause(exception)) {
                throw new UserV2ConflictException(user.id());
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

    @Override
    public void saveAll(List<UserV2> users) {
        var entities = users.stream().map(mapper::toEntity).toList();
        repository.saveAllAndFlush(entities);
    }

    @Override
    public Optional<UserV2> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public PageWrapper<UserV2> findAll(Pageable pageable) {
        return mapper.toDomain(repository.findAll(pageable));
    }

    @Override
    public void delete(UserV2 user) {
        try {
            repository.deleteById(user.id());
            repository.flush();
        } catch (OptimisticLockingFailureException exception) {
            if (hasStaleStateCause(exception)) {
                throw new UserV2ConflictException(user.id());
            }
            throw exception;
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        return repository.existsByUsername(username);
    }
}
