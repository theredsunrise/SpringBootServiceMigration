package com.example.servicea.migration.infrastructure.service;

import com.example.servicea.migration.application.exception.ControllerDisabledException;
import com.example.servicea.migration.application.port.in.FeatureFlagsUseCases;
import com.example.servicea.migration.domain.mapper.UserV1ToUserV2Mapper;
import com.example.servicea.shared.infrastructure.adapter.in.web.model.PageWrapper;
import com.example.servicea.userV1.application.command.UserV1PatchCommand;
import com.example.servicea.userV1.application.command.UserV1WriteCommand;
import com.example.servicea.userV1.application.port.in.UserV1UseCases;
import com.example.servicea.userV1.domain.UserV1;
import com.example.servicea.userV1.infrastructure.service.UserV1ServiceFactory;
import com.example.servicea.userV2.application.port.in.UserV2UseCases;
import com.example.servicea.userV2.infrastructure.service.UserV2ServiceFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Primary
public class UserV1ToV2SpringBootService implements UserV1UseCases {

    private final UserV1UseCases userService;
    private final UserV2UseCases userV2Service;
    private final UserV1ToUserV2Mapper userV1ToUserV2Mapper;
    private final FeatureFlagsUseCases migrationService;

    public UserV1ToV2SpringBootService(UserV1ServiceFactory userServiceFactory,
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
    public UserV1 create(UserV1WriteCommand command) {
        assertUseV1();
        var user = userService.create(command);
        if (migrationService.getCurrent().getState().syncV2()) {
            userV2Service.save(userV1ToUserV2Mapper.from(user));
        }
        return user;
    }

    @Override
    public UserV1 save(UserV1 user) {
        assertUseV1();
        return userService.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public PageWrapper<UserV1> list(Pageable pageable) {
        assertUseV1();
        return userService.list(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public UserV1 get(UUID id) {
        assertUseV1();
        return userService.get(id);
    }

    @Override
    @Transactional
    public UserV1 update(UUID id, UserV1WriteCommand command) {
        assertUseV1();
        var user = userService.update(id, command);
        if (migrationService.getCurrent().getState().syncV2()) {
            userV2Service.save(userV1ToUserV2Mapper.from(user));
        }
        return user;
    }

    @Override
    @Transactional
    public UserV1 patch(UUID id, UserV1PatchCommand command) {
        assertUseV1();
        var user = userService.patch(id, command);
        if (migrationService.getCurrent().getState().syncV2()) {
            userV2Service.save(userV1ToUserV2Mapper.from(user));
        }
        return user;
    }

    @Override
    @Transactional
    public UserV1 delete(UUID id) {
        assertUseV1();
        var user = userService.delete(id);
        if (migrationService.getCurrent().getState().syncV2()) {
            userV2Service.delete(user.getId());
        }
        return user;
    }

    private void assertUseV1() {
        if (!migrationService.getState().useV1()) {
            throw new ControllerDisabledException("Controller userV1 migration has been disabled.");
        }
    }
}
