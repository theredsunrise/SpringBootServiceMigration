package com.example.servicea.userV2.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity(name = "UserV2")
@Table(name = "users_v2")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class UserV2JpaEntity {
    @Id
    @NonNull
    private UUID id;

    private String username;
    private String name;
    private String surname;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
    @Version
    private Long version;
}
