package ru.practicum.shareit.request;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemRequestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "requestor", ignore = true)
    @Mapping(target = "created", ignore = true)
    ItemRequest toEntity(ItemRequestCreateDto createDto);

    @Mapping(target = "items", source = "items")
    ItemRequestDto toDto(ItemRequest itemRequest, List<ItemShortDto> items);
}
