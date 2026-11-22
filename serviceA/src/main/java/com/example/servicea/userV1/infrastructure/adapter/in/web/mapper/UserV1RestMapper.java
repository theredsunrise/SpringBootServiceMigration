package com.example.servicea.userV1.infrastructure.adapter.in.web.mapper;

import com.example.servicea.userV1.application.command.UserV1PatchCommand;
import com.example.servicea.userV1.application.command.UserV1WriteCommand;
import com.example.servicea.userV1.domain.UserV1;
import com.example.servicea.userV1.infrastructure.adapter.in.web.dto.UserV1PatchRequest;
import com.example.servicea.userV1.infrastructure.adapter.in.web.dto.UserV1Response;
import com.example.servicea.userV1.infrastructure.adapter.in.web.dto.UserV1WriteRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserV1RestMapper {
    UserV1WriteCommand toCommand(UserV1WriteRequest request);

    UserV1PatchCommand toCommand(UserV1PatchRequest request);

    UserV1Response toResponse(UserV1 user);
}
