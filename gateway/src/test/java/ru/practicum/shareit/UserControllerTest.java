package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.handler.ErrorHandler;
import ru.practicum.shareit.user.UserClient;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@ContextConfiguration(classes = ru.practicum.shareit.ShareItGatewayApp.class)
@Import(ErrorHandler.class)
class UserControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    UserClient userClient;

    @Test
    void createUser_valid_shouldProxyResponseFromClient() throws Exception {
        UserDto req = new UserDto(null, "Ann", "ann@mail.ru");
        UserDto resp = new UserDto(1L, "Ann", "ann@mail.ru");

        ResponseEntity<UserDto> clientResponse = ResponseEntity
                .status(HttpStatus.CREATED)
                .header(HttpHeaders.LOCATION, "/users/1")
                .body(resp);

        when(userClient.createUser(any(UserDto.class))).thenReturn(clientResponse);

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, containsString("/users/1")))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Ann")))
                .andExpect(jsonPath("$.email", is("ann@mail.ru")));

        verify(userClient, times(1)).createUser(any(UserDto.class));
        verifyNoMoreInteractions(userClient);
    }

    @Test
    void createUser_invalid_shouldReturn400_andNotCallClient() throws Exception {
        String badJson = """
                { "email": "ann@mail.ru" }
                """;

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userClient);
    }

    @Test
    void update_valid_shouldProxy() throws Exception {
        long id = 10L;
        UserDto req = new UserDto(null, "NewName", "new@mail.ru");
        UserDto resp = new UserDto(id, "NewName", "new@mail.ru");

        when(userClient.update(eq(id), any(UserDto.class)))
                .thenReturn(ResponseEntity.ok(resp));

        mvc.perform(patch("/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.name", is("NewName")))
                .andExpect(jsonPath("$.email", is("new@mail.ru")));

        verify(userClient).update(eq(id), any(UserDto.class));
        verifyNoMoreInteractions(userClient);
    }

    @Test
    void update_invalid_shouldReturn400_andNotCallClient() throws Exception {
        long id = 10L;

        String badJson = """
                { "name": "   " }
                """;

        mvc.perform(patch("/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userClient);
    }

    @Test
    void delete_shouldProxy() throws Exception {
        long id = 5L;
        when(userClient.deleteUser(id)).thenReturn(ResponseEntity.noContent().build());

        mvc.perform(delete("/users/{id}", id))
                .andExpect(status().isNoContent());

        verify(userClient).deleteUser(id);
        verifyNoMoreInteractions(userClient);
    }

    @Test
    void get_shouldProxy() throws Exception {
        long id = 7L;
        UserDto resp = new UserDto(id, "Bob", "bob@mail.ru");

        when(userClient.getUsers(id)).thenReturn(ResponseEntity.ok(resp));

        mvc.perform(get("/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(7)))
                .andExpect(jsonPath("$.name", is("Bob")))
                .andExpect(jsonPath("$.email", is("bob@mail.ru")));

        verify(userClient).getUsers(id);
        verifyNoMoreInteractions(userClient);
    }
}
