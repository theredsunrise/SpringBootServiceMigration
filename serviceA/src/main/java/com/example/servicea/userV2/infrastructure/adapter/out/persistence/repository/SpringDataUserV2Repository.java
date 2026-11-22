package com.example.servicea.userV2.infrastructure.adapter.out.persistence.repository;

import com.example.servicea.userV2.infrastructure.adapter.out.persistence.entity.UserV2JpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataUserV2Repository extends JpaRepository<UserV2JpaEntity, UUID> {
    boolean existsByUsername(String username);
}
