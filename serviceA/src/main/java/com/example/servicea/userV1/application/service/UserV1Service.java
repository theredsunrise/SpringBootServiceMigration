package com.example.servicea.userV1.application.service;

import com.example.servicea.shared.exception.UserV1NotFoundException;
import com.example.servicea.shared.exception.UsernameConflictException;
import com.example.servicea.userV1.application.command.UserV1PatchCommand;
import com.example.servicea.userV1.application.command.UserV1WriteCommand;
import com.example.servicea.userV1.application.port.in.UserV1UseCases;
import com.example.servicea.userV1.application.port.out.UserV1PersistencePort;
import com.example.servicea.userV1.domain.UserV1;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class UserV1Service implements UserV1UseCases {
    private final UserV1PersistencePort users;

    @Override
    public UserV1 create(UserV1WriteCommand command) {
        ensureUsernameAvailable(command.username());
        return users.create(UserV1.create(command.username(), command.name()));
    }

    @Override
    public UserV1 save(UserV1 user) {
        return users.save(user);
    }

    @Override
    public UserV1 get(UUID id) {
        return users.findById(id).orElseThrow(() -> new UserV1NotFoundException(id));
    }

    @Override
    public List<UserV1> list() {
        return users.findAll();
    }

    @Override
    public UserV1 update(UUID id, UserV1WriteCommand command) {
        UserV1 current = get(id);
        ensureUsernameAvailableIfChanged(current.username(), command.username());
        return users.save(current.change(command.username(), command.name()));
    }

    @Override
    public UserV1 patch(UUID id, UserV1PatchCommand command) {
        UserV1 current = get(id);
        String nextUsername = command.username() == null ? current.username() : command.username();
        String nextName = command.name() == null ? current.name() : command.name();
        ensureUsernameAvailableIfChanged(current.username(), nextUsername);
        return users.save(current.change(nextUsername, nextName));
    }

    @Override
    public UserV1 delete(UUID id) {
        UserV1 user = get(id);
        users.delete(user);
        return user;
    }

    private void ensureUsernameAvailable(String username) {
        if (users.existsByUsername(username)) throw new UsernameConflictException(username);
    }

    private void ensureUsernameAvailableIfChanged(String currentUsername, String nextUsername) {
        if (!currentUsername.equals(nextUsername)) {
            ensureUsernameAvailable(nextUsername);
        }
    }
}
