package com.example.servicea.migration.infrastructure.service;

import com.example.servicea.migration.domain.mapper.UserV1ToUserV2Mapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserV1ToUserV2MapperConfiguration {

    @Bean
    public UserV1ToUserV2Mapper userV1ToUserV2Mapper() {
        return new UserV1ToUserV2Mapper();
    }


}
