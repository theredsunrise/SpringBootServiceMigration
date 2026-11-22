package com.example.servicea.migration.infrastructure.adapter.out.persistence.repository;

import com.example.servicea.migration.infrastructure.adapter.out.persistence.entity.UserJpaEntityPair;
import com.example.servicea.userV1.infrastructure.adapter.out.persistence.entity.UserV1JpaEntity;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SpringDataUserValidationRepository extends Repository<UserV1JpaEntity, UUID> {

    @Query("""
            SELECT v1 AS userV1, v2 AS userV2
            FROM UserV1 v1
            LEFT JOIN UserV2 v2 ON v1.id = v2.id
            ORDER BY v1.createdAt ASC
            """)
    List<UserJpaEntityPair> findFirstBatch(Limit limit);

    @Query("""
            SELECT v1 AS userV1, v2 AS userV2
            FROM UserV1 v1
            LEFT JOIN UserV2 v2 ON v1.id = v2.id
            WHERE v1.id > :lastId
            ORDER BY v1.id ASC
            """)
    List<UserJpaEntityPair> findNextBatch(@Param("lastId") UUID lastId, Limit limit);
}
