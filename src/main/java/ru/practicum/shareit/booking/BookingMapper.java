package ru.practicum.shareit.booking;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookingMapper {

    @Mapping(target = "bookingId", ignore = true)
    @Mapping(target = "item", ignore = true)
    @Mapping(target = "booker", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "startBooking", source = "start")
    @Mapping(target = "endBooking", source = "end")
    Booking toEntity(BookingRequestDto dto);

    @Mapping(target = "id", source = "bookingId")
    @Mapping(target = "start", source = "startBooking")
    @Mapping(target = "end", source = "endBooking")
    @Mapping(target = "status", expression = "java(booking.getStatus().name())")
    BookingResponseDto toResponseDto(Booking booking);

    // Вложенные маппинги (чтобы появились booker{id} и item{id,name})
    BookingResponseDto.Booker toBooker(User user);
    BookingResponseDto.Item toItem(Item item);
}

