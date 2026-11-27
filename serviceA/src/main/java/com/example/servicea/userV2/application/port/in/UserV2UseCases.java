package com.example.servicea.userV2.application.port.in;

import com.example.servicea.shared.infrastructure.adapter.in.web.model.PageWrapper;
import com.example.servicea.userV2.application.command.UserV2PatchCommand;
import com.example.servicea.userV2.application.command.UserV2WriteCommand;
import com.example.servicea.userV2.domain.UserV2;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserV2UseCases {
    UserV2 create(UserV2WriteCommand command);

    UserV2 save(UserV2 user);

    PageWrapper<UserV2> list(Pageable pageable);

    UserV2 get(UUID id);

    UserV2 update(UUID id, UserV2WriteCommand command);

    UserV2 patch(UUID id, UserV2PatchCommand command);

    UserV2 delete(UUID id);
}
