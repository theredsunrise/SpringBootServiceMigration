package com.example.servicea.userV1.infrastructure.adapter.out.persistence.adapter;

import com.example.servicea.shared.exception.UserV1ConflictException;
import com.example.servicea.shared.util.UUIDv7Generator;
import com.example.servicea.support.PostgresTestConfiguration;
import com.example.servicea.userV1.domain.UserV1;
import com.example.servicea.userV1.infrastructure.adapter.out.persistence.entity.UserV1JpaEntity;
import com.example.servicea.userV1.infrastructure.adapter.out.persistence.mapper.UserV1PersistenceMapper;
import com.example.servicea.userV1.infrastructure.adapter.out.persistence.mapper.UserV1PersistenceMapperImpl;
import com.example.servicea.userV1.infrastructure.adapter.out.persistence.repository.SpringDataUserV1Repository;
import com.example.servicea.userV1.infrastructure.adapter.out.persistence.repository.UserV1PersistenceAdapter;
import org.hibernate.StaleStateException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PostgresTestConfiguration.class, UserV1PersistenceAdapter.class, UserV1PersistenceMapperImpl.class})
class UserV1PersistenceAdapterTest {
    @Autowired
    UserV1PersistenceAdapter adapter;

    @Test
    void savesReadsListsAndDeletesPersistedUsers() {
        UserV1 user = user("adapter-" + uniqueSuffix(), "Alice");

        assertThat(adapter.save(user)).isEqualTo(user);
        assertThat(adapter.findById(user.id())).contains(user);
        assertThat(adapter.findAll(PageRequest.of(0, 100)).items()).contains(user);
        assertThat(adapter.existsByUsername(user.username())).isTrue();

        adapter.delete(user);

        assertThat(adapter.findById(user.id())).isEmpty();
        assertThat(adapter.existsByUsername(user.username())).isFalse();
    }

    @Test
    void postgresKeepsUsernamesCaseSensitive() {
        String username = "case-" + uniqueSuffix();
        UserV1 lowerCase = adapter.save(user(username, "Lower"));
        UserV1 upperCase = adapter.save(user(username.toUpperCase(), "Upper"));

        assertThat(adapter.findAll(PageRequest.of(0, 100)).items()).contains(lowerCase, upperCase);
    }

    @Test
    void allowsUpdatingUserWithoutChangingItsUsername() {
        UserV1 existing = adapter.save(user("same-username-" + uniqueSuffix().substring(0, 20), "Before"));
        UserV1 updated = existing.change(existing.username(), "After");

        assertThat(adapter.save(updated)).isEqualTo(updated);
        assertThat(adapter.findById(existing.id())).contains(updated);
    }

    @Test
    void translatesOptimisticLockFailureWithStaleStateCauseToUserConflict() {
        UserV1 user = user("stale-" + uniqueSuffix().substring(0, 20), "Alice");
        SpringDataUserV1Repository repository = mock(SpringDataUserV1Repository.class);
        UserV1PersistenceMapper mapper = mock(UserV1PersistenceMapper.class);
        UserV1JpaEntity entity = new UserV1JpaEntity();
        entity.setId(user.id());
        entity.setUsername(user.username());
        OptimisticLockingFailureException staleFailure = new OptimisticLockingFailureException(
                "optimistic lock failed", new StaleStateException("stale row"));

        when(repository.findById(user.id())).thenReturn(Optional.empty());
        when(mapper.toEntity(user)).thenReturn(entity);
        when(repository.saveAndFlush(entity)).thenThrow(staleFailure);

        Throwable thrown = catchThrowable(() -> new UserV1PersistenceAdapter(repository, mapper).save(user));

        assertThat(thrown).isInstanceOf(UserV1ConflictException.class);
    }

    @Test
    void leavesOptimisticLockFailuresWithoutStaleCauseUnchanged() {
        UserV1 user = user("lock-" + uniqueSuffix().substring(0, 20), "Alice");
        SpringDataUserV1Repository repository = mock(SpringDataUserV1Repository.class);
        UserV1PersistenceMapper mapper = mock(UserV1PersistenceMapper.class);
        UserV1JpaEntity entity = new UserV1JpaEntity();
        entity.setId(user.id());
        entity.setUsername(user.username());
        OptimisticLockingFailureException optimisticFailure = new OptimisticLockingFailureException(
                "version changed");

        when(mapper.toEntity(user)).thenReturn(entity);
        when(repository.saveAndFlush(entity)).thenThrow(optimisticFailure);

        Throwable thrown = catchThrowable(() -> new UserV1PersistenceAdapter(repository, mapper).save(user));

        assertThat(thrown).isSameAs(optimisticFailure);
    }

    private UserV1 user(String username, String name) {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        return new UserV1(UUIDv7Generator.randomUUID(), username, name, now, now);
    }

    private String uniqueSuffix() {
        return UUIDv7Generator.randomUUID().toString().replace("-", "").substring(0, 30);
    }
}
