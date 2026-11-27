package com.example.servicea.userV2.infrastructure.adapter.in.web.controller;

import com.example.servicea.shared.exception.UserV2NotFoundException;
import com.example.servicea.shared.exception.UsernameConflictException;
import com.example.servicea.shared.infrastructure.adapter.in.web.model.PageWrapper;
import com.example.servicea.shared.infrastructure.adapter.in.web.exception.GlobalExceptionHandler;
import com.example.servicea.support.CacheTestConfiguration;
import com.example.servicea.userV2.application.command.UserV2PatchCommand;
import com.example.servicea.userV2.application.command.UserV2WriteCommand;
import com.example.servicea.userV2.application.port.in.UserV2UseCases;
import com.example.servicea.userV2.domain.UserV2;
import com.example.servicea.userV2.infrastructure.adapter.in.web.mapper.UserV2RestMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@WebMvcTest(UserV2Controller.class)
@Import({GlobalExceptionHandler.class, UserV2RestMapperImpl.class, CacheTestConfiguration.class})
class UserV2ControllerTest {
    @Autowired
    MockMvcTester mvc;
    @MockitoBean
    UserV2UseCases useCases;
    private UUID id;
    private UserV2 user;

    @BeforeEach
    void setUp() {
        id = UUID.fromString("8f2a9d72-5f46-4c54-9ea1-10ee21d227c5");
        Instant timestamp = Instant.parse("2026-01-01T12:00:00Z");
        user = new UserV2(id, "alice", "Alice", "Smith", timestamp, timestamp);
    }

    @Test
    void createsUserAndReturnsAbsoluteLocation() {
        when(useCases.create(new UserV2WriteCommand("alice", "Alice", "Smith"))).thenReturn(user);

        assertThat(mvc.post().uri("/api/v2/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"username":"alice","name":"Alice","surname":"Smith"}
                        """))
                .hasStatus(201)
                .hasHeader("Location", "http://localhost/api/v2/users/" + id)
                .bodyJson().isLenientlyEqualTo("""
                        {
                          "id": "8f2a9d72-5f46-4c54-9ea1-10ee21d227c5",
                          "username": "alice",
                          "name": "Alice",
                          "surname": "Smith",
                          "createdAt": "2026-01-01T12:00:00Z",
                          "updatedAt": "2026-01-01T12:00:00Z"
                        }
                        """);
    }

    @Test
    void rejectsInvalidCreateRequestBeforeCallingUseCase() {
        assertThat(mvc.post().uri("/api/v2/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"username":"","name":"Alice","surname":"Smith"}
                        """))
                .hasStatus(400)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON);
        verifyNoInteractions(useCases);
    }

    @Test
    void returnsConflictForDuplicateUsername() {
        when(useCases.create(new UserV2WriteCommand("alice", "Alice", "Smith")))
                .thenThrow(new UsernameConflictException("alice"));

        assertThat(mvc.post().uri("/api/v2/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"username":"alice","name":"Alice","surname":"Smith"}
                        """))
                .hasStatus(409)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void listsUsers() {
        when(useCases.list(any())).thenReturn(new PageWrapper<>(List.of(user),
                new PageWrapper.PageInfo(0, 1, 20, 1)));

        assertThat(mvc.get().uri("/api/v2/users"))
                .hasStatusOk()
                .bodyJson().isLenientlyEqualTo("""
                        {
                          "items": [{
                            "id": "8f2a9d72-5f46-4c54-9ea1-10ee21d227c5",
                            "username": "alice",
                            "name": "Alice",
                            "surname": "Smith",
                            "createdAt": "2026-01-01T12:00:00Z",
                            "updatedAt": "2026-01-01T12:00:00Z"
                          }],
                          "pagination": {
                            "currentPage": 0,
                            "totalPages": 1,
                            "pageSize": 20,
                            "totalItems": 1
                          }
                        }
                        """);
    }

    @Test
    void getsExistingUser() {
        when(useCases.get(id)).thenReturn(user);

        assertThat(mvc.get().uri("/api/v2/users/{id}", id))
                .hasStatusOk()
                .bodyJson().isLenientlyEqualTo("""
                        {
                          "id": "8f2a9d72-5f46-4c54-9ea1-10ee21d227c5",
                          "username": "alice",
                          "name": "Alice",
                          "surname": "Smith",
                          "createdAt": "2026-01-01T12:00:00Z",
                          "updatedAt": "2026-01-01T12:00:00Z"
                        }
                        """);
    }

    @Test
    void returnsNotFoundForMissingUser() {
        when(useCases.get(id)).thenThrow(new UserV2NotFoundException(id));

        assertThat(mvc.get().uri("/api/v2/users/{id}", id))
                .hasStatus(404)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void replacesUserFields() {
        UserV2 updated = new UserV2(id, "alice-new", "Alicia", "Jones", user.getCreatedAt(), user.getUpdatedAt());
        when(useCases.update(eq(id), eq(new UserV2WriteCommand("alice-new", "Alicia", "Jones"))))
                .thenReturn(updated);

        assertThat(mvc.put().uri("/api/v2/users/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"username":"alice-new","name":"Alicia","surname":"Jones"}
                        """))
                .hasStatusOk()
                .bodyJson().isLenientlyEqualTo("""
                        {
                          "id": "8f2a9d72-5f46-4c54-9ea1-10ee21d227c5",
                          "username": "alice-new",
                          "name": "Alicia",
                          "surname": "Jones",
                          "createdAt": "2026-01-01T12:00:00Z",
                          "updatedAt": "2026-01-01T12:00:00Z"
                        }
                        """);
    }

    @Test
    void rejectsInvalidPutRequestBeforeCallingUseCase() {
        assertThat(mvc.put().uri("/api/v2/users/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"username":"alice","name":"Alicia","surname":""}
                        """))
                .hasStatus(400)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON);
        verifyNoInteractions(useCases);
    }

    @Test
    void patchesSuppliedFieldsAndPreservesNullFields() {
        UserV2 patched = new UserV2(id, "alice", "Alicia", "Smith", user.getCreatedAt(), user.getUpdatedAt());
        when(useCases.patch(eq(id), eq(new UserV2PatchCommand(null, "Alicia", null)))).thenReturn(patched);

        assertThat(mvc.patch().uri("/api/v2/users/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"username":null,"name":"Alicia","surname":null}
                        """))
                .hasStatusOk()
                .bodyJson().isLenientlyEqualTo("""
                        {
                          "id": "8f2a9d72-5f46-4c54-9ea1-10ee21d227c5",
                          "username": "alice",
                          "name": "Alicia",
                          "surname": "Smith",
                          "createdAt": "2026-01-01T12:00:00Z",
                          "updatedAt": "2026-01-01T12:00:00Z"
                        }
                        """);
        verify(useCases).patch(id, new UserV2PatchCommand(null, "Alicia", null));
    }

    @Test
    void rejectsInvalidPatchRequestBeforeCallingUseCase() {
        assertThat(mvc.patch().uri("/api/v2/users/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"username":"alice","name":"   ","surname":null}
                        """))
                .hasStatus(400)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON);
        verifyNoInteractions(useCases);
    }

    @Test
    void deletesUserAndReturnsDeletedRepresentation() {
        when(useCases.delete(id)).thenReturn(user);

        assertThat(mvc.delete().uri("/api/v2/users/{id}", id))
                .hasStatusOk()
                .bodyJson().isLenientlyEqualTo("""
                        {
                          "id": "8f2a9d72-5f46-4c54-9ea1-10ee21d227c5",
                          "username": "alice",
                          "name": "Alice",
                          "surname": "Smith",
                          "createdAt": "2026-01-01T12:00:00Z",
                          "updatedAt": "2026-01-01T12:00:00Z"
                        }
                        """);
    }

    @Test
    void rejectsInvalidDeleteIdBeforeCallingUseCase() {
        assertThat(mvc.delete().uri("/api/v2/users/not-a-uuid")).hasStatus(400);
        verifyNoInteractions(useCases);
    }
}
