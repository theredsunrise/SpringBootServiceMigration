package com.example.servicea.userV2.application.port.out;

import com.example.servicea.shared.infrastructure.adapter.in.web.model.PageWrapper;
import com.example.servicea.userV2.domain.UserV2;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserV2PersistencePort {
    UserV2 create(UserV2 user);

    UserV2 save(UserV2 user);

    void saveAll(List<UserV2> users);

    Optional<UserV2> findById(UUID id);

    PageWrapper<UserV2> findAll(Pageable pageable);

    void delete(UserV2 user);

    boolean existsByUsername(String username);
}
