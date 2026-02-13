package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestDtoAssembler;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestDtoAssemblerTest {

    @Mock
    ItemRequestService itemRequestService;
    @Mock
    ItemRequestMapper itemRequestMapper;
    @Mock
    ItemMapper itemMapper;

    @InjectMocks
    ItemRequestDtoAssembler itemRequestDtoAssembler;

    // --- helpers ---

    private static ItemRequest request(long id) {
        ItemRequest r = new ItemRequest();
        r.setId(id);
        return r;
    }

    private static Item itemWithRequest(ItemRequest r, long itemId) {
        Item i = new Item();
        i.setId(itemId);
        i.setItemRequest(r);
        return i;
    }

    // --- tests ---

    @Test
    void mapRequestsToDtos_whenNull_shouldReturnEmpty() {
        var res = itemRequestDtoAssembler.mapRequestsToDtos(null);

        assertThat(res).isEmpty();
        verifyNoInteractions(itemRequestService, itemRequestMapper, itemMapper);
    }

    @Test
    void mapRequestsToDtos_whenEmpty_shouldReturnEmpty() {
        var res = itemRequestDtoAssembler.mapRequestsToDtos(List.of());

        assertThat(res).isEmpty();
        verifyNoInteractions(itemRequestService, itemRequestMapper, itemMapper);
    }

    @Test
    void mapRequestsToDtos_shouldGroupItemsByRequestId_andMapEachRequest() {
        ItemRequest r1 = request(1L);
        ItemRequest r2 = request(2L);

        // items только для r1, а для r2 — пусто (проверим getOrDefault(.., List.of()))
        Item i11 = itemWithRequest(r1, 11L);
        Item i12 = itemWithRequest(r1, 12L);

        when(itemRequestService.findAllItemsByRequestsIds(List.of(1L, 2L)))
                .thenReturn(List.of(i11, i12));

        ItemShortDto s11 = new ItemShortDto(11L, "n11", 100L);
        ItemShortDto s12 = new ItemShortDto(12L, "n12", 100L);

        when(itemMapper.toItemShortDto(i11)).thenReturn(s11);
        when(itemMapper.toItemShortDto(i12)).thenReturn(s12);

        ItemRequestDto dto1 = mock(ItemRequestDto.class);
        ItemRequestDto dto2 = mock(ItemRequestDto.class);

        when(itemRequestMapper.toDto(eq(r1), anyList())).thenReturn(dto1);
        when(itemRequestMapper.toDto(eq(r2), anyList())).thenReturn(dto2);

        List<ItemRequestDto> result = itemRequestDtoAssembler.mapRequestsToDtos(List.of(r1, r2));

        assertThat(result).containsExactly(dto1, dto2);

        verify(itemRequestService).findAllItemsByRequestsIds(List.of(1L, 2L));

        verify(itemMapper).toItemShortDto(i11);
        verify(itemMapper).toItemShortDto(i12);

        ArgumentCaptor<List<ItemShortDto>> captor1 = ArgumentCaptor.forClass(List.class);
        verify(itemRequestMapper).toDto(eq(r1), captor1.capture());
        assertThat(captor1.getValue()).containsExactly(s11, s12);

        ArgumentCaptor<List<ItemShortDto>> captor2 = ArgumentCaptor.forClass(List.class);
        verify(itemRequestMapper).toDto(eq(r2), captor2.capture());
        assertThat(captor2.getValue()).isEmpty();

        verifyNoMoreInteractions(itemRequestService, itemRequestMapper, itemMapper);
    }
}
