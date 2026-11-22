package com.example.servicea.migration.domain;

import com.example.servicea.migration.domain.entity.FeatureFlag;
import com.example.servicea.migration.domain.entity.MigrationState;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class FeatureFlagTest {
    @Test
    void changeStateKeepsSingletonIdentityAndRefreshesTimestamp() {
        Instant createdAt = Instant.now().minusSeconds(10);
        Instant updatedAt = Instant.now().minusSeconds(5);
        FeatureFlag current = new FeatureFlag(MigrationState.StartV2, createdAt, updatedAt);

        FeatureFlag changed = current.changeState(MigrationState.BackFillV2);

        assertEquals(1, changed.id());
        assertEquals(MigrationState.BackFillV2, changed.state());
        assertEquals(createdAt, changed.getCreatedAt());
        assertTrue(changed.updatedAt().isAfter(updatedAt));
        assertEquals(current, changed);
        assertEquals(current.hashCode(), changed.hashCode());
    }

    @Test
    void rejectsInvalidDomainValues() {
        Instant now = Instant.now();

        assertThrows(IllegalArgumentException.class,
                () -> new FeatureFlag(null, now, now));
        assertThrows(IllegalArgumentException.class,
                () -> new FeatureFlag(MigrationState.StartV2, now, null));
        assertEquals(1, new FeatureFlag(MigrationState.StartV2, now, now).id());
        assertEquals(new FeatureFlag(MigrationState.StartV2, now, now),
                new FeatureFlag(MigrationState.ActivatedV2, now, now.plusSeconds(1)));
    }
}
