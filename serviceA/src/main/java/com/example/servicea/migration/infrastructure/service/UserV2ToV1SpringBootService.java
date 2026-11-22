package com.example.servicea.migration.infrastructure.service;

import com.example.servicea.migration.application.exception.ControllerDisabledException;
import com.example.servicea.migration.application.port.in.FeatureFlagsUseCases;
import com.example.servicea.migration.domain.mapper.UserV1ToUserV2Mapper;
import com.example.servicea.userV1.application.port.in.UserV1UseCases;
import com.example.servicea.userV1.infrastructure.service.UserV1ServiceFactory;
import com.example.servicea.userV2.application.command.UserV2PatchCommand;
import com.example.servicea.userV2.application.command.UserV2WriteCommand;
import com.example.servicea.userV2.application.port.in.UserV2UseCases;
import com.example.servicea.userV2.domain.UserV2;
import com.example.servicea.userV2.infrastructure.service.UserV2ServiceFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Primary
public class UserV2ToV1SpringBootService implements UserV2UseCases {

    private final UserV1UseCases userService;
    private final UserV2UseCases userV2Service;
    private final UserV1ToUserV2Mapper userV1ToUserV2Mapper;
    private final FeatureFlagsUseCases migrationService;

    public UserV2ToV1SpringBootService(UserV1ServiceFactory userServiceFactory,
                                       UserV2ServiceFactory userV2ServiceFactory,
                                       UserV1ToUserV2Mapper userV1ToUserV2Mapper,
                                       FeatureFlagsUseCases migrationService) {
        this.userService = userServiceFactory.create();
        this.userV2Service = userV2ServiceFactory.create();
        this.userV1ToUserV2Mapper = userV1ToUserV2Mapper;
        this.migrationService = migrationService;
    }

    @Override
    @Transactional
    public UserV2 create(UserV2WriteCommand command) {
        assertUseV2();
        var userV2 = userV2Service.create(command);
        if (migrationService.getCurrent().getState().syncV1()) {
            userService.save(userV1ToUserV2Mapper.from(userV2));
        }
        return userV2;
    }

    @Override
    public UserV2 save(UserV2 user) {
        assertUseV2();
        return userV2Service.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserV2> list() {
        assertUseV2();
        return userV2Service.list();
    }

    @Override
    @Transactional(readOnly = true)
    public UserV2 get(UUID id) {
        assertUseV2();
        return userV2Service.get(id);
    }

    @Override
    @Transactional
    public UserV2 update(UUID id, UserV2WriteCommand command) {
        assertUseV2();
        UserV2 userV2 = userV2Service.update(id, command);
        if (migrationService.getCurrent().getState().syncV1()) {
            userService.save(userV1ToUserV2Mapper.from(userV2));
        }
        return userV2;
    }

    @Override
    @Transactional
    public UserV2 patch(UUID id, UserV2PatchCommand command) {
        assertUseV2();
        var userV2 = userV2Service.patch(id, command);
        if (migrationService.getCurrent().getState().syncV1()) {
            userService.save(userV1ToUserV2Mapper.from(userV2));
        }
        return userV2;
    }

    @Override
    @Transactional
    public UserV2 delete(UUID id) {
        assertUseV2();
        var userV2 = userV2Service.delete(id);
        if (migrationService.getCurrent().getState().syncV1()) {
            userService.delete(id);
        }
        return userV2;
    }

    private void assertUseV2() {
        if (!migrationService.getState().useV2()) {
            throw new ControllerDisabledException("Controller userV2 migration has been disabled.");
        }
    }
}
