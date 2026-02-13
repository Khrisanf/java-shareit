package ru.practicum.shareit.request;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "shareit-server",
        url = "${shareit-server.url}",
        contextId = "itemRequestClient"
)
public interface ItemRequestClient extends ItemRequestApi {
}
