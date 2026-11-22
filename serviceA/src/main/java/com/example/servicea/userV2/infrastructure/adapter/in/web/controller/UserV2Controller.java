package com.example.servicea.userV2.infrastructure.adapter.in.web.controller;

import com.example.servicea.userV2.application.port.in.UserV2UseCases;
import com.example.servicea.userV2.domain.UserV2;
import com.example.servicea.userV2.infrastructure.adapter.in.web.dto.UserV2PatchRequest;
import com.example.servicea.userV2.infrastructure.adapter.in.web.dto.UserV2Response;
import com.example.servicea.userV2.infrastructure.adapter.in.web.dto.UserV2WriteRequest;
import com.example.servicea.userV2.infrastructure.adapter.in.web.mapper.UserV2RestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v2/users")
@Tag(name = "Users V2", description = "Create and manage users with surname.")
@RequiredArgsConstructor
public class UserV2Controller {
    private final UserV2UseCases service;
    private final UserV2RestMapper mapper;

    @PostMapping
    @Operation(
            summary = "Create a V2 user.",
            description = "Creates a user with username, name, and surname in the V2 user collection.")
    public ResponseEntity<UserV2Response> create(@Valid @RequestBody UserV2WriteRequest request) {
        UserV2 user = service.create(mapper.toCommand(request));
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}").buildAndExpand(user.id()).toUri();
        return ResponseEntity.created(location).body(mapper.toResponse(user));
    }

    @GetMapping
    @Operation(summary = "List V2 users.", description = "Returns all users stored through the V2 API.")
    public List<UserV2Response> list() {
        return service.list().stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a V2 user.", description = "Returns one V2 user by its UUID.")
    public UserV2Response get(@PathVariable UUID id) {
        return mapper.toResponse(service.get(id));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Replace a V2 user's mutable fields.",
            description = "Replaces the username, name, and surname of an existing V2 user.")
    public UserV2Response update(@PathVariable UUID id, @Valid @RequestBody UserV2WriteRequest request) {
        return mapper.toResponse(service.update(id, mapper.toCommand(request)));
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Update supplied V2 user fields.",
            description = "Updates non-null fields and preserves each field sent as null.")
    public UserV2Response patch(@PathVariable UUID id, @Valid @RequestBody UserV2PatchRequest request) {
        return mapper.toResponse(service.patch(id, mapper.toCommand(request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a V2 user.", description = "Deletes and returns one V2 user by UUID.")
    public UserV2Response delete(@PathVariable UUID id) {
        return mapper.toResponse(service.delete(id));
    }
}
