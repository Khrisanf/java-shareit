package ru.practicum.shareit.item;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "shareit-server",
        url = "${shareit-server.url}",
        contextId = "itemClient"
)
public interface ItemClient extends ItemApi {
}

