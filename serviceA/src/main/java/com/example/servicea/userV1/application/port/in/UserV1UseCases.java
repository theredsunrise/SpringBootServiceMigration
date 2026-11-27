package com.example.servicea.userV1.application.port.in;

import com.example.servicea.shared.infrastructure.adapter.in.web.model.PageWrapper;
import com.example.servicea.userV1.application.command.UserV1PatchCommand;
import com.example.servicea.userV1.application.command.UserV1WriteCommand;
import com.example.servicea.userV1.domain.UserV1;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface UserV1UseCases {
    UserV1 create(UserV1WriteCommand command);

    UserV1 save(UserV1 user);

    PageWrapper<UserV1> list(Pageable pageable);

    UserV1 get(UUID id);

    UserV1 update(UUID id, UserV1WriteCommand command);

    UserV1 patch(UUID id, UserV1PatchCommand command);

    UserV1 delete(UUID id);
}
