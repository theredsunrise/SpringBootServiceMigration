package com.example.servicea.userV1.infrastructure.adapter.in.web.controller;

import com.example.servicea.userV1.application.port.in.UserV1UseCases;
import com.example.servicea.userV1.domain.UserV1;
import com.example.servicea.userV1.infrastructure.adapter.in.web.dto.UserV1PatchRequest;
import com.example.servicea.userV1.infrastructure.adapter.in.web.dto.UserV1Response;
import com.example.servicea.userV1.infrastructure.adapter.in.web.dto.UserV1WriteRequest;
import com.example.servicea.userV1.infrastructure.adapter.in.web.mapper.UserV1RestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Create and manage users stored in PostgresQL DB.")
@RequiredArgsConstructor
public class UserV1Controller {
    private final UserV1UseCases service;
    private final UserV1RestMapper mapper;

    @PostMapping
    @Operation(summary = "Create a user.")
    public ResponseEntity<UserV1Response> create(@Valid @RequestBody UserV1WriteRequest request) {
        UserV1 user = service.create(mapper.toCommand(request));
        URI location = org.springframework.web.servlet.support.ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}").buildAndExpand(user.id()).toUri();
        return ResponseEntity.created(location).body(mapper.toResponse(user));
    }

    @GetMapping
    @Operation(summary = "List users.")
    public List<UserV1Response> list() {
        return service.list().stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user.")
    public UserV1Response get(@PathVariable UUID id) {
        return mapper.toResponse(service.get(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace a user's mutable fields.")
    public UserV1Response update(@PathVariable UUID id, @Valid @RequestBody UserV1WriteRequest request) {
        return mapper.toResponse(service.update(id, mapper.toCommand(request)));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update supplied user fields.")
    public UserV1Response patch(@PathVariable UUID id, @Valid @RequestBody UserV1PatchRequest request) {
        return mapper.toResponse(service.patch(id, mapper.toCommand(request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user.")
    public UserV1Response delete(@PathVariable UUID id) {
        return mapper.toResponse(service.delete(id));
    }
}
