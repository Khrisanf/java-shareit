package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.validate.OnCreate;
import ru.practicum.shareit.user.validate.OnUpdate;

import java.net.URI;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping
    public ResponseEntity<UserDto> createUser(
            @Validated(OnCreate.class) @RequestBody UserDto user
    ) {
        User createdUser = userService.createUser(userMapper.toEntity(user));
        return ResponseEntity.created(URI.create("/users/" + createdUser.getId())).body(userMapper.toDto(createdUser));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserDto> update(
            @PathVariable Long id,
            @Validated(OnUpdate.class) @RequestBody UserDto user
    ) {
        User updatedUser = userService.updateUser(id, userMapper.toEntity(user));
        return ResponseEntity.ok(userMapper.toDto(updatedUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id
    ) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUsers(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(userMapper.toDto(user));
    }

}
