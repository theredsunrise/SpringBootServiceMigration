package com.example.servicea.userV2.infrastructure.service;

import com.example.servicea.userV2.application.port.out.UserV2PersistencePort;
import com.example.servicea.userV2.application.service.UserV2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserV2ServiceFactory {
    private final UserV2PersistencePort users;

    public UserV2Service create() {
        return new UserV2Service(users);
    }
}