package com.example.servicea.migration.domain;

import com.example.servicea.migration.application.exception.UnsupportedMigrationStepException;
import com.example.servicea.migration.domain.entity.MigrationState;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;

class MigrationStateTest {
    private static final Map<MigrationState, Set<MigrationState>> ALLOWED_TRANSITIONS = Map.of(
            MigrationState.StartV2, Set.of(MigrationState.StartV2, MigrationState.BackFillV2),
            MigrationState.BackFillV2, Set.of(MigrationState.BackFillV2, MigrationState.ActivatedV2AndSync),
            MigrationState.ActivatedV2AndSync, Set.of(MigrationState.ActivatedV2AndSync, MigrationState.ActivatedV2),
            MigrationState.ActivatedV2, Set.of(MigrationState.ActivatedV2, MigrationState.ActivatedV1),
            MigrationState.ActivatedV1, Set.of(MigrationState.ActivatedV1, MigrationState.StartV2)
    );

    @Test
    void allowsOnlyConfiguredTransitions() {
        for (MigrationState current : MigrationState.values()) {
            for (MigrationState next : MigrationState.values()) {
                String transition = "Transition from %s to %s".formatted(current, next);
                if (ALLOWED_TRANSITIONS.get(current).contains(next)) {
                    current.assertAllowed(next);
                } else {
                    assertThrows(
                            UnsupportedMigrationStepException.class,
                            () -> current.assertAllowed(next),
                            transition
                    );
                }
            }
        }
    }
}
