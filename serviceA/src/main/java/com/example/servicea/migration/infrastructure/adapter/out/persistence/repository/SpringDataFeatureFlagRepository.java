package com.example.servicea.migration.infrastructure.adapter.out.persistence.repository;

import com.example.servicea.migration.infrastructure.adapter.out.persistence.entity.FeatureFlagJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataFeatureFlagRepository extends JpaRepository<FeatureFlagJpaEntity, Integer> {
}
