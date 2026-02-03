package ru.practicum.shareit.item;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    ItemDto toDto(Item item);

    Item toEntity(ItemDto dto);

    List<ItemDto> toDtos(List<Item> items);
}

