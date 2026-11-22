package com.example.servicea.migration.application.service;

import com.example.servicea.migration.application.port.in.FeatureFlagsUseCases;
import com.example.servicea.migration.application.port.in.MigrationProcessUseCases;
import com.example.servicea.migration.application.port.in.ValidationJobLauncher;
import com.example.servicea.migration.domain.mapper.UserV1ToUserV2Mapper;
import com.example.servicea.migration.domain.model.BackFillStatus;
import com.example.servicea.migration.domain.model.ValidationJobStatus;
import com.example.servicea.userV1.application.port.out.UserV1PersistencePort;
import com.example.servicea.userV1.domain.UserV1;
import com.example.servicea.userV2.application.port.out.UserV2PersistencePort;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
public class MigrationProcessService implements MigrationProcessUseCases {
    private final UserV1PersistencePort userRepository;
    private final UserV2PersistencePort userV2Repository;
    private final UserV1ToUserV2Mapper userMigrationMapper;
    private final ValidationJobLauncher validationJobLauncher;
    private final FeatureFlagsUseCases featureFlag;

    @Override
    public Optional<BackFillStatus> execute(int batchSize, Instant startedAt, String instanceId) {
        var users = userRepository.findFirstNotMigratedUsers(batchSize);
        var fromId = users.stream().findFirst().map(UserV1::getId).orElse(null);
        AtomicReference<UUID> toId = new AtomicReference<>(fromId);

        var usersV2 = users.stream().map(user -> {
            toId.set(user.id());
            return userMigrationMapper.from(user);
        }).toList();
        userV2Repository.saveAll(usersV2);

        return Optional.of(new BackFillStatus(
                instanceId,
                batchSize,
                startedAt,
                Math.max(Duration.between(startedAt, Instant.now()).toMillis(), 0),
                fromId,
                toId.get(),
                users.isEmpty() ? BackFillStatus.COMPLETED : BackFillStatus.PROCESSED
        ));
    }

    @Override
    public ValidationJobStatus startValidationJob() {
        featureFlag.getState().assertValidateV2();
        return validationJobLauncher.start();
    }
}
