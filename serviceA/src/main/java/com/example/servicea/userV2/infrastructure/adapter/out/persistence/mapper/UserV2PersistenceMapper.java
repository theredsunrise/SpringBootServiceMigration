package com.example.servicea.userV2.infrastructure.adapter.out.persistence.mapper;

import com.example.servicea.userV2.domain.UserV2;
import com.example.servicea.userV2.infrastructure.adapter.out.persistence.entity.UserV2JpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserV2PersistenceMapper {
    @Mapping(target = "version", ignore = true)
    UserV2JpaEntity toEntity(UserV2 user);

    @Mapping(target = "version", ignore = true)
    void updateEntity(UserV2 user, @MappingTarget UserV2JpaEntity entity);

    UserV2 toDomain(UserV2JpaEntity entity);
}
