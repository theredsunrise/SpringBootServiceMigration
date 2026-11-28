package com.example.servicea.migration.domain.entity;

import com.example.servicea.migration.application.exception.UnsupportedMigrationStepException;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Getter
public enum MigrationState {
    StartV2(0),
    BackFillV2(1),
    ActivatedV2AndSync(2),
    ActivatedV2(3),
    ActivatedV1(4);

    private static final Map<MigrationState, Function<MigrationState, Boolean>> migrationStates = Map.of(
            StartV2, MigrationState::shouldTransitionFromStartV2,
            BackFillV2, MigrationState::shouldTransitionFromBackFillV2,
            ActivatedV2AndSync, MigrationState::shouldTransitionFromActivatedV2AndSync,
            ActivatedV2, MigrationState::shouldTransitionFromActivatedV2,
            ActivatedV1, MigrationState::shouldTransitionFromActivatedV1
    );
    private final int order;

    MigrationState(int ordinal) {
        this.order = ordinal;
    }

    private static boolean shouldTransitionFromActivatedV1(MigrationState newState) {
        return switch (newState) {
            case ActivatedV1, StartV2 -> true;
            default -> false;
        };
    }

    private static boolean shouldTransitionFromStartV2(MigrationState newState) {
        return switch (newState) {
            case StartV2, BackFillV2, ActivatedV1 -> true;
            default -> false;
        };
    }

    private static boolean shouldTransitionFromBackFillV2(MigrationState newState) {
        return switch (newState) {
            case BackFillV2, ActivatedV2AndSync, ActivatedV1 -> true;
            default -> false;
        };
    }

    private static boolean shouldTransitionFromActivatedV2AndSync(MigrationState newState) {
        return switch (newState) {
            case ActivatedV2AndSync, ActivatedV2, ActivatedV1 -> true;
            default -> false;
        };
    }

    private static boolean shouldTransitionFromActivatedV2(MigrationState newState) {
        return switch (newState) {
            case ActivatedV2, ActivatedV1 -> true;
            default -> false;
        };
    }

    public boolean useV1() {
        return this.ordinal() < ActivatedV2AndSync.ordinal() || this == ActivatedV1;
    }

    public boolean syncV1() {
        return this == ActivatedV2AndSync;
    }

    public boolean useV2() {
        return List.of(ActivatedV2AndSync, ActivatedV2).contains(this);
    }

    public boolean syncV2() {
        return List.of(StartV2, BackFillV2).contains(this);
    }

    public boolean backfillV2() {
        return this == BackFillV2;
    }

    public void assertValidateV2() {
        if (!List.of(BackFillV2, ActivatedV2AndSync, ActivatedV2).contains(this)) {
            throw new UnsupportedMigrationStepException(
                    "Validation is forbidden in the %s state.".formatted(this));
        }
    }

    public void assertAllowed(MigrationState newState) {
        if (!isAllowed(newState)) {
            throw new UnsupportedMigrationStepException(
                    "Transition from the state %s to %s is forbidden.".formatted(this, newState)
            );
        }
    }

    private boolean isAllowed(MigrationState newState) {
        if (newState == null) throw new NullPointerException("New state is null.");
        return migrationStates.get(this).apply(newState);
    }

}
