package com.example.servicea.migration.infrastructure.adapter.out.persistence.mapper;

import com.example.servicea.migration.domain.model.ValidationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class ValidationStatusRedisMapper {
    private final ObjectMapper objectMapper;

    public String serialize(ValidationStatus status) {
        try {
            return objectMapper.writeValueAsString(status);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Failed to serialize validation status.", exception);
        }
    }

    public ValidationStatus deserialize(String value) {
        try {
            return objectMapper.readValue(value, ValidationStatus.class);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Failed to deserialize validation status.", exception);
        }
    }
}
