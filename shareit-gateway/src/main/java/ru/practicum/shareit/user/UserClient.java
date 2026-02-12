package ru.practicum.shareit.user;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "shareit-server",
        url = "${shareit-server.url}",
        contextId = "userClient"
)
public interface UserClient extends UserApi {}

