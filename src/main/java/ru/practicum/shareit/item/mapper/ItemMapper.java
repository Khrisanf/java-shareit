package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithCommentsDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemDetails;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CommentMapper.class})
public interface ItemMapper {

    @Mapping(target = "owner", ignore = true)
    Item toEntity(ItemDto dto);

    ItemDto toDto(Item item);

    @Mapping(target = "id", source = "details.item.id")
    @Mapping(target = "name", source = "details.item.name")
    @Mapping(target = "description", source = "details.item.description")
    @Mapping(target = "isAvailable", source = "details.item.isAvailable")
    @Mapping(target = "lastBooking", source = "details.lastBooking", qualifiedByName = "toBookingShortDto")
    @Mapping(target = "nextBooking", source = "details.nextBooking", qualifiedByName = "toBookingShortDto")
    @Mapping(target = "comments", source = "details.comments")
    ItemWithCommentsDto toItemWithCommentsDto(ItemDetails details);

    List<ItemWithCommentsDto> toItemWithCommentsDtoList(List<ItemDetails> details);

    @Named("toBookingShortDto")
    default BookingShortDto toBookingShortDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        return new BookingShortDto(booking.getBookingId(), booking.getBooker().getId());
    }
}