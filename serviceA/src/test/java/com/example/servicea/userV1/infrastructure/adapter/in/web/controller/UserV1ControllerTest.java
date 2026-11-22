package com.example.servicea.userV1.infrastructure.adapter.in.web.controller;

import com.example.servicea.shared.exception.UserV1NotFoundException;
import com.example.servicea.shared.exception.UsernameConflictException;
import com.example.servicea.shared.infrastructure.adapter.in.web.exception.GlobalExceptionHandler;
import com.example.servicea.shared.util.UUIDv7Generator;
import com.example.servicea.support.CacheTestConfiguration;
import com.example.servicea.userV1.application.command.UserV1PatchCommand;
import com.example.servicea.userV1.application.command.UserV1WriteCommand;
import com.example.servicea.userV1.application.port.in.UserV1UseCases;
import com.example.servicea.userV1.domain.UserV1;
import com.example.servicea.userV1.infrastructure.adapter.in.web.mapper.UserV1RestMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserV1Controller.class)
@Import({GlobalExceptionHandler.class, UserV1RestMapperImpl.class, CacheTestConfiguration.class})
class UserV1ControllerTest {
    @Autowired
    MockMvc mvc;
    @MockitoBean
    UserV1UseCases useCases;
    private UUID id;
    private UserV1 user;

    @BeforeEach
    void setUp() {
        id = UUIDv7Generator.randomUUID();
        user = user(id, "alice", "Alice");
    }

    @Test
    void createsUserAndReturnsAbsoluteLocation() throws Exception {
        when(useCases.create(new UserV1WriteCommand("alice", "Alice"))).thenReturn(user);

        mvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"name\":\"Alice\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/users/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    void rejectsInvalidCreateRequestWithProblemDetails() throws Exception {
        mvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"name\":\"Alice\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Invalid request"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void returnsConflictProblemDetailsForDuplicateUsername() throws Exception {
        when(useCases.create(new UserV1WriteCommand("alice", "Alice")))
                .thenThrow(new UsernameConflictException("alice"));

        mvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"name\":\"Alice\"}"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void returnsNotFoundWhenUserWasDeletedDuringUpdateOrPatch() throws Exception {
        when(useCases.update(eq(id), eq(new UserV1WriteCommand("alice", "Updated"))))
                .thenThrow(new UserV1NotFoundException(id));
        when(useCases.patch(eq(id), eq(new UserV1PatchCommand(null, "Updated"))))
                .thenThrow(new UserV1NotFoundException(id));

        mvc.perform(put("/api/v1/users/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"name\":\"Updated\"}"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404));

        mvc.perform(patch("/api/v1/users/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":null,\"name\":\"Updated\"}"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void returnsConflictForOptimisticLockFailureWithoutStaleStateCause() throws Exception {
        when(useCases.update(eq(id), eq(new UserV1WriteCommand("alice", "Updated"))))
                .thenThrow(new OptimisticLockingFailureException("Concurrent version change"));

        mvc.perform(put("/api/v1/users/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"name\":\"Updated\"}"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void listsUsers() throws Exception {
        when(useCases.list()).thenReturn(List.of(user));

        mvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].username").value("alice"));
    }

    @Test
    void getsExistingUser() throws Exception {
        when(useCases.get(id)).thenReturn(user);

        mvc.perform(get("/api/v1/users/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    void returnsNotFoundProblemDetailsForMissingUser() throws Exception {
        when(useCases.get(id)).thenThrow(new UserV1NotFoundException(id));

        mvc.perform(get("/api/v1/users/" + id))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void replacesUserFields() throws Exception {
        UserV1 updated = user(id, "alice-new", "Alicia");
        when(useCases.update(eq(id), eq(new UserV1WriteCommand("alice-new", "Alicia"))))
                .thenReturn(updated);

        mvc.perform(put("/api/v1/users/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice-new\",\"name\":\"Alicia\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice-new"))
                .andExpect(jsonPath("$.name").value("Alicia"));
    }

    @Test
    void rejectsInvalidPutRequestBeforeCallingUseCase() throws Exception {
        mvc.perform(put("/api/v1/users/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + "x".repeat(41) + "\",\"name\":\"Alicia\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(useCases);
    }

    @Test
    void patchesOnlySuppliedFields() throws Exception {
        UserV1 patched = user(id, "alice", "Alicia");
        when(useCases.patch(eq(id), eq(new UserV1PatchCommand(null, "Alicia"))))
                .thenReturn(patched);

        mvc.perform(patch("/api/v1/users/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":null,\"name\":\"Alicia\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.name").value("Alicia"));

        verify(useCases).patch(id, new UserV1PatchCommand(null, "Alicia"));
    }

    @Test
    void patchesUsernameAndPreservesNameWhenNameIsNull() throws Exception {
        UserV1 patched = user(id, "alice-new", "Alice");
        when(useCases.patch(eq(id), eq(new UserV1PatchCommand("alice-new", null))))
                .thenReturn(patched);

        mvc.perform(patch("/api/v1/users/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice-new\",\"name\":null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice-new"))
                .andExpect(jsonPath("$.name").value("Alice"));

        verify(useCases).patch(id, new UserV1PatchCommand("alice-new", null));
    }

    @Test
    void preservesBothFieldsWhenBothPatchFieldsAreNull() throws Exception {
        when(useCases.patch(eq(id), eq(new UserV1PatchCommand(null, null)))).thenReturn(user);

        mvc.perform(patch("/api/v1/users/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":null,\"name\":null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.name").value("Alice"));

        verify(useCases).patch(id, new UserV1PatchCommand(null, null));
    }

    @Test
    void patchesBothFieldsWhenBothValuesAreProvided() throws Exception {
        UserV1 patched = user(id, "alice-new", "Alicia");
        when(useCases.patch(eq(id), eq(new UserV1PatchCommand("alice-new", "Alicia"))))
                .thenReturn(patched);

        mvc.perform(patch("/api/v1/users/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice-new\",\"name\":\"Alicia\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice-new"))
                .andExpect(jsonPath("$.name").value("Alicia"));

        verify(useCases).patch(id, new UserV1PatchCommand("alice-new", "Alicia"));
    }

    @Test
    void rejectsInvalidPatchRequestBeforeCallingUseCase() throws Exception {
        mvc.perform(patch("/api/v1/users/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":null,\"name\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(useCases);
    }

    @Test
    void deletesUserAndReturnsDeletedRepresentation() throws Exception {
        when(useCases.delete(id)).thenReturn(user);

        mvc.perform(delete("/api/v1/users/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    void rejectsInvalidDeleteId() throws Exception {
        mvc.perform(delete("/api/v1/users/not-a-uuid"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(useCases);
    }

    private UserV1 user(UUID userId, String username, String name) {
        Instant timestamp = Instant.parse("2026-01-01T00:00:00Z");
        return new UserV1(userId, username, name, timestamp, timestamp);
    }
}
