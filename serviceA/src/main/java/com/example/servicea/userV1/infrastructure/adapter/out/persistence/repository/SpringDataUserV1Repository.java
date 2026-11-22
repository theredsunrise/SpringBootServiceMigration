package com.example.servicea.userV1.infrastructure.adapter.out.persistence.repository;

import com.example.servicea.userV1.infrastructure.adapter.out.persistence.entity.UserV1JpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SpringDataUserV1Repository extends JpaRepository<UserV1JpaEntity, UUID> {
    boolean existsByUsername(String username);

    @Query(value = """
            SELECT v1.* FROM users_v1 v1
            WHERE NOT EXISTS (
                SELECT 1 FROM users_v2 v2 WHERE v2.id = v1.id
            )
            ORDER BY v1.id
            LIMIT :limit
            FOR UPDATE OF v1 SKIP LOCKED
            """, nativeQuery = true)
    List<UserV1JpaEntity> findNotMigratedUsers(@Param("limit") int limit);
}
