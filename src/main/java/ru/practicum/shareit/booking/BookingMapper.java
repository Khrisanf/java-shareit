package ru.practicum.shareit.booking;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.BookerDto;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookingMapper {

    @Mapping(target = "bookingId", ignore = true)
    @Mapping(target = "item", ignore = true)
    @Mapping(target = "booker", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "startTimeBooking", source = "start")
    @Mapping(target = "endTimeBooking", source = "end")
    Booking toEntity(BookingRequestDto dto);

    @Mapping(target = "id", source = "bookingId")
    @Mapping(target = "start", source = "startTimeBooking")
    @Mapping(target = "end", source = "endTimeBooking")
    @Mapping(target = "status", expression = "java(booking.getStatus().name())")
    BookingResponseDto toResponseDto(Booking booking);

    BookerDto toBooker(User user);

    ItemResponseDto toItem(Item item);
}

