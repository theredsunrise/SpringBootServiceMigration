package com.example.servicea.migration.infrastructure.adapter.in.scheduler;

import com.example.servicea.migration.application.port.in.FeatureFlagsUseCases;
import com.example.servicea.migration.application.port.in.MigrationProcessUseCases;
import com.example.servicea.migration.application.port.in.MigrationWriterUseCases;
import com.example.servicea.migration.domain.model.BackFillStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component

@RequiredArgsConstructor
public class MigrationScheduler {
    private static final int SCHEDULER_DELAY_MS = 1000;
    private static final int BATCH_SIZE = 1000;

    private final FeatureFlagsUseCases featureFlags;
    private final MigrationProcessUseCases process;
    private final MigrationWriterUseCases writer;

    @Value("${HOSTNAME:unknown}")
    private String instanceId;

    @Scheduled(fixedDelay = SCHEDULER_DELAY_MS)
    void backFill() {
        if (!featureFlags.getCurrent().getState().backfillV2()) {
            return;
        }
        var startedAt = Instant.now();
        try {
            var status = process.execute(
                    BATCH_SIZE,
                    startedAt,
                    instanceId
            );
            status.ifPresent(writer::setBackFillStatus);
        } catch (Exception ex) {
            log.error("Processing of the back fill batch on the instance: {} has failed.",
                    instanceId,
                    ex);
            try {
                writer.setBackFillStatus(
                        BackFillStatus.fromFailure(
                                BATCH_SIZE,
                                instanceId,
                                startedAt,
                                ex
                        )
                );
            } catch (Exception exception) {
                log.error("Sending of the back fill status on the instance: {} has failed.",
                        instanceId,
                        exception);
            }
        }
    }
}
