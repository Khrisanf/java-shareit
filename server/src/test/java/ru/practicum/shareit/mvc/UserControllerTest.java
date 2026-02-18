package ru.practicum.shareit.mvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.ShareItServerApp;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.handler.ErrorHandler;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@ContextConfiguration(classes = ShareItServerApp.class)
@Import(ErrorHandler.class)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    UserService userService;
    @MockBean
    UserMapper userMapper;

    @Test
    @DisplayName("POST /users -> 201, Location, body")
    void createUser_ok() throws Exception {
        UserDto requestDto = new UserDto(null, "Alice", "alice@mail.com");

        User entityFromMapper = new User();
        entityFromMapper.setId(null);
        entityFromMapper.setName("Alice");
        entityFromMapper.setEmail("alice@mail.com");

        User saved = new User();
        saved.setId(1L);
        saved.setName("Alice");
        saved.setEmail("alice@mail.com");

        UserDto responseDto = new UserDto(1L, "Alice", "alice@mail.com");

        when(userMapper.toEntity(any(UserDto.class))).thenReturn(entityFromMapper);
        when(userService.createUser(any(User.class))).thenReturn(saved);
        when(userMapper.toDto(saved)).thenReturn(responseDto);

        mockMvc.perform(post("/users")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string(LOCATION, endsWith("/users/1")))
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.email").value("alice@mail.com"));
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userService).createUser(captor.capture());
        assertEquals("Alice", captor.getValue().getName());
        assertEquals("alice@mail.com", captor.getValue().getEmail());

        verifyNoMoreInteractions(userService);
    }

    @Test
    @DisplayName("PATCH /users/{id} -> 200 + body")
    void update_ok() throws Exception {
        long id = 10L;

        UserDto patchDto = new UserDto(null, "NewName", null);

        User patchEntity = new User();
        patchEntity.setName("NewName");

        User updated = new User();
        updated.setId(id);
        updated.setName("NewName");
        updated.setEmail("old@mail.com");

        UserDto responseDto = new UserDto(id, "NewName", "old@mail.com");

        when(userMapper.toEntity(any(UserDto.class))).thenReturn(patchEntity);
        when(userService.updateUser(eq(id), any(User.class))).thenReturn(updated);
        when(userMapper.toDto(updated)).thenReturn(responseDto);

        mockMvc.perform(patch("/users/{id}", id)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("NewName"))
                .andExpect(jsonPath("$.email").value("old@mail.com"));
    }

    @Test
    @DisplayName("DELETE /users/{id} -> 204")
    void delete_ok() throws Exception {
        long id = 5L;
        doNothing().when(userService).deleteUser(id);

        mockMvc.perform(delete("/users/{id}", id))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(userService).deleteUser(id);
        verifyNoMoreInteractions(userService);
    }

    @Test
    @DisplayName("GET /users/{id} -> 200 + body")
    void get_ok() throws Exception {
        long id = 7L;

        User user = new User();
        user.setId(id);
        user.setName("Bob");
        user.setEmail("bob@mail.com");

        UserDto dto = new UserDto(id, "Bob", "bob@mail.com");

        when(userService.findById(id)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(dto);

        mockMvc.perform(get("/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.name").value("Bob"))
                .andExpect(jsonPath("$.email").value("bob@mail.com"));
    }

    @Test
    @DisplayName("GET /users/{id} -> 404 when service throws NotFoundException")
    void get_notFound() throws Exception {
        long id = 999L;
        when(userService.findById(id)).thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(get("/users/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("User not found")));
    }

    @Test
    @DisplayName("POST /users -> 409 when service throws ConflictException")
    void create_conflict() throws Exception {
        UserDto requestDto = new UserDto(null, "Alice", "alice@mail.com");

        User entityFromMapper = new User();
        entityFromMapper.setName("Alice");
        entityFromMapper.setEmail("alice@mail.com");

        when(userMapper.toEntity(any(UserDto.class))).thenReturn(entityFromMapper);
        when(userService.createUser(any(User.class))).thenThrow(new ConflictException("Email is already used"));

        mockMvc.perform(post("/users")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Email is already used")));
    }
}
