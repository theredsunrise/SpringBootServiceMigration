package com.example.servicea.userV1.infrastructure.adapter.out.persistence.mapper;

import com.example.servicea.shared.util.UUIDv7Generator;

import com.example.servicea.userV1.domain.UserV1;
import com.example.servicea.userV1.infrastructure.adapter.out.persistence.entity.UserV1JpaEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserV1PersistenceMapperTest {
    private final UserV1PersistenceMapper mapper = new UserV1PersistenceMapperImpl();

    @Test
    void mapsEveryFieldInBothDirections() {
        UserV1 source = new UserV1(UUIDv7Generator.randomUUID(), "alice", "Alice", Instant.now(), Instant.now());
        UserV1JpaEntity entity = mapper.toEntity(source);
        assertEquals(source.id(), entity.getId());
        assertEquals(source.username(), entity.getUsername());
        assertEquals(source.name(), entity.getName());
        assertEquals(source.createdAt(), entity.getCreatedAt());
        assertEquals(source.updatedAt(), entity.getUpdatedAt());
        assertEquals(source, mapper.toDomain(entity));
    }

    @Test
    void entityEqualityUsesOnlyId() {
        UUID id = UUIDv7Generator.randomUUID();
        UserV1JpaEntity first = new UserV1JpaEntity();
        first.setId(id);
        first.setUsername("alice");
        UserV1JpaEntity second = new UserV1JpaEntity();
        second.setId(id);
        second.setUsername("other");
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
}
