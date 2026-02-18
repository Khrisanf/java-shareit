package ru.practicum.shareit.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.practicum.shareit.user.dto.UserDto;

public interface UserApi {

    @RequestMapping(method = RequestMethod.POST, value = "/users")
    ResponseEntity<UserDto> createUser(@RequestBody UserDto user);

    @RequestMapping(method = RequestMethod.PATCH, value = "/users/{id}")
    ResponseEntity<UserDto> update(@PathVariable("id") Long id, @RequestBody UserDto user);

    @RequestMapping(method = RequestMethod.DELETE, value = "/users/{id}")
    ResponseEntity<Void> deleteUser(@PathVariable("id") Long id);

    @RequestMapping(method = RequestMethod.GET, value = "/users/{id}")
    ResponseEntity<UserDto> getUsers(@PathVariable("id") Long id);
}
