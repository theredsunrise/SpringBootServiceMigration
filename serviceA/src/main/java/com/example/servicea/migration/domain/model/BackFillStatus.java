package com.example.servicea.migration.domain.model;

import java.time.Instant;
import java.util.UUID;

public record BackFillStatus(
        String instanceId,
        int batchSize,
        Instant startedAt,
        long durationMs,
        UUID fromId,
        UUID toId,
        String status
) {
    public final static String COMPLETED = "COMPLETED";
    public final static String PROCESSED = "PROCESSED";

    public static BackFillStatus fromFailure(int batchSize, String instanceId, Instant startedAt, Exception ex) {
        return new BackFillStatus(
                instanceId,
                batchSize,
                startedAt,
                0,
                null,
                null,
                ex.getMessage()
        );
    }
}
