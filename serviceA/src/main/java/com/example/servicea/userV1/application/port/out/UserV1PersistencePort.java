package com.example.servicea.userV1.application.port.out;

import com.example.servicea.shared.infrastructure.adapter.in.web.model.PageWrapper;
import com.example.servicea.userV1.domain.UserV1;
import org.springframework.data.domain.Pageable;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserV1PersistencePort {
    UserV1 create(UserV1 user);

    UserV1 save(UserV1 user);

    Optional<UserV1> findById(UUID id);

    PageWrapper<UserV1> findAll(Pageable pageable);

    void delete(UserV1 user);

    boolean existsByUsername(String username);

    List<UserV1> findFirstNotMigratedUsers(int limit);
}
