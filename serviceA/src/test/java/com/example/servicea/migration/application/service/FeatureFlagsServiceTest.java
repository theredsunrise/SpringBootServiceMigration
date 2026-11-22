package com.example.servicea.migration.application.service;

import com.example.servicea.migration.application.exception.MigrationNotFoundException;
import com.example.servicea.migration.application.port.out.FeatureFlagPersistencePort;
import com.example.servicea.migration.domain.entity.FeatureFlag;
import com.example.servicea.migration.domain.entity.MigrationState;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FeatureFlagsServiceTest {
    private static FeatureFlagsService service(FeatureFlagPersistencePort persistence) {
        return new FeatureFlagsService(persistence);
    }

    @Test
    void readsSingletonAndPersistsSelectedState() {
        Instant createdAt = Instant.now().minusSeconds(10);
        TestMigrationPersistence persistence = new TestMigrationPersistence(
                new FeatureFlag(MigrationState.BackFillV2, createdAt, Instant.now()));
        FeatureFlagsService service = service(persistence);

        assertEquals(MigrationState.BackFillV2, service.getCurrent().state());
        FeatureFlag changed = service.setState(MigrationState.ActivatedV2AndSync);

        assertEquals(1, changed.id());
        assertEquals(MigrationState.ActivatedV2AndSync, persistence.current.state());
        assertEquals(createdAt, changed.getCreatedAt());
        assertEquals(createdAt, persistence.current.getCreatedAt());
        assertEquals(changed.updatedAt(), persistence.current.updatedAt());
    }

    @Test
    void missingSingletonIsNotRecreated() {
        FeatureFlagsService service = service(new TestMigrationPersistence(null));

        assertThrows(MigrationNotFoundException.class, service::getCurrent);
        assertThrows(MigrationNotFoundException.class,
                () -> service.setState(MigrationState.ActivatedV2));
    }

    private static class TestMigrationPersistence implements FeatureFlagPersistencePort {
        private FeatureFlag current;

        private TestMigrationPersistence(FeatureFlag current) {
            this.current = current;
        }

        @Override
        public Optional<FeatureFlag> findSingleton() {
            return Optional.ofNullable(current);
        }

        @Override
        public FeatureFlag save(FeatureFlag migration) {
            current = migration;
            return current;
        }
    }
}
