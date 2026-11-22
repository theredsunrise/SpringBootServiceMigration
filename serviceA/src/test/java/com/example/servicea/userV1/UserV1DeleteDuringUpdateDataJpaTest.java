package com.example.servicea.userV1;

import com.example.servicea.shared.exception.UserV1ConflictException;
import com.example.servicea.shared.util.UUIDv7Generator;
import com.example.servicea.support.PostgresTestConfiguration;
import com.example.servicea.userV1.application.command.UserV1PatchCommand;
import com.example.servicea.userV1.application.command.UserV1WriteCommand;
import com.example.servicea.userV1.application.port.in.UserV1UseCases;
import com.example.servicea.userV1.infrastructure.adapter.out.persistence.mapper.UserV1PersistenceMapperImpl;
import com.example.servicea.userV1.infrastructure.adapter.out.persistence.repository.SpringDataUserV1Repository;
import com.example.servicea.userV1.infrastructure.adapter.out.persistence.repository.UserV1PersistenceAdapter;
import com.example.servicea.userV1.infrastructure.service.UserV1ServiceFactory;
import com.example.servicea.userV1.infrastructure.service.UserV1SpringBootService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
class UserV1DeleteDuringUpdateDataJpaTest {
    private static final int ITERATIONS = 30;

    @Autowired
    UserV1UseCases users;
    @Autowired
    SpringDataUserV1Repository repository;

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for concurrent operation");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for concurrent operation", exception);
        }
    }

    private static void sleepHalfMillisecond() {
        try {
            TimeUnit.MICROSECONDS.sleep(500);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted before delete", exception);
        }
    }

    @Test
    void updateAndDeleteRacesRespectOptimisticLocking() throws Exception {
        boolean sawUserConflict = false;

        for (int i = 0; i < ITERATIONS; i++) {
            sawUserConflict |= runRace(i, false);
            sawUserConflict |= runRace(i, true);
        }

        assertThat(sawUserConflict)
                .as("at least one stale update is translated to UserV1ConflictException")
                .isTrue();
    }

    private boolean runRace(int iteration, boolean patch) throws Exception {
        String username = "race-" + UUIDv7Generator.randomUUID().toString().substring(0, 10);
        var created = users.create(new UserV1WriteCommand(username, "Initial"));
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        CompletableFuture<Exception> update = CompletableFuture.supplyAsync(() -> {
            ready.countDown();
            await(start);
            try {
                if (patch) {
                    users.patch(created.id(), new UserV1PatchCommand(null, "Updated"));
                } else {
                    users.update(created.id(), new UserV1WriteCommand(username, "Updated"));
                }
                return null;
            } catch (Exception exception) {
                return exception;
            }
        });

        CompletableFuture<Exception> delete = CompletableFuture.supplyAsync(() -> {
            ready.countDown();
            await(start);
            sleepHalfMillisecond();
            try {
                users.delete(created.id());
                return null;
            } catch (Exception exception) {
                return exception;
            }
        });

        boolean bothReady = ready.await(5, TimeUnit.SECONDS);
        start.countDown();
        assertThat(bothReady).as("both operations are ready in iteration %s", iteration).isTrue();

        Exception updateFailure = update.get(20, TimeUnit.SECONDS);
        Exception deleteFailure = delete.get(20, TimeUnit.SECONDS);

        try {
            if (deleteFailure == null) {
                assertThat(updateFailure == null || updateFailure instanceof UserV1ConflictException)
                        .as("update succeeds or hits a stale version in iteration %s (patch=%s)",
                                iteration, patch)
                        .isTrue();
                assertThat(repository.existsById(created.id()))
                        .as("user is deleted in iteration %s (patch=%s)", iteration, patch)
                        .isFalse();
                return updateFailure != null;
            }

            assertThat(deleteFailure).isInstanceOf(UserV1ConflictException.class);
            assertThat(updateFailure)
                    .as("update wins when delete detects a stale version in iteration %s (patch=%s)",
                            iteration, patch)
                    .isNull();
            assertThat(repository.existsById(created.id()))
                    .as("user remains after delete loses in iteration %s (patch=%s)", iteration, patch)
                    .isTrue();
            return false;
        } finally {
            if (repository.existsById(created.id())) {
                repository.deleteById(created.id());
            }
        }
    }

}
