package com.example.servicea.userV2.infrastructure.adapter.out.persistence.mapper;

import com.example.servicea.shared.infrastructure.adapter.in.web.model.PageWrapper;
import com.example.servicea.userV2.domain.UserV2;
import com.example.servicea.userV2.infrastructure.adapter.out.persistence.entity.UserV2JpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface UserV2PersistenceMapper {
    @Mapping(target = "version", ignore = true)
    UserV2JpaEntity toEntity(UserV2 user);

    @Mapping(target = "version", ignore = true)
    void updateEntity(UserV2 user, @MappingTarget UserV2JpaEntity entity);

    UserV2 toDomain(UserV2JpaEntity entity);

    default PageWrapper<UserV2> toDomain(Page<UserV2JpaEntity> page) {
        if (page == null) return null;

        var pageInfo = new PageWrapper.PageInfo(
                page.getNumber(),
                page.getTotalPages(),
                page.getSize(),
                page.getTotalElements()
        );
        return new PageWrapper<>(
                page.getContent().stream().map(this::toDomain).toList(),
                pageInfo
        );
    }
}
