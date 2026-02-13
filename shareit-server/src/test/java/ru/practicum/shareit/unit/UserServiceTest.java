package ru.practicum.shareit.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_whenEmailFree_shouldSaveWithNullId() {
        User input = new User();
        input.setId(999L);
        input.setName("Ivan");
        input.setEmail("ivan@mail.ru");

        when(userRepository.existsByEmail("ivan@mail.ru")).thenReturn(false);

        User saved = new User();
        saved.setId(1L);
        saved.setName("Ivan");
        saved.setEmail("ivan@mail.ru");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        User result = userService.createUser(input);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("ivan@mail.ru");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User toSave = captor.getValue();

        assertThat(toSave.getId()).isNull();
        assertThat(toSave.getName()).isEqualTo("Ivan");
        assertThat(toSave.getEmail()).isEqualTo("ivan@mail.ru");

        verify(userRepository).existsByEmail("ivan@mail.ru");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void createUser_whenEmailAlreadyUsed_shouldThrowConflict() {
        User input = new User();
        input.setName("Ivan");
        input.setEmail("ivan@mail.ru");

        when(userRepository.existsByEmail("ivan@mail.ru")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(input))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email is already used");

        verify(userRepository).existsByEmail("ivan@mail.ru");
        verify(userRepository, never()).save(any());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateUser_whenUserNotFound_shouldThrowNotFound() {
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        User patch = new User();
        patch.setName("New");

        assertThatThrownBy(() -> userService.updateUser(10L, patch))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById(10L);
        verify(userRepository, never()).save(any());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateUser_whenPatchNameBlank_shouldThrowValidation() {
        User existing = new User();
        existing.setId(10L);
        existing.setName("Old");
        existing.setEmail("old@mail.ru");

        when(userRepository.findById(10L)).thenReturn(Optional.of(existing));

        User patch = new User();
        patch.setName("   "); // blank

        assertThatThrownBy(() -> userService.updateUser(10L, patch))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("name cannot be empty");

        verify(userRepository).findById(10L);
        verify(userRepository, never()).save(any());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateUser_whenPatchEmailBlank_shouldThrowValidation() {
        User existing = new User();
        existing.setId(10L);
        existing.setName("Old");
        existing.setEmail("old@mail.ru");

        when(userRepository.findById(10L)).thenReturn(Optional.of(existing));

        User patch = new User();
        patch.setEmail(" "); // blank

        assertThatThrownBy(() -> userService.updateUser(10L, patch))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("email cannot be empty");

        verify(userRepository).findById(10L);
        verify(userRepository, never()).save(any());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateUser_whenPatchEmailUsedByAnotherUser_shouldThrowConflict() {
        User existing = new User();
        existing.setId(10L);
        existing.setName("Old");
        existing.setEmail("old@mail.ru");

        when(userRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmailAndIdNot("new@mail.ru", 10L)).thenReturn(true);

        User patch = new User();
        patch.setEmail("new@mail.ru");

        assertThatThrownBy(() -> userService.updateUser(10L, patch))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email is already used");

        verify(userRepository).findById(10L);
        verify(userRepository).existsByEmailAndIdNot("new@mail.ru", 10L);
        verify(userRepository, never()).save(any());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateUser_whenPatchNameAndEmailValid_shouldUpdateAndSave() {
        User existing = new User();
        existing.setId(10L);
        existing.setName("Old");
        existing.setEmail("old@mail.ru");

        when(userRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmailAndIdNot("new@mail.ru", 10L)).thenReturn(false);

        User patch = new User();
        patch.setName("NewName");
        patch.setEmail("new@mail.ru");

        User saved = new User();
        saved.setId(10L);
        saved.setName("NewName");
        saved.setEmail("new@mail.ru");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        User result = userService.updateUser(10L, patch);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("NewName");
        assertThat(result.getEmail()).isEqualTo("new@mail.ru");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User toSave = captor.getValue();

        assertThat(toSave.getId()).isEqualTo(10L);
        assertThat(toSave.getName()).isEqualTo("NewName");
        assertThat(toSave.getEmail()).isEqualTo("new@mail.ru");

        verify(userRepository).findById(10L);
        verify(userRepository).existsByEmailAndIdNot("new@mail.ru", 10L);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void deleteUser_whenExists_shouldDeleteById() {
        User existing = new User();
        existing.setId(10L);

        when(userRepository.findById(10L)).thenReturn(Optional.of(existing));

        userService.deleteUser(10L);

        verify(userRepository).findById(10L);
        verify(userRepository).deleteById(10L);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void deleteUser_whenNotFound_shouldThrowNotFound() {
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(10L))
                .isInstanceOf(NotFoundException.class);

        verify(userRepository).findById(10L);
        verify(userRepository, never()).deleteById(anyLong());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void findById_whenExists_shouldReturnUser() {
        User existing = new User();
        existing.setId(10L);
        existing.setName("Ivan");
        existing.setEmail("ivan@mail.ru");

        when(userRepository.findById(10L)).thenReturn(Optional.of(existing));

        User result = userService.findById(10L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("Ivan");

        verify(userRepository).findById(10L);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void findById_whenNotFound_shouldThrowNotFound() {
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(10L))
                .isInstanceOf(NotFoundException.class);

        verify(userRepository).findById(10L);
        verifyNoMoreInteractions(userRepository);
    }
}
