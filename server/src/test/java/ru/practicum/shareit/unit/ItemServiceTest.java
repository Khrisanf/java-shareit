package ru.practicum.shareit.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.Status;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDetailsDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    ItemRepository itemRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    CommentRepository commentRepository;
    @Mock
    BookingRepository bookingRepository;
    @Mock
    ItemRequestRepository itemRequestRepository;

    @InjectMocks
    ItemService itemService;

    // --- helpers ---

    private static User user(long id) {
        User u = new User();
        u.setId(id);
        u.setName("U" + id);
        u.setEmail("u" + id + "@mail.ru");
        return u;
    }

    private static Item item(long id, long ownerId) {
        Item i = new Item();
        i.setId(id);
        i.setName("Item" + id);
        i.setDescription("Desc" + id);
        i.setIsAvailable(true);
        i.setOwner(user(ownerId));
        return i;
    }

    private static Booking booking(long bookingId, long itemId) {
        Booking b = new Booking();
        b.setBookingId(bookingId);
        Item it = new Item();
        it.setId(itemId);
        b.setItem(it);
        b.setStatus(Status.APPROVED);
        return b;
    }

    // --- createItem ---

    @Test
    void createItem_whenNoRequest_shouldSetNullIdOwnerAndSave() {
        long ownerId = 1L;
        Item input = new Item();
        input.setId(999L);
        input.setName("Drill");
        input.setDescription("Nice");
        input.setIsAvailable(true);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(user(ownerId)));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        Item saved = itemService.createItem(input, ownerId, null);

        assertThat(saved.getId()).isNull();
        assertThat(saved.getOwner()).isNotNull();
        assertThat(saved.getOwner().getId()).isEqualTo(ownerId);
        assertThat(saved.getItemRequest()).isNull();

        verify(userRepository).findById(ownerId);
        verify(itemRepository).save(any(Item.class));
        verifyNoMoreInteractions(itemRepository, userRepository, itemRequestRepository, commentRepository, bookingRepository);
    }

    @Test
    void createItem_whenRequestProvided_shouldAttachRequest() {
        long ownerId = 1L;
        long requestId = 50L;

        ItemRequest req = new ItemRequest();
        req.setId(requestId);

        Item input = new Item();
        input.setName("Drill");
        input.setDescription("Nice");
        input.setIsAvailable(true);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(user(ownerId)));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(req));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        Item saved = itemService.createItem(input, ownerId, requestId);

        assertThat(saved.getOwner().getId()).isEqualTo(ownerId);
        assertThat(saved.getItemRequest()).isNotNull();
        assertThat(saved.getItemRequest().getId()).isEqualTo(requestId);

        verify(userRepository).findById(ownerId);
        verify(itemRequestRepository).findById(requestId);
        verify(itemRepository).save(any(Item.class));
        verifyNoMoreInteractions(itemRepository, userRepository, itemRequestRepository, commentRepository, bookingRepository);
    }

    @Test
    void createItem_whenRequestNotFound_shouldThrowNotFound() {
        long ownerId = 1L;
        long requestId = 50L;

        Item input = new Item();
        input.setName("Drill");
        input.setDescription("Nice");
        input.setIsAvailable(true);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(user(ownerId)));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.createItem(input, ownerId, requestId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Request not found");

        verify(userRepository).findById(ownerId);
        verify(itemRequestRepository).findById(requestId);
        verify(itemRepository, never()).save(any());
        verifyNoMoreInteractions(itemRepository, userRepository, itemRequestRepository, commentRepository, bookingRepository);
    }

    // --- updateItem ---

    @Test
    void updateItem_whenNotOwner_shouldThrowForbidden() {
        long ownerId = 1L;
        long itemId = 10L;

        Item existing = item(itemId, 999L);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existing));

        Item patch = new Item();
        patch.setName("NewName");

        assertThatThrownBy(() -> itemService.updateItem(ownerId, itemId, patch))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Only owner");

        verify(itemRepository).findById(itemId);
        verify(itemRepository, never()).save(any());
        verifyNoMoreInteractions(itemRepository, userRepository, itemRequestRepository, commentRepository, bookingRepository);
    }

    @Test
    void updateItem_whenPatchNameBlank_shouldThrowValidation() {
        long ownerId = 1L;
        long itemId = 10L;

        Item existing = item(itemId, ownerId);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existing));

        Item patch = new Item();
        patch.setName("   ");

        assertThatThrownBy(() -> itemService.updateItem(ownerId, itemId, patch))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Name cannot be empty");

        verify(itemRepository).findById(itemId);
        verify(itemRepository, never()).save(any());
        verifyNoMoreInteractions(itemRepository, userRepository, itemRequestRepository, commentRepository, bookingRepository);
    }

    @Test
    void updateItem_whenPatchValid_shouldApplyAndSave() {
        long ownerId = 1L;
        long itemId = 10L;

        Item existing = item(itemId, ownerId);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existing));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        Item patch = new Item();
        patch.setDescription("NewDesc");
        patch.setIsAvailable(false);

        Item updated = itemService.updateItem(ownerId, itemId, patch);

        assertThat(updated.getDescription()).isEqualTo("NewDesc");
        assertThat(updated.getIsAvailable()).isFalse();

        ArgumentCaptor<Item> captor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(itemId);

        verify(itemRepository).findById(itemId);
        verifyNoMoreInteractions(itemRepository, userRepository, itemRequestRepository, commentRepository, bookingRepository);
    }

    // --- deleteItem ---

    @Test
    void deleteItem_whenOwner_shouldDelete() {
        long ownerId = 1L;
        long itemId = 10L;

        Item existing = item(itemId, ownerId);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existing));

        itemService.deleteItem(ownerId, itemId);

        verify(itemRepository).findById(itemId);
        verify(itemRepository).delete(existing);
        verifyNoMoreInteractions(itemRepository, userRepository, itemRequestRepository, commentRepository, bookingRepository);
    }

    // --- getItemWithComments ---

    @Test
    void getItemWithComments_whenNonOwner_shouldNotQueryBookings() {
        long requesterId = 2L;
        long itemId = 10L;

        Item existing = item(itemId, 1L);

        when(userRepository.existsById(requesterId)).thenReturn(true);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existing));
        when(commentRepository.findByItemIdOrderByCreatedDesc(itemId)).thenReturn(List.of());

        ItemDetailsDto dto = itemService.getItemWithComments(requesterId, itemId);

        assertThat(dto.item().getId()).isEqualTo(itemId);
        assertThat(dto.lastBooking()).isNull();
        assertThat(dto.nextBooking()).isNull();

        verify(userRepository).existsById(requesterId);
        verify(itemRepository).findById(itemId);
        verify(commentRepository).findByItemIdOrderByCreatedDesc(itemId);
        verify(bookingRepository, never()).findFirstByItemIdAndStatusAndStartTimeBookingLessThanEqualOrderByStartTimeBookingDesc(anyLong(), any(), any());
        verify(bookingRepository, never()).findFirstByItemIdAndStatusAndStartTimeBookingGreaterThanOrderByStartTimeBookingAsc(anyLong(), any(), any());
        verifyNoMoreInteractions(itemRepository, userRepository, itemRequestRepository, commentRepository, bookingRepository);
    }

    @Test
    void getItemWithComments_whenOwner_shouldIncludeLastAndNextBookings() {
        long ownerId = 1L;
        long itemId = 10L;

        Item existing = item(itemId, ownerId);
        Booking last = booking(100L, itemId);
        Booking next = booking(200L, itemId);

        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existing));
        when(commentRepository.findByItemIdOrderByCreatedDesc(itemId)).thenReturn(List.of());

        when(bookingRepository
                .findFirstByItemIdAndStatusAndStartTimeBookingLessThanEqualOrderByStartTimeBookingDesc(
                        eq(itemId), eq(Status.APPROVED), any(LocalDateTime.class)
                )).thenReturn(Optional.of(last));

        when(bookingRepository
                .findFirstByItemIdAndStatusAndStartTimeBookingGreaterThanOrderByStartTimeBookingAsc(
                        eq(itemId), eq(Status.APPROVED), any(LocalDateTime.class)
                )).thenReturn(Optional.of(next));

        ItemDetailsDto dto = itemService.getItemWithComments(ownerId, itemId);

        assertThat(dto.lastBooking()).isNotNull();
        assertThat(dto.nextBooking()).isNotNull();
        assertThat(dto.lastBooking().getBookingId()).isEqualTo(100L);
        assertThat(dto.nextBooking().getBookingId()).isEqualTo(200L);

        verify(userRepository).existsById(ownerId);
        verify(itemRepository).findById(itemId);
        verify(commentRepository).findByItemIdOrderByCreatedDesc(itemId);
        verify(bookingRepository).findFirstByItemIdAndStatusAndStartTimeBookingLessThanEqualOrderByStartTimeBookingDesc(eq(itemId), eq(Status.APPROVED), any(LocalDateTime.class));
        verify(bookingRepository).findFirstByItemIdAndStatusAndStartTimeBookingGreaterThanOrderByStartTimeBookingAsc(eq(itemId), eq(Status.APPROVED), any(LocalDateTime.class));
        verifyNoMoreInteractions(itemRepository, userRepository, itemRequestRepository, commentRepository, bookingRepository);
    }

    // --- getAllByOwnerWithComments ---

    @Test
    void getAllByOwnerWithComments_whenNoItems_shouldReturnEmptyAndSkipHeavyQueries() {
        long ownerId = 1L;

        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(itemRepository.findAllByOwnerId(ownerId)).thenReturn(List.of());

        List<ItemDetailsDto> result = itemService.getAllByOwnerWithComments(ownerId);

        assertThat(result).isEmpty();

        verify(userRepository).existsById(ownerId);
        verify(itemRepository).findAllByOwnerId(ownerId);
        verifyNoMoreInteractions(itemRepository, userRepository, itemRequestRepository, commentRepository, bookingRepository);
    }

    @Test
    void getAllByOwnerWithComments_shouldGroupCommentsAndPickFirstBookingPerItem() {
        long ownerId = 1L;

        Item i1 = item(10L, ownerId);
        Item i2 = item(20L, ownerId);

        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(itemRepository.findAllByOwnerId(ownerId)).thenReturn(List.of(i1, i2));

        Comment c1 = new Comment();
        c1.setItem(i1);
        Comment c2 = new Comment();
        c2.setItem(i1);

        when(commentRepository.findByItemIdInOrderByCreatedDesc(List.of(10L, 20L)))
                .thenReturn(List.of(c1, c2));

        Booking last1 = booking(101L, 10L);
        Booking last1older = booking(102L, 10L);
        Booking last2 = booking(201L, 20L);

        when(bookingRepository.findByItemIdInAndStatusAndStartTimeBookingLessThanEqualOrderByItemIdAscStartTimeBookingDesc(
                eq(List.of(10L, 20L)), eq(Status.APPROVED), any(LocalDateTime.class)
        )).thenReturn(List.of(last1, last1older, last2));

        Booking next1 = booking(301L, 10L);
        Booking next2 = booking(401L, 20L);
        Booking next2later = booking(402L, 20L);

        when(bookingRepository.findByItemIdInAndStatusAndStartTimeBookingGreaterThanOrderByItemIdAscStartTimeBookingAsc(
                eq(List.of(10L, 20L)), eq(Status.APPROVED), any(LocalDateTime.class)
        )).thenReturn(List.of(next1, next2, next2later));

        List<ItemDetailsDto> result = itemService.getAllByOwnerWithComments(ownerId);

        assertThat(result).hasSize(2);

        ItemDetailsDto dto1 = result.stream().filter(d -> d.item().getId().equals(10L)).findFirst().orElseThrow();
        ItemDetailsDto dto2 = result.stream().filter(d -> d.item().getId().equals(20L)).findFirst().orElseThrow();

        assertThat(dto1.comments()).hasSize(2);
        assertThat(dto2.comments()).isEmpty();

        assertThat(dto1.lastBooking().getBookingId()).isEqualTo(101L);
        assertThat(dto2.lastBooking().getBookingId()).isEqualTo(201L);

        assertThat(dto1.nextBooking().getBookingId()).isEqualTo(301L);
        assertThat(dto2.nextBooking().getBookingId()).isEqualTo(401L);

        verify(userRepository).existsById(ownerId);
        verify(itemRepository).findAllByOwnerId(ownerId);
        verify(commentRepository).findByItemIdInOrderByCreatedDesc(List.of(10L, 20L));
        verify(bookingRepository).findByItemIdInAndStatusAndStartTimeBookingLessThanEqualOrderByItemIdAscStartTimeBookingDesc(eq(List.of(10L, 20L)), eq(Status.APPROVED), any(LocalDateTime.class));
        verify(bookingRepository).findByItemIdInAndStatusAndStartTimeBookingGreaterThanOrderByItemIdAscStartTimeBookingAsc(eq(List.of(10L, 20L)), eq(Status.APPROVED), any(LocalDateTime.class));
        verifyNoMoreInteractions(itemRepository, userRepository, itemRequestRepository, commentRepository, bookingRepository);
    }

    // --- addComment ---

    @Test
    void addComment_whenNoFinishedApprovedBooking_shouldThrowValidation() {
        long authorId = 1L;
        long itemId = 10L;

        when(userRepository.findById(authorId)).thenReturn(Optional.of(user(authorId)));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item(itemId, 2L)));

        when(bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndTimeBookingLessThan(
                eq(itemId), eq(authorId), eq(Status.APPROVED), any(LocalDateTime.class)
        )).thenReturn(false);

        CommentRequestDto req = new CommentRequestDto("hi");

        assertThatThrownBy(() -> itemService.addComment(authorId, itemId, req))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("has not finished");

        verify(commentRepository, never()).save(any());
    }

    @Test
    void addComment_whenFinishedApprovedBooking_shouldSaveCommentWithFields() {
        long authorId = 1L;
        long itemId = 10L;

        User author = user(authorId);
        Item it = item(itemId, 2L);

        when(userRepository.findById(authorId)).thenReturn(Optional.of(author));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(it));

        when(bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndTimeBookingLessThan(
                eq(itemId), eq(authorId), eq(Status.APPROVED), any(LocalDateTime.class)
        )).thenReturn(true);

        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> inv.getArgument(0));

        CommentRequestDto req = new CommentRequestDto("hello");
        LocalDateTime before = LocalDateTime.now();

        Comment saved = itemService.addComment(authorId, itemId, req);

        LocalDateTime after = LocalDateTime.now();

        assertThat(saved.getText()).isEqualTo("hello");
        assertThat(saved.getAuthor().getId()).isEqualTo(authorId);
        assertThat(saved.getItem().getId()).isEqualTo(itemId);
        assertThat(saved.getCreated()).isNotNull();
        assertThat(saved.getCreated()).isAfterOrEqualTo(before).isBeforeOrEqualTo(after.plusSeconds(1));

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());
        Comment toSave = captor.getValue();
        assertThat(toSave.getText()).isEqualTo("hello");
        assertThat(toSave.getAuthor().getId()).isEqualTo(authorId);
        assertThat(toSave.getItem().getId()).isEqualTo(itemId);
        assertThat(toSave.getCreated()).isNotNull();
    }

    // --- search ---

    @Test
    void search_whenBlank_shouldReturnEmptyAndNotCallRepo() {
        List<Item> res1 = itemService.search(null);
        List<Item> res2 = itemService.search("   ");

        assertThat(res1).isEmpty();
        assertThat(res2).isEmpty();

        verify(itemRepository, never()).searchAvailableByText(anyString());
    }

    @Test
    void search_whenTextProvided_shouldCallRepo() {
        when(itemRepository.searchAvailableByText("drill"))
                .thenReturn(List.of(item(1L, 1L)));

        List<Item> result = itemService.search("drill");

        assertThat(result).hasSize(1);

        verify(itemRepository).searchAvailableByText("drill");
        verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void updateItem_whenPatchNameValid_shouldApplyAndSave() {
        long ownerId = 1L;
        long itemId = 10L;

        Item existing = item(itemId, ownerId);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existing));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        Item patch = new Item();
        patch.setName("NewName");

        Item updated = itemService.updateItem(ownerId, itemId, patch);

        assertThat(updated.getName()).isEqualTo("NewName");

        verify(itemRepository).findById(itemId);
        verify(itemRepository).save(any(Item.class));
        verifyNoMoreInteractions(itemRepository, userRepository, itemRequestRepository, commentRepository, bookingRepository);
    }

    @Test
    void updateItem_whenPatchDescriptionBlank_shouldThrowValidation() {
        long ownerId = 1L;
        long itemId = 10L;

        Item existing = item(itemId, ownerId);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existing));

        Item patch = new Item();
        patch.setDescription("   ");

        assertThatThrownBy(() -> itemService.updateItem(ownerId, itemId, patch))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Description cannot be empty");

        verify(itemRepository).findById(itemId);
        verify(itemRepository, never()).save(any());
        verifyNoMoreInteractions(itemRepository, userRepository, itemRequestRepository, commentRepository, bookingRepository);
    }

    @Test
    void getItemWithComments_whenUserNotExists_shouldThrowNotFound() {
        long requesterId = 1L;
        long itemId = 10L;

        when(userRepository.existsById(requesterId)).thenReturn(false);

        assertThatThrownBy(() -> itemService.getItemWithComments(requesterId, itemId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).existsById(requesterId);
        verifyNoInteractions(itemRepository, commentRepository, bookingRepository, itemRequestRepository);
    }

    @Test
    void getAllByOwnerWithComments_whenUserNotExists_shouldThrowNotFound() {
        long ownerId = 1L;

        when(userRepository.existsById(ownerId)).thenReturn(false);

        assertThatThrownBy(() -> itemService.getAllByOwnerWithComments(ownerId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).existsById(ownerId);
        verifyNoInteractions(itemRepository, commentRepository, bookingRepository, itemRequestRepository);
    }
}
