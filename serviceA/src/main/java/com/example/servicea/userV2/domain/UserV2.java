package com.example.servicea.userV2.domain;

import com.example.servicea.shared.util.UUIDv7Generator;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@EqualsAndHashCode(of = "id")
public final class UserV2 {
    private final UUID id;
    private final String username;
    private final String name;
    private final String surname;
    private final Instant createdAt;
    private final Instant updatedAt;

    public UserV2(
            UUID id,
            String username,
            String name,
            String surname,
            Instant createdAt,
            Instant updatedAt) {
        if (id == null || username == null || username.length() > 40
                || name == null || name.length() > 100
                || surname == null || surname.length() > 100
                || createdAt == null || updatedAt == null) {
            throw new IllegalArgumentException("Invalid V2 user fields.");
        }
        this.id = id;
        this.username = username.strip();
        this.name = name.strip();
        this.surname = surname.strip();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UserV2 create(String username, String name, String surname) {
        Instant now = Instant.now();
        return new UserV2(UUIDv7Generator.randomUUID(), username, name, surname, now, now);
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

    public String surname() {
        return surname;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public UserV2 change(String username, String name, String surname) {
        return new UserV2(id, username, name, surname, createdAt, Instant.now());
    }
}
