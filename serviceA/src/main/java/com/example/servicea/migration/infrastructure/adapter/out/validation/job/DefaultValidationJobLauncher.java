package com.example.servicea.migration.infrastructure.adapter.out.validation.job;

import com.example.servicea.migration.application.exception.ValidationJobStatusUnknownException;
import com.example.servicea.migration.application.port.in.MigrationWriterUseCases;
import com.example.servicea.migration.application.port.in.ValidationJobLauncher;
import com.example.servicea.migration.domain.model.ValidationJobStatus;
import com.example.servicea.migration.infrastructure.adapter.out.validation.mapper.ValidationJobStatusMapper;
import com.example.servicea.shared.util.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultValidationJobLauncher implements ValidationJobLauncher {

    public final static String PROCESSOR_NAME = "validationProcessor";
    public final static String WRITER_NAME = "validationWriter";
    public final static String READER_NAME = "validationReader";
    public final static String STEP_NAME = "validationStep";
    public final static String JOB_NAME = "validationJob";
    public final static int BATCH_SIZE = 10;

    private final JobOperator jobOperator;
    private final JobRepository jobRepository;
    private final Job validationJob; //name must match the JOB_NAME

    private final ValidationJobStatusMapper validationJobStatusMapper;
    private final MigrationWriterUseCases writer;

    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public ValidationJobStatus start() {
        return Utils.sync(lock, () -> {
            UUID lockId = UUID.randomUUID();
            if (!writer.lock(lockId)) {
                log.info("**** Writer lock locked");
                return getLastJobExecution()
                        .map(validationJobStatusMapper::fromJobExecution)
                        .orElseThrow(ValidationJobStatusUnknownException::new);
            }

            try {
                var lastExecution = getLastJobExecution();
                if (lastExecution.filter(this::isRunning).isPresent()) {
                    log.info("**** Getting info from running job instance!");
                    return validationJobStatusMapper.fromJobExecution(lastExecution.get());
                }

                writer.clearValidationStatuses();
                log.info("**** Starting new job instance!");
                return validationJobStatusMapper.fromJobExecution(jobOperator.startNextInstance(validationJob));
            } finally {
                writer.unlock(lockId);
            }
        });
    }

    private boolean isRunning(JobExecution execution) {
        return switch (execution.getStatus()) {
            case STARTING, STARTED, STOPPING -> true;
            default -> false;
        };
    }

    private Optional<JobExecution> getLastJobExecution() {
        var instance = jobRepository.getLastJobInstance(DefaultValidationJobLauncher.JOB_NAME);
        return Optional.ofNullable(instance != null ? jobRepository.getLastJobExecution(instance) : null);
    }
}
