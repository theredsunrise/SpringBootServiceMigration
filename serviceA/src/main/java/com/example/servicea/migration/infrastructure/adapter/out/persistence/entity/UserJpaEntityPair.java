package com.example.servicea.migration.infrastructure.adapter.out.persistence.entity;

import com.example.servicea.userV1.infrastructure.adapter.out.persistence.entity.UserV1JpaEntity;
import com.example.servicea.userV2.infrastructure.adapter.out.persistence.entity.UserV2JpaEntity;

public record UserJpaEntityPair(UserV1JpaEntity userV1, UserV2JpaEntity userV2) {
}
