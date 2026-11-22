package com.example.servicea.userV1.domain;

import com.example.servicea.shared.util.UUIDv7Generator;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@EqualsAndHashCode(of = "id")
public final class UserV1 {
    private final UUID id;
    private final String username;
    private final String name;
    private final Instant createdAt;
    private final Instant updatedAt;

    public UserV1(UUID id, String username, String name, Instant createdAt, Instant updatedAt) {
        if (id == null || username == null || username.length() > 40 || name == null
                || name.length() > 100 || createdAt == null || updatedAt == null) {
            throw new IllegalArgumentException("Invalid user fields.");
        }
        this.id = id;
        this.username = username.strip();
        this.name = name.strip();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UserV1 create(String username, String name) {
        Instant now = Instant.now();
        return new UserV1(UUIDv7Generator.randomUUID(), username, name, now, now);
    }

    public UUID id() {
        return id;
    }

    public String username() {
        return username;
    }

    public String name() {
        return name;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public UserV1 change(String username, String name) {
        return new UserV1(id, username, name, createdAt, Instant.now());
    }
}
