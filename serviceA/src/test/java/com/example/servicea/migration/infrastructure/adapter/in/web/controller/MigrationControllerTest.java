package com.example.servicea.migration.infrastructure.adapter.in.web.controller;

import com.example.servicea.migration.application.exception.MigrationNotFoundException;
import com.example.servicea.migration.application.exception.UnsupportedMigrationStepException;
import com.example.servicea.migration.application.port.in.FeatureFlagsUseCases;
import com.example.servicea.migration.application.port.in.MigrationProcessUseCases;
import com.example.servicea.migration.application.port.in.MigrationWriterUseCases;
import com.example.servicea.migration.domain.entity.FeatureFlag;
import com.example.servicea.migration.domain.entity.MigrationState;
import com.example.servicea.migration.domain.model.BackFillStatus;
import com.example.servicea.migration.infrastructure.adapter.in.web.mapper.MigrationRestMapperImpl;
import com.example.servicea.shared.infrastructure.adapter.in.web.exception.GlobalExceptionHandler;
import com.example.servicea.support.CacheTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@WebMvcTest(MigrationController.class)
@Import({GlobalExceptionHandler.class, MigrationRestMapperImpl.class, CacheTestConfiguration.class})
class MigrationControllerTest {
    @Autowired
    MockMvcTester mvc;
    @MockitoBean
    FeatureFlagsUseCases featureFlags;
    @MockitoBean
    MigrationWriterUseCases writer;
    @MockitoBean
    MigrationProcessUseCases process;

    @Test
    void getsCurrentMigrationState() {
        Instant updatedAt = Instant.parse("2026-11-16T20:00:00Z");
        when(featureFlags.getCurrent()).thenReturn(new FeatureFlag(MigrationState.StartV2, updatedAt, updatedAt));

        assertThat(mvc.get().uri("/api/migration"))
                .hasStatusOk()
                .bodyJson().isLenientlyEqualTo("""
                        {
                          "id": 1,
                          "state": "StartV2",
                          "updatedAt": "2026-11-16T20:00:00Z"
                        }
                        """);
    }

    @Test
    void returnsNotFoundWhenMigrationStateDoesNotExist() {
        when(featureFlags.getCurrent()).thenThrow(new MigrationNotFoundException());

        assertThat(mvc.get().uri("/api/migration"))
                .hasStatus(404)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void updatesMigrationState() {
        Instant updatedAt = Instant.parse("2026-11-16T20:00:00Z");
        when(featureFlags.setState(MigrationState.StartV2))
                .thenReturn(new FeatureFlag(MigrationState.StartV2, updatedAt, updatedAt));

        assertThat(mvc.put().uri("/api/migration")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"state":"StartV2"}
                        """))
                .hasStatusOk()
                .bodyJson().isLenientlyEqualTo("""
                        {
                          "id": 1,
                          "state": "StartV2",
                          "updatedAt": "2026-11-16T20:00:00Z"
                        }
                        """);
    }

    @Test
    void rejectsNullOrUnknownMigrationState() {
        assertThat(mvc.put().uri("/api/migration")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"state":null}
                        """))
                .hasStatus(400)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON);

        assertThat(mvc.put().uri("/api/migration")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"state":"Unknown"}
                        """))
                .hasStatus(400);
    }

    @Test
    void returnsUnprocessableForUnsupportedMigrationTransition() {
        when(featureFlags.setState(MigrationState.ActivatedV2))
                .thenThrow(new UnsupportedMigrationStepException("unsupported transition"));

        assertThat(mvc.put().uri("/api/migration")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"state":"ActivatedV2"}
                        """))
                .hasStatus(422)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void listsBackfillStatuses() {
        Instant startedAt = Instant.parse("2026-11-19T12:00:00Z");
        UUID fromId = UUID.fromString("8f2a9d72-5f46-4c54-9ea1-10ee21d227c5");
        UUID toId = UUID.fromString("e6bf3ad4-24f3-43c3-a127-8d230a11a42d");
        when(writer.getBackFillStatuses()).thenReturn(List.of(
                new BackFillStatus("service-a-1", 100, startedAt, 1250, fromId, toId, "PROCESSED")));

        assertThat(mvc.get().uri("/api/migration/backfill-statuses"))
                .hasStatusOk()
                .bodyJson().isLenientlyEqualTo("""
                        [{
                          "instanceId": "service-a-1",
                          "batchSize": 100,
                          "startedAt": "2026-11-19T12:00:00Z",
                          "durationMs": 1250,
                          "fromId": "8f2a9d72-5f46-4c54-9ea1-10ee21d227c5",
                          "toId": "e6bf3ad4-24f3-43c3-a127-8d230a11a42d",
                          "status": "PROCESSED"
                        }]
                        """);
    }
}
