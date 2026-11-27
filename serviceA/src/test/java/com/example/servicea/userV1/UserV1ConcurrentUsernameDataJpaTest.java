package com.example.servicea.userV1;

import com.example.servicea.shared.exception.UserV1ConflictException;
import com.example.servicea.shared.util.UUIDv7Generator;
import com.example.servicea.support.PostgresTestConfiguration;
import com.example.servicea.userV1.application.command.UserV1WriteCommand;
import com.example.servicea.userV1.domain.UserV1;
import com.example.servicea.userV1.infrastructure.adapter.out.persistence.mapper.UserV1PersistenceMapperImpl;
import com.example.servicea.userV1.infrastructure.adapter.out.persistence.repository.UserV1PersistenceAdapter;
import com.example.servicea.userV1.infrastructure.service.UserV1ServiceFactory;
import com.example.servicea.userV1.infrastructure.service.UserV1SpringBootService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        PostgresTestConfiguration.class,
        UserV1PersistenceAdapter.class,
        UserV1PersistenceMapperImpl.class,
        UserV1SpringBootService.class,
        UserV1ServiceFactory.class,
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class UserV1ConcurrentUsernameDataJpaTest {
    private static final int RACE_ATTEMPTS = 20;

    @Autowired
    UserV1PersistenceAdapter persistence;
    @Autowired
    UserV1SpringBootService users;

    @Test
    void concurrentUpdatesToSameUserAllowOnlyOneVersion() throws Exception {
        String username = "race-" + uniqueSuffix();
        UserV1 existing = users.create(new UserV1WriteCommand(username, "Initial"));

        try {
            boolean sawOptimisticLockingFailure = false;
            try (var executor = Executors.newFixedThreadPool(2)) {
                for (int attempt = 0; attempt < RACE_ATTEMPTS; attempt++) {
                    CountDownLatch ready = new CountDownLatch(2);
                    CountDownLatch start = new CountDownLatch(1);
                    var first = CompletableFuture.supplyAsync(
                            () -> update(existing.id(), username, "First", ready, start), executor);
                    var second = CompletableFuture.supplyAsync(
                            () -> update(existing.id(), username, "Second", ready, start), executor);

                    boolean bothReady = ready.await(5, TimeUnit.SECONDS);
                    assertThat(bothReady).isTrue();
                    start.countDown();

                    CompletableFuture.allOf(first, second).get(20, TimeUnit.SECONDS);
                    List<Throwable> failures = Stream.of(first, second)
                            .map(CompletableFuture::join)
                            .filter(Objects::nonNull)
                            .toList();
                    assertThat(failures).allMatch(this::isOptimisticLockingFailure);
                    sawOptimisticLockingFailure |= !failures.isEmpty();
                }
            }

            assertThat(sawOptimisticLockingFailure)
                    .as("at least one concurrent update should hit optimistic locking")
                    .isTrue();
            List<UserV1> persisted = persistence.findAll(PageRequest.of(0, 100)).items().stream()
                    .filter(user -> user.username().equals(username)).toList();
            assertThat(persisted).hasSize(1);
        } finally {
            persistence.findAll(PageRequest.of(0, 100)).items().stream()
                    .filter(user -> user.username().equals(username))
                    .forEach(persistence::delete);
        }
        assertThat(persistence.findAll(PageRequest.of(0, 100)).items()).isEmpty();
    }

    private Throwable update(UUID id, String username, String name, CountDownLatch ready, CountDownLatch start) {
        ready.countDown();
        await(start);
        try {
            users.update(id, new UserV1WriteCommand(username, name));
            return null;
        } catch (RuntimeException exception) {
            return exception;
        }
    }

    private void await(CountDownLatch latch) {
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for concurrent updates to start");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for concurrent updates", exception);
        }
    }

    private boolean isOptimisticLockingFailure(Throwable exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof UserV1ConflictException) {
                return true;
            }
        }
        return false;
    }

    private String uniqueSuffix() {
        return UUIDv7Generator.randomUUID().toString().replace("-", "").substring(0, 30);
    }
}
