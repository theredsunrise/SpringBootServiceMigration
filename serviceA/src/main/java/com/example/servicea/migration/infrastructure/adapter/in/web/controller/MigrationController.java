package com.example.servicea.migration.infrastructure.adapter.in.web.controller;

import com.example.servicea.migration.application.port.in.FeatureFlagsUseCases;
import com.example.servicea.migration.application.port.in.MigrationProcessUseCases;
import com.example.servicea.migration.application.port.in.MigrationWriterUseCases;
import com.example.servicea.migration.domain.model.ValidationJobAttributes;
import com.example.servicea.migration.infrastructure.adapter.in.web.dto.BackFillStatusResponse;
import com.example.servicea.migration.infrastructure.adapter.in.web.dto.MigrationResponse;
import com.example.servicea.migration.infrastructure.adapter.in.web.dto.MigrationStateRequest;
import com.example.servicea.migration.infrastructure.adapter.in.web.dto.ValidationStatusResponse;
import com.example.servicea.migration.infrastructure.adapter.in.web.mapper.MigrationRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/migration")
@Tag(name = "Migration", description = "Read and update the V2 migration rollout state.")
@RequiredArgsConstructor
public class MigrationController {
    private final FeatureFlagsUseCases featureFlags;
    private final MigrationWriterUseCases writer;
    private final MigrationProcessUseCases process;
    private final MigrationRestMapper mapper;

    @Value("${HOSTNAME:unknown}")
    private String hostName;

    @GetMapping
    @Operation(summary = "Get migration state.", description = "Returns the singleton migration rollout state.")
    public MigrationResponse getState() {
        return mapper.toResponse(featureFlags.getCurrent());
    }

    @PutMapping
    @Operation(summary = "Set migration state.", description = "Sets one of the supported migration rollout states.")
    public MigrationResponse setState(@Valid @RequestBody MigrationStateRequest request) {
        return mapper.toResponse(featureFlags.setState(request.state()));
    }

    @GetMapping("/backfill-statuses")
    @Operation(summary = "Get backfill statuses.", description = "Returns recent backfill status from all instances.")
    public List<BackFillStatusResponse> getBackFillStatuses() {
        return mapper.toBackFillStatusResponses(writer.getBackFillStatuses());
    }

    @GetMapping("/validation-statuses")
    @Operation(
            summary = "Get validation statuses.",
            description = "Returns up to 10 stored validation statuses collected during migration validation.")
    public List<ValidationStatusResponse> getValidationStatuses() {
        return mapper.toValidationStatusResponses(writer.getValidationStatuses());
    }

    @GetMapping("/validation-job-status")
    @Operation(
            summary = "Get validation job status.",
            description = "Get the current validation job status, or an empty result if none is present.")
    public Optional<ValidationJobAttributes> getValidationJobStatus() {
        return process.getValidationJobStatus();
    }

    @PostMapping("/start-validation-job")
    @Operation(
            summary = "Start or inspect the validation job.",
            description = "Starts a new validation job if none is running; otherwise returns the current execution status.")
    public ValidationJobAttributes startValidationJob() {
        var result = process.startValidationJob();
        log.info("**** EXIT");
        return result;
    }
}
