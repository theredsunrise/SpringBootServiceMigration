package com.example.servicea.migration.domain.model;

import java.time.LocalDateTime;

public record ValidationJobStatus(
        Long executionId,
        Long instanceId,
        String jobName,
        String status,          // STARTING, STARTED, STOPPING, STOPPED, COMPLETED, FAILED, ABANDONED
        String exitCode,        // EXECUTING, COMPLETED, FAILED, STOPPED, ...
        String exitDescription,
        LocalDateTime createTime,
        LocalDateTime startTime,
        LocalDateTime endTime,
        LocalDateTime lastUpdated,
        long readCount,
        long filterCount,
        long writeCount,
        long commitCount,
        long rollbackCount,
        long skipCount
) {

}