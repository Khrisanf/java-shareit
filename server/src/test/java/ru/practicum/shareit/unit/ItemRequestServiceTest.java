package ru.practicum.shareit.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceTest {

    @Mock
    ItemRequestRepository itemRequestRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    ItemRepository itemRepository;

    @InjectMocks
    ItemRequestService itemRequestService;

    // ---------- helpers ----------

    private static User user(long id) {
        User u = new User();
        u.setId(id);
        u.setName("U" + id);
        u.setEmail("u" + id + "@mail.ru");
        return u;
    }

    private static ItemRequest request(long id) {
        ItemRequest r = new ItemRequest();
        r.setId(id);
        r.setDescription("need item");
        return r;
    }

    private static Item item(long id) {
        Item it = new Item();
        it.setId(id);
        it.setName("Item" + id);
        it.setDescription("Desc");
        return it;
    }

    // ---------- create ----------

    @Test
    void create_whenUserNotFound_shouldThrowNotFoundAndNotSave() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ItemRequest req = request(999L);

        assertThatThrownBy(() -> itemRequestService.create(req, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById(1L);
        verify(itemRequestRepository, never()).save(any());
        verifyNoMoreInteractions(userRepository, itemRequestRepository, itemRepository);
    }

    @Test
    void create_whenValid_shouldSetIdNullRequestorCreatedAndSave() {
        long userId = 1L;
        User u = user(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(u));
        when(itemRequestRepository.save(any(ItemRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        ItemRequest req = request(999L);

        LocalDateTime before = LocalDateTime.now();
        ItemRequest saved = itemRequestService.create(req, userId);
        LocalDateTime after = LocalDateTime.now();

        assertThat(saved.getId()).isNull();
        assertThat(saved.getRequestor()).isNotNull();
        assertThat(saved.getRequestor().getId()).isEqualTo(userId);
        assertThat(saved.getCreated()).isNotNull();
        assertThat(saved.getCreated()).isBetween(before.minusSeconds(1), after.plusSeconds(1));

        ArgumentCaptor<ItemRequest> captor = ArgumentCaptor.forClass(ItemRequest.class);
        verify(itemRequestRepository).save(captor.capture());
        ItemRequest toSave = captor.getValue();

        assertThat(toSave.getId()).isNull();
        assertThat(toSave.getRequestor().getId()).isEqualTo(userId);
        assertThat(toSave.getCreated()).isNotNull();
    }

    // ---------- findById ----------

    @Test
    void findById_whenUserNotExists_shouldThrowNotFoundAndNotQueryRequestRepo() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> itemRequestService.findById(1L, 10L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).existsById(1L);
        verify(itemRequestRepository, never()).findById(anyLong());
        verifyNoMoreInteractions(userRepository, itemRequestRepository, itemRepository);
    }

    @Test
    void findById_whenRequestNotFound_shouldThrowNotFound() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(itemRequestRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemRequestService.findById(1L, 10L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Request not found");

        verify(userRepository).existsById(1L);
        verify(itemRequestRepository).findById(10L);
    }

    @Test
    void findById_whenOk_shouldReturnRequest() {
        when(userRepository.existsById(1L)).thenReturn(true);

        ItemRequest r = request(10L);
        when(itemRequestRepository.findById(10L)).thenReturn(Optional.of(r));

        ItemRequest res = itemRequestService.findById(1L, 10L);

        assertThat(res.getId()).isEqualTo(10L);
        verify(itemRequestRepository).findById(10L);
    }

    // ---------- findItemsByRequestId ----------

    @Test
    void findItemsByRequestId_shouldDelegateToRepo() {
        when(itemRepository.findAllByItemRequest_Id(10L)).thenReturn(List.of(item(1L), item(2L)));

        List<Item> res = itemRequestService.findItemsByRequestId(10L);

        assertThat(res).hasSize(2);
        verify(itemRepository).findAllByItemRequest_Id(10L);
        verifyNoMoreInteractions(itemRepository);
    }

    // ---------- findAllByRequestor ----------

    @Test
    void findAllByRequestor_whenUserNotExists_shouldThrowNotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> itemRequestService.findAllByRequestor(1L))
                .isInstanceOf(NotFoundException.class);

        verify(userRepository).existsById(1L);
        verify(itemRequestRepository, never()).findAllByRequestor_IdOrderByCreatedDesc(anyLong());
    }

    @Test
    void findAllByRequestor_whenOk_shouldDelegate() {
        when(userRepository.existsById(1L)).thenReturn(true);

        List<ItemRequest> list = List.of(request(1L), request(2L));
        when(itemRequestRepository.findAllByRequestor_IdOrderByCreatedDesc(1L)).thenReturn(list);

        List<ItemRequest> res = itemRequestService.findAllByRequestor(1L);

        assertThat(res).hasSize(2);
        verify(itemRequestRepository).findAllByRequestor_IdOrderByCreatedDesc(1L);
    }

    // ---------- findAllItemsByRequestsIds ----------

    @Test
    void findAllItemsByRequestsIds_shouldDelegate() {
        Collection<Long> ids = List.of(10L, 20L);
        when(itemRepository.findAllByItemRequest_IdIn(ids)).thenReturn(List.of(item(1L)));

        List<Item> res = itemRequestService.findAllItemsByRequestsIds(ids);

        assertThat(res).hasSize(1);
        verify(itemRepository).findAllByItemRequest_IdIn(ids);
    }

    // ---------- findAllOtherRequests + pageable ----------

    @Test
    void findAllOtherRequests_whenUserNotExists_shouldThrowNotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> itemRequestService.findAllOtherRequests(1L, 0, 10))
                .isInstanceOf(NotFoundException.class);

        verify(userRepository).existsById(1L);
        verify(itemRequestRepository, never())
                .findAllByRequestor_IdNotOrderByCreatedDesc(anyLong(), any(Pageable.class));
    }

    @Test
    void findAllOtherRequests_whenSizeInvalid_shouldThrowIllegalArgument() {
        when(userRepository.existsById(1L)).thenReturn(true);

        assertThatThrownBy(() -> itemRequestService.findAllOtherRequests(1L, 0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("size must be > 0");

        verify(userRepository).existsById(1L);
        verify(itemRequestRepository, never())
                .findAllByRequestor_IdNotOrderByCreatedDesc(anyLong(), any(Pageable.class));
    }

    @Test
    void findAllOtherRequests_whenFromInvalid_shouldThrowIllegalArgument() {
        when(userRepository.existsById(1L)).thenReturn(true);

        assertThatThrownBy(() -> itemRequestService.findAllOtherRequests(1L, -1, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("from must be >= 0");

        verify(userRepository).existsById(1L);
        verify(itemRequestRepository, never())
                .findAllByRequestor_IdNotOrderByCreatedDesc(anyLong(), any(Pageable.class));
    }

    @Test
    void findAllOtherRequests_whenOk_shouldUseCorrectPageableAndReturnContent() {
        long userId = 1L;
        int from = 15;
        int size = 10;
        int expectedPage = from / size; // 1

        when(userRepository.existsById(userId)).thenReturn(true);

        List<ItemRequest> content = List.of(request(1L), request(2L));
        Page<ItemRequest> page = new PageImpl<>(content);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(itemRequestRepository.findAllByRequestor_IdNotOrderByCreatedDesc(eq(userId), pageableCaptor.capture()))
                .thenReturn(page);

        List<ItemRequest> res = itemRequestService.findAllOtherRequests(userId, from, size);

        assertThat(res).hasSize(2);

        Pageable used = pageableCaptor.getValue();
        assertThat(used.getPageNumber()).isEqualTo(expectedPage);
        assertThat(used.getPageSize()).isEqualTo(size);

        verify(itemRequestRepository)
                .findAllByRequestor_IdNotOrderByCreatedDesc(eq(userId), any(Pageable.class));
    }
}
