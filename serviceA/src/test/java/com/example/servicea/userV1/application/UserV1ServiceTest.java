package com.example.servicea.userV1.application;

import com.example.servicea.shared.exception.UserV1NotFoundException;
import com.example.servicea.shared.exception.UsernameConflictException;
import com.example.servicea.shared.util.UUIDv7Generator;
import com.example.servicea.userV1.application.command.UserV1PatchCommand;
import com.example.servicea.userV1.application.command.UserV1WriteCommand;
import com.example.servicea.userV1.application.port.out.UserV1PersistencePort;
import com.example.servicea.userV1.application.service.UserV1Service;
import com.example.servicea.userV1.domain.UserV1;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserV1ServiceTest {
    private static final UUID USER_ID = UUID.fromString("8f2a9d72-5f46-4c54-9ea1-10ee21d227c5");

    @Mock
    private UserV1PersistencePort users;

    @InjectMocks
    private UserV1Service service;

    private UserV1 existingUser;

    @BeforeEach
    void setUp() {
        Instant now = Instant.parse("2026-01-01T12:00:00Z");
        existingUser = new UserV1(USER_ID, "alice", "Alice", now, now);
    }

    @Test
    void createsUsersWithCaseDistinctUsernames() {
        when(users.create(any(UserV1.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserV1 lowerCase = service.create(new UserV1WriteCommand("alice", "Alice"));
        UserV1 upperCase = service.create(new UserV1WriteCommand("Alice", "Different case"));

        assertEquals("alice", lowerCase.username());
        assertEquals("Alice", upperCase.username());
        verify(users).existsByUsername("alice");
        verify(users).existsByUsername("Alice");
    }

    @Test
    void rejectsDuplicateUsernamesAndMissingUsers() {
        UUID missingId = UUIDv7Generator.randomUUID();
        when(users.existsByUsername("alice")).thenReturn(true);
        when(users.existsByUsername("bob")).thenReturn(true);
        when(users.findById(USER_ID)).thenReturn(Optional.of(existingUser));
        when(users.findById(missingId)).thenReturn(Optional.empty());

        assertThrows(UsernameConflictException.class,
                () -> service.create(new UserV1WriteCommand("alice", "Other")));
        assertThrows(UserV1NotFoundException.class, () -> service.get(missingId));
        assertThrows(UsernameConflictException.class,
                () -> service.update(USER_ID, new UserV1WriteCommand("bob", "Renamed")));
        assertThrows(UsernameConflictException.class,
                () -> service.patch(USER_ID, new UserV1PatchCommand("bob", null)));
    }

    @Test
    void patchPreservesNullFieldsAndDeleteReturnsDeletedUser() {
        when(users.findById(USER_ID)).thenReturn(Optional.of(existingUser));
        when(users.save(any(UserV1.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserV1 patched = service.patch(USER_ID, new UserV1PatchCommand(null, "Alicia"));
        UserV1 deleted = service.delete(USER_ID);

        assertEquals("alice", patched.username());
        assertEquals("Alicia", patched.name());
        assertEquals(existingUser, deleted);
        verify(users).delete(existingUser);
        verify(users, never()).existsByUsername("alice");
    }

    @Test
    void patchMergesNullableFieldsAndChecksOnlyChangedUsername() {
        when(users.findById(USER_ID)).thenReturn(Optional.of(existingUser));
        when(users.existsByUsername("alice-new")).thenReturn(false);
        when(users.save(any(UserV1.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserV1 unchanged = service.patch(USER_ID, new UserV1PatchCommand(null, null));
        UserV1 changed = service.patch(USER_ID, new UserV1PatchCommand("alice-new", "Alicia"));

        assertEquals("alice", unchanged.username());
        assertEquals("Alice", unchanged.name());
        assertEquals("alice-new", changed.username());
        assertEquals("Alicia", changed.name());
        verify(users).existsByUsername("alice-new");
        verify(users, never()).existsByUsername("alice");
    }

    @Test
    void updateAndPatchAllowTheUnchangedUsername() {
        when(users.findById(USER_ID)).thenReturn(Optional.of(existingUser));
        when(users.save(any(UserV1.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserV1 updated = service.update(USER_ID, new UserV1WriteCommand("alice", "Alice Updated"));
        UserV1 patched = service.patch(USER_ID, new UserV1PatchCommand("alice", "Alice Patched"));

        assertEquals("alice", updated.username());
        assertEquals("alice", patched.username());
        assertEquals("Alice Patched", patched.name());
        verify(users, never()).existsByUsername("alice");
    }
}
