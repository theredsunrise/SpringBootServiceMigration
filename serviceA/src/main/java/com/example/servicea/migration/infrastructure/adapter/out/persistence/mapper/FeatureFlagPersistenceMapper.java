package com.example.servicea.migration.infrastructure.adapter.out.persistence.mapper;

import com.example.servicea.migration.domain.entity.FeatureFlag;
import com.example.servicea.migration.infrastructure.adapter.out.persistence.entity.FeatureFlagJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FeatureFlagPersistenceMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "changeState", ignore = true)
    FeatureFlag toDomain(FeatureFlagJpaEntity entity);

    @Mapping(target = "version", ignore = true)
    FeatureFlagJpaEntity toEntity(FeatureFlag featureFlag);

    @Mapping(target = "version", ignore = true)
    void updateEntity(FeatureFlag featureFlag, @MappingTarget FeatureFlagJpaEntity entity);
}
