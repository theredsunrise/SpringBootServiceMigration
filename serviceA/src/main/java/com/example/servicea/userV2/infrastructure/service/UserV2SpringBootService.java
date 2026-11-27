package com.example.servicea.userV2.infrastructure.service;

import com.example.servicea.userV2.application.command.UserV2PatchCommand;
import com.example.servicea.userV2.application.command.UserV2WriteCommand;
import com.example.servicea.shared.infrastructure.adapter.in.web.model.PageWrapper;
import com.example.servicea.userV2.application.port.in.UserV2UseCases;
import com.example.servicea.userV2.domain.UserV2;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserV2SpringBootService implements UserV2UseCases {
    private final UserV2UseCases userService;

    UserV2SpringBootService(UserV2ServiceFactory factory) {
        this.userService = factory.create();
    }

    @Override
    @Transactional
    public UserV2 create(UserV2WriteCommand command) {
        return userService.create(command);
    }

    @Override
    public UserV2 save(UserV2 user) {
        return userService.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public PageWrapper<UserV2> list(Pageable pageable) {
        return userService.list(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public UserV2 get(UUID id) {
        return userService.get(id);
    }

    @Override
    @Transactional
    public UserV2 update(UUID id, UserV2WriteCommand command) {
        return userService.update(id, command);
    }

    @Override
    @Transactional
    public UserV2 patch(UUID id, UserV2PatchCommand command) {
        return userService.patch(id, command);
    }

    @Override
    @Transactional
    public UserV2 delete(UUID id) {
        return userService.delete(id);
    }
}
