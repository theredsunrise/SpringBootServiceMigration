package com.example.servicea.userV1.domain;

import com.example.servicea.shared.util.UUIDv7Generator;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserV1Test {
    @Test
    void createGeneratesIdentityAndUtcInstants() {
        UserV1 user = UserV1.create("alice", "Alice");
        assertEquals(36, user.id().toString().length());
        assertEquals(user.createdAt(), user.updatedAt());
        assertEquals(Instant.class, user.createdAt().getClass());
    }

    @Test
    void fieldsAreBoundedAndEqualityUsesOnlyIdentity() {
        UUID id = UUIDv7Generator.randomUUID();
        Instant now = Instant.now();
        UserV1 first = new UserV1(id, "alice", "Alice", now, now);
        UserV1 renamed = new UserV1(id, "alice2", "Alicia", now, now.plusSeconds(1));
        assertEquals(first, renamed);
        assertEquals(first.hashCode(), renamed.hashCode());
        assertNotEquals(first, UserV1.create("alice", "Alice"));
        assertThrows(IllegalArgumentException.class,
                () -> new UserV1(id, "x".repeat(41), "Alice", now, now));
        assertThrows(IllegalArgumentException.class,
                () -> new UserV1(id, "alice", "x".repeat(101), now, now));
    }
}
