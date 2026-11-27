package com.example.servicea.userV2.infrastructure.adapter.in.web.mapper;

import com.example.servicea.shared.infrastructure.adapter.in.web.model.PageWrapper;
import com.example.servicea.shared.infrastructure.adapter.in.web.model.PageWrapperResponse;
import com.example.servicea.userV2.application.command.UserV2PatchCommand;
import com.example.servicea.userV2.application.command.UserV2WriteCommand;
import com.example.servicea.userV2.domain.UserV2;
import com.example.servicea.userV2.infrastructure.adapter.in.web.dto.UserV2PatchRequest;
import com.example.servicea.userV2.infrastructure.adapter.in.web.dto.UserV2Response;
import com.example.servicea.userV2.infrastructure.adapter.in.web.dto.UserV2WriteRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserV2RestMapper {
    UserV2WriteCommand toCommand(UserV2WriteRequest request);

    UserV2PatchCommand toCommand(UserV2PatchRequest request);

    UserV2Response toResponse(UserV2 user);

    PageWrapperResponse<UserV2Response> toResponse(PageWrapper<UserV2> page);
}
