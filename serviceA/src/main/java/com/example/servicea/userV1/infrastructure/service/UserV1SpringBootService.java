package com.example.servicea.userV1.infrastructure.service;

import com.example.servicea.shared.infrastructure.adapter.in.web.model.PageWrapper;
import com.example.servicea.userV1.application.command.UserV1PatchCommand;
import com.example.servicea.userV1.application.command.UserV1WriteCommand;
import com.example.servicea.userV1.application.port.in.UserV1UseCases;
import com.example.servicea.userV1.domain.UserV1;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserV1SpringBootService implements UserV1UseCases {
    private final UserV1UseCases userService;

    UserV1SpringBootService(UserV1ServiceFactory factory) {
        this.userService = factory.create();
    }

    @Override
    @Transactional
    public UserV1 create(UserV1WriteCommand command) {
        return userService.create(command);
    }

    @Override
    public UserV1 save(UserV1 user) {
        return userService.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public PageWrapper<UserV1> list(Pageable page) {
        return userService.list(page);
    }

    @Override
    @Transactional(readOnly = true)
    public UserV1 get(UUID id) {
        return userService.get(id);
    }

    @Override
    @Transactional
    public UserV1 update(UUID id, UserV1WriteCommand command) {
        return userService.update(id, command);
    }

    @Override
    @Transactional
    public UserV1 patch(UUID id, UserV1PatchCommand command) {
        return userService.patch(id, command);
    }

    @Override
    @Transactional
    public UserV1 delete(UUID id) {
        return userService.delete(id);
    }
}
