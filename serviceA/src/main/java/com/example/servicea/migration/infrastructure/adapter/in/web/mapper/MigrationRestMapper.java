package com.example.servicea.migration.infrastructure.adapter.in.web.mapper;

import com.example.servicea.migration.domain.entity.FeatureFlag;
import com.example.servicea.migration.domain.model.BackFillStatus;
import com.example.servicea.migration.domain.model.ValidationStatus;
import com.example.servicea.migration.infrastructure.adapter.in.web.dto.BackFillStatusResponse;
import com.example.servicea.migration.infrastructure.adapter.in.web.dto.MigrationResponse;
import com.example.servicea.migration.infrastructure.adapter.in.web.dto.ValidationStatusResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MigrationRestMapper {
    MigrationResponse toResponse(FeatureFlag featureFlag);

    BackFillStatusResponse toResponse(BackFillStatus status);

    ValidationStatusResponse toResponse(ValidationStatus status);

    List<BackFillStatusResponse> toBackFillStatusResponses(List<BackFillStatus> statuses);

    List<ValidationStatusResponse> toValidationStatusResponses(List<ValidationStatus> statuses);
}
