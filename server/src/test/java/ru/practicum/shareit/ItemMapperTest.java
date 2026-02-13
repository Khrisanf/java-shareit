package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDetailsDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ItemMapperTest {

    private final ItemMapper mapper = mapperWithInjectedCommentMapper();

    private static ItemMapper mapperWithInjectedCommentMapper() {
        ItemMapper m = Mappers.getMapper(ItemMapper.class);
        Object impl = m;
        CommentMapper commentMapper = Mappers.getMapper(CommentMapper.class);

        try {
            Field f = impl.getClass().getDeclaredField("commentMapper");
            f.setAccessible(true);
            f.set(impl, commentMapper);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Не нашёл поле commentMapper в " + impl.getClass().getName(), e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }

        return m;
    }

    @Test
    void toBookingShortDto_shouldReturnNull_whenBookingIsNull() {
        assertThat(mapper.toBookingShortDto(null)).isNull();
    }

    @Test
    void toBookingShortDto_shouldMapBookingIdAndBookerId() {
        Booking booking = mock(Booking.class);
        User booker = mock(User.class);

        when(booking.getBookingId()).thenReturn(42L);
        when(booking.getBooker()).thenReturn(booker);
        when(booker.getId()).thenReturn(7L);

        assertThat(mapper.toBookingShortDto(booking))
                .isEqualTo(new BookingShortDto(42L, 7L));
    }

    @Test
    void toDto_shouldMapItemToItemDto() {
        Item item = mock(Item.class);

        when(item.getId()).thenReturn(1L);
        when(item.getName()).thenReturn("name");
        when(item.getDescription()).thenReturn("desc");
        when(item.getUseCount()).thenReturn(0L);
        when(item.getIsAvailable()).thenReturn(true);

        ItemDto dto = mapper.toDto(item);

        assertThat(dto).isEqualTo(new ItemDto(
                1L, "name", "desc", 0L, true, null
        ));
    }

    @Test
    void toEntity_shouldIgnoreOwnerAndItemRequest() {
        ItemDto dto = new ItemDto(
                1L, "name", "desc", 0L, true, 2L
        );

        Item entity = mapper.toEntity(dto);

        assertThat(entity.getOwner()).isNull();
        assertThat(entity.getItemRequest()).isNull();
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getName()).isEqualTo("name");
        assertThat(entity.getDescription()).isEqualTo("desc");
        assertThat(entity.getUseCount()).isEqualTo(0L);
        assertThat(entity.getIsAvailable()).isTrue();
        assertThat(entity.getId()).isEqualTo(1L);
    }


    @Test
    void toItemShortDto_shouldMapOwnerId() {
        Item item = mock(Item.class);
        User owner = mock(User.class);

        when(item.getId()).thenReturn(5L);
        when(item.getName()).thenReturn("drill");
        when(item.getOwner()).thenReturn(owner);
        when(owner.getId()).thenReturn(99L);

        ItemShortDto dto = mapper.toItemShortDto(item);

        assertThat(dto).isEqualTo(new ItemShortDto(5L, "drill", 99L));
    }

    @Test
    void toItemWithCommentsDtoList_shouldMapList() {
        Item item = mock(Item.class);
        when(item.getId()).thenReturn(1L);
        when(item.getName()).thenReturn("n");
        when(item.getDescription()).thenReturn("d");
        when(item.getUseCount()).thenReturn(0L);
        when(item.getIsAvailable()).thenReturn(true);

        ItemDetailsDto d1 = new ItemDetailsDto(item, null, null, List.of());
        ItemDetailsDto d2 = new ItemDetailsDto(item, null, null, List.of());

        var list = mapper.toItemWithCommentsDtoList(List.of(d1, d2));

        assertThat(list).hasSize(2);
    }
}
