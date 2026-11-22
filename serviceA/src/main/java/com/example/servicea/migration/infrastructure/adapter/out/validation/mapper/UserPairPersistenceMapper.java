package com.example.servicea.migration.infrastructure.adapter.out.validation.mapper;

import com.example.servicea.migration.domain.model.UserPair;
import com.example.servicea.migration.infrastructure.adapter.out.persistence.entity.UserJpaEntityPair;
import com.example.servicea.userV1.infrastructure.adapter.out.persistence.mapper.UserV1PersistenceMapper;
import com.example.servicea.userV2.infrastructure.adapter.out.persistence.mapper.UserV2PersistenceMapper;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring",
        uses = {UserV1PersistenceMapper.class, UserV2PersistenceMapper.class}
)
public interface UserPairPersistenceMapper {
    UserPair toDomain(UserJpaEntityPair entityPair);
}
