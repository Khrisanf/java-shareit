package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.user.dto.UserDto;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {
    private final UserService userService;
    private final UserMapper userMapper;

    @Override
    public ResponseEntity<UserDto> createUser(UserDto user) {
        User createdUser = userService.createUser(userMapper.toEntity(user));
        return ResponseEntity.created(URI.create("/users/" + createdUser.getId()))
                .body(userMapper.toDto(createdUser));
    }

    @Override
    public ResponseEntity<UserDto> update(Long id, UserDto user) {
        User updatedUser = userService.updateUser(id, userMapper.toEntity(user));
        return ResponseEntity.ok(userMapper.toDto(updatedUser));
    }

    @Override
    public ResponseEntity<Void> deleteUser(Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<UserDto> getUsers(Long id) {
        User u = userService.findById(id);
        return ResponseEntity.ok(userMapper.toDto(u));
    }
}
