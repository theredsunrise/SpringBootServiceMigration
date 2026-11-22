package com.example.servicea.migration.infrastructure.adapter.out.validation.job;

import com.example.servicea.migration.application.port.in.MigrationWriterUseCases;
import com.example.servicea.migration.domain.model.ValidationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component(DefaultValidationJobLauncher.WRITER_NAME)
@RequiredArgsConstructor
public class ValidationWriter implements ItemWriter<ValidationStatus> {
    private final MigrationWriterUseCases migrationUseCases;

    @Override
    public void write(Chunk<? extends ValidationStatus> chunk) {
        migrationUseCases.setValidationStatuses((List<ValidationStatus>) chunk.getItems());
    }
}
