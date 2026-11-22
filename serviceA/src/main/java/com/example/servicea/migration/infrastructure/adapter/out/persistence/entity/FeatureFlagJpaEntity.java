package com.example.servicea.migration.infrastructure.adapter.out.persistence.entity;

import com.example.servicea.migration.domain.entity.MigrationState;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity(name = "FeatureFlag")
@Table(name = "feature_flags")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class FeatureFlagJpaEntity {
    @Id
    @NonNull
    private Integer id;

    @Enumerated(EnumType.STRING)
    private MigrationState state;

    @Column(name = "created_at")
    private Instant createdAt;
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Version
    private Long version;
}
