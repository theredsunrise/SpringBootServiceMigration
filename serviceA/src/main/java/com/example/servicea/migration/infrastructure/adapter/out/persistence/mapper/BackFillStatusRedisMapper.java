package com.example.servicea.migration.infrastructure.adapter.out.persistence.mapper;

import com.example.servicea.migration.domain.model.BackFillStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class BackFillStatusRedisMapper {
    private final ObjectMapper objectMapper;

    public String serialize(BackFillStatus status) {
        try {
            return objectMapper.writeValueAsString(status);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Failed to serialize backfill status.", exception);
        }
    }

    public BackFillStatus deserialize(String value) {
        try {
            return objectMapper.readValue(value, BackFillStatus.class);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Failed to deserialize backfill status.", exception);
        }
    }
}
