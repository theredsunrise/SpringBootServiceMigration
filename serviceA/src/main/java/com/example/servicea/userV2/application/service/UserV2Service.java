package com.example.servicea.userV2.application.service;

import com.example.servicea.shared.exception.UserV2NotFoundException;
import com.example.servicea.shared.exception.UsernameConflictException;
import com.example.servicea.shared.infrastructure.adapter.in.web.model.PageWrapper;
import com.example.servicea.userV2.application.command.UserV2PatchCommand;
import com.example.servicea.userV2.application.command.UserV2WriteCommand;
import com.example.servicea.userV2.application.port.in.UserV2UseCases;
import com.example.servicea.userV2.application.port.out.UserV2PersistencePort;
import com.example.servicea.userV2.domain.UserV2;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@RequiredArgsConstructor
public class UserV2Service implements UserV2UseCases {
    private final UserV2PersistencePort users;

    @Override
    public UserV2 create(UserV2WriteCommand command) {
        ensureUsernameAvailable(command.username());
        return users.create(UserV2.create(command.username(), command.name(), command.surname()));
    }

    @Override
    public UserV2 save(UserV2 user) {
        return users.save(user);
    }

    @Override
    public PageWrapper<UserV2> list(Pageable pageable) {
        return users.findAll(pageable);
    }

    @Override
    public UserV2 get(UUID id) {
        return users.findById(id).orElseThrow(() -> new UserV2NotFoundException(id));
    }

    @Override
    public UserV2 update(UUID id, UserV2WriteCommand command) {
        UserV2 current = get(id);
        ensureUsernameAvailableIfChanged(current.username(), command.username());
        return users.save(current.change(command.username(), command.name(), command.surname()));
    }

    @Override
    public UserV2 patch(UUID id, UserV2PatchCommand command) {
        UserV2 current = get(id);
        String nextUsername = command.username() == null ? current.username() : command.username();
        String nextName = command.name() == null ? current.name() : command.name();
        String nextSurname = command.surname() == null ? current.surname() : command.surname();
        ensureUsernameAvailableIfChanged(current.username(), nextUsername);
        return users.save(current.change(nextUsername, nextName, nextSurname));
    }

    @Override
    public UserV2 delete(UUID id) {
        UserV2 user = get(id);
        users.delete(user);
        return user;
    }

    private void ensureUsernameAvailable(String username) {
        if (users.existsByUsername(username)) {
            throw new UsernameConflictException(username);
        }
    }

    private void ensureUsernameAvailableIfChanged(String currentUsername, String nextUsername) {
        if (!currentUsername.equals(nextUsername)) {
            ensureUsernameAvailable(nextUsername);
        }
    }
}
