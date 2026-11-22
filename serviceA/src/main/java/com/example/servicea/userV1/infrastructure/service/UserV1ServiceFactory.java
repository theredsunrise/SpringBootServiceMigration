package com.example.servicea.userV1.infrastructure.service;

import com.example.servicea.userV1.application.port.out.UserV1PersistencePort;
import com.example.servicea.userV1.application.service.UserV1Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserV1ServiceFactory {
    private final UserV1PersistencePort users;

    public UserV1Service create() {
        return new UserV1Service(users);
    }
}
