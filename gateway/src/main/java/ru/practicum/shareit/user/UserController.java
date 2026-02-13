package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.validate.OnCreate;
import ru.practicum.shareit.validate.OnUpdate;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserClient userClient;

    @Override
    public ResponseEntity<UserDto> createUser(@Validated(OnCreate.class) UserDto user) {
        return userClient.createUser(user);
    }

    @Override
    public ResponseEntity<UserDto> update(Long id, @Validated(OnUpdate.class) UserDto user) {
        return userClient.update(id, user);
    }

    @Override
    public ResponseEntity<Void> deleteUser(Long id) {
        return userClient.deleteUser(id);
    }

    @Override
    public ResponseEntity<UserDto> getUsers(Long id) {
        return userClient.getUsers(id);
    }
}
