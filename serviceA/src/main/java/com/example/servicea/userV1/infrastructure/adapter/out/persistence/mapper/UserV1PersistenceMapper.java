package com.example.servicea.userV1.infrastructure.adapter.out.persistence.mapper;

import com.example.servicea.userV1.domain.UserV1;
import com.example.servicea.userV1.infrastructure.adapter.out.persistence.entity.UserV1JpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserV1PersistenceMapper {
    @Mapping(target = "version", ignore = true)
    UserV1JpaEntity toEntity(UserV1 user);

    @Mapping(target = "version", ignore = true)
    void updateEntity(
            UserV1 user,
            @MappingTarget UserV1JpaEntity entity
    );

    UserV1 toDomain(UserV1JpaEntity entity);
}
