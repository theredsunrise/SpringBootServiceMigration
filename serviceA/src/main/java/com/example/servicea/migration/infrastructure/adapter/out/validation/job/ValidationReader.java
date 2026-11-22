package com.example.servicea.migration.infrastructure.adapter.out.validation.job;

import com.example.servicea.migration.application.port.out.UserValidationPersistencePort;
import com.example.servicea.migration.domain.model.UserPair;
import com.example.servicea.migration.infrastructure.adapter.out.validation.mapper.UserPairPersistenceMapper;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemStreamReader;
import org.springframework.batch.infrastructure.item.ItemStreamSupport;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component(DefaultValidationJobLauncher.READER_NAME)
@StepScope
public class ValidationReader extends ItemStreamSupport implements ItemStreamReader<UserPair> {
    private static final String LAST_KEY = "lastValidationKey";
    private static final int BATCH_SIZE = DefaultValidationJobLauncher.BATCH_SIZE;

    private final UserValidationPersistencePort validationPersistencePort;
    private final UserPairPersistenceMapper userPairMapper;

    private UUID lastKey;
    private Iterator<UserPair> currentBatch = Collections.emptyIterator();

    public ValidationReader(
            UserValidationPersistencePort validationPersistencePort,
            UserPairPersistenceMapper userPairMapper) {
        this.validationPersistencePort = validationPersistencePort;
        this.userPairMapper = userPairMapper;
        setName(DefaultValidationJobLauncher.READER_NAME);
    }

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        Thread current = Thread.currentThread();
        log.info("**** Batch reader thread info: name='{}', isVirtual={}", current.getName(), current.isVirtual());
    }

    @Override
    public void open(ExecutionContext executionContext) {
        this.currentBatch = Collections.emptyIterator();

        String nameKey = getNameKey();
        this.lastKey = executionContext.containsKey(nameKey)
                ? (UUID) executionContext.get(nameKey)
                : null;
    }

    @Override
    public UserPair read() {
        if (!currentBatch.hasNext()) {
            List<UserPair> nextBatch = (lastKey == null
                    ? validationPersistencePort.findFirstBatch(BATCH_SIZE)
                    : validationPersistencePort.findNextBatch(BATCH_SIZE, lastKey))
                    .stream()
                    .map(userPairMapper::toDomain)
                    .toList();
            if (nextBatch.isEmpty()) {
                return null;
            }
            this.currentBatch = nextBatch.iterator();
        }

        UserPair item = currentBatch.next();
        this.lastKey = item.userV1().id();
        return item;
    }

    @Override
    public void update(@NonNull ExecutionContext executionContext) {
        if (lastKey != null) {
            executionContext.put(getNameKey(), lastKey);
        }
    }

    private String getNameKey() {
        return getExecutionContextKey(LAST_KEY);
    }

    @Override
    public void close() {
        this.currentBatch = Collections.emptyIterator();
        this.lastKey = null;
    }
}
