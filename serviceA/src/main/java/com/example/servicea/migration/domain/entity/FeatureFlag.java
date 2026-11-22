package com.example.servicea.migration.domain.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;

@Getter
@EqualsAndHashCode(of = "id")
public final class FeatureFlag implements Serializable {
    public static final int SINGLETON_ID = 1;
    private static final long serialVersionUID = 1L;
    private final int id = SINGLETON_ID;
    private final MigrationState state;
    private final Instant updatedAt;
    private final Instant createdAt;

    public FeatureFlag(MigrationState state, Instant createdAt, Instant updatedAt) {
        if (state == null || updatedAt == null) {
            throw new IllegalArgumentException("Invalid feature flag fields.");
        }
        this.state = state;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int id() {
        return id;
    }

    public MigrationState state() {
        return state;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public FeatureFlag changeState(MigrationState newState) {
        state.assertAllowed(newState);
        return new FeatureFlag(newState, createdAt, Instant.now());
    }
}
