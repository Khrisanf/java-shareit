package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.Status;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Transactional
    public Item createItem(Item item, Long ownerId, Long requestId) {
        item.setId(null);
        item.setOwner(getUserOrThrow(ownerId));
        if (requestId != null) {
            ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                    .orElseThrow(() -> new NotFoundException("Request not found: " + requestId));
            item.setItemRequest(itemRequest);
        }
        return itemRepository.save(item);
    }

    @Transactional
    public Item updateItem(Long ownerId, Long itemId, Item patch) {
        Item existingItem = getItemOrThrow(itemId);
        assertOwner(existingItem, ownerId, "updateUser");
        applyPatch(existingItem, patch);
        return itemRepository.save(existingItem);
    }

    @Transactional
    public void deleteItem(Long ownerId, Long itemId) {
        Item item = getItemOrThrow(itemId);
        assertOwner(item, ownerId, "deleteUser");
        itemRepository.delete(item);
    }

    @Transactional(readOnly = true)
    public ItemDetailsDto getItemWithComments(Long requesterId, Long itemId) {
        checkUserExists(requesterId);
        Item item = getItemOrThrow(itemId);
        List<Comment> comments = commentRepository.findByItemIdOrderByCreatedDesc(itemId);

        Booking last = null;
        Booking next = null;

        if (isOwner(item, requesterId)) {
            LocalDateTime now = LocalDateTime.now();
            last = findLastBooking(itemId, now);
            next = findNextBooking(itemId, now);
        }

        return new ItemDetailsDto(item, last, next, comments);
    }

    @Transactional(readOnly = true)
    public List<ItemDetailsDto> getAllByOwnerWithComments(Long ownerId) {
        checkUserExists(ownerId);

        List<Item> items = itemRepository.findAllByOwnerId(ownerId);
        if (items.isEmpty()) return List.of();

        List<Long> itemIds = items.stream().map(Item::getId).toList();

        Map<Long, List<Comment>> commentsByItemId = commentsByItemId(itemIds);

        LocalDateTime now = LocalDateTime.now();

        Map<Long, Booking> lastByItemId = lastApprovedByItemId(itemIds, now);
        Map<Long, Booking> nextByItemId = nextApprovedByItemId(itemIds, now);

        return items.stream()
                .map(item -> new ItemDetailsDto(
                        item,
                        lastByItemId.get(item.getId()),
                        nextByItemId.get(item.getId()),
                        commentsByItemId.getOrDefault(item.getId(), List.of())
                ))
                .toList();
    }

    @Transactional
    public Comment addComment(Long authorId, Long itemId, CommentRequestDto request) {
        User author = getUserOrThrow(authorId);
        Item item = getItemOrThrow(itemId);
        LocalDateTime now = LocalDateTime.now();

        validateCommentAuthor(authorId, itemId, now);

        return commentRepository.save(Comment.builder()
                .text(request.text())
                .item(item)
                .author(author)
                .created(now)
                .build());
    }

    @Transactional(readOnly = true)
    public List<Item> search(String text) {
        if (text == null || text.isBlank()) return List.of();
        return itemRepository.searchAvailableByText(text);
    }

    // --- Helpers ---

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("ItemResponseDto not found"));
    }

    private void checkUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }
    }

    private boolean isOwner(Item item, Long userId) {
        return item.getOwner() != null && item.getOwner().getId().equals(userId);
    }

    private void assertOwner(Item item, Long ownerId, String action) {
        if (!isOwner(item, ownerId)) {
            throw new ForbiddenException("Only owner can " + action + " item");
        }
    }

    private Booking findLastBooking(Long itemId, LocalDateTime now) {
        return bookingRepository.findFirstByItemIdAndStatusAndStartTimeBookingLessThanEqualOrderByStartTimeBookingDesc(itemId, Status.APPROVED, now)
                .orElse(null);
    }

    private Booking findNextBooking(Long itemId, LocalDateTime now) {
        return bookingRepository.findFirstByItemIdAndStatusAndStartTimeBookingGreaterThanOrderByStartTimeBookingAsc(itemId, Status.APPROVED, now)
                .orElse(null);
    }

    private void validateCommentAuthor(Long userId, Long itemId, LocalDateTime now) {
        boolean hasFinishedBooking = bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndTimeBookingLessThan(itemId, userId, Status.APPROVED, now);
        if (!hasFinishedBooking) {
            throw new ValidationException("User has not finished approved booking for this item");
        }
    }

    private void applyPatch(Item existingItem, Item patch) {
        if (patch.getName() != null) {
            if (patch.getName().isBlank()) throw new ValidationException("Name cannot be empty");
            existingItem.setName(patch.getName());
        }
        if (patch.getDescription() != null) {
            if (patch.getDescription().isBlank()) throw new ValidationException("Description cannot be empty");
            existingItem.setDescription(patch.getDescription());
        }
        if (patch.getIsAvailable() != null) {
            existingItem.setIsAvailable(patch.getIsAvailable());
        }
    }


    private Map<Long, List<Comment>> commentsByItemId(List<Long> itemIds) {
        return commentRepository.findByItemIdInOrderByCreatedDesc(itemIds)
                .stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId()));
    }

    private Map<Long, Booking> lastApprovedByItemId(List<Long> itemIds, LocalDateTime now) {
        List<Booking> candidates =
                bookingRepository.findByItemIdInAndStatusAndStartTimeBookingLessThanEqualOrderByItemIdAscStartTimeBookingDesc(
                        itemIds, Status.APPROVED, now
                );
        return firstBookingPerItemId(candidates);
    }

    private Map<Long, Booking> nextApprovedByItemId(List<Long> itemIds, LocalDateTime now) {
        List<Booking> candidates =
                bookingRepository.findByItemIdInAndStatusAndStartTimeBookingGreaterThanOrderByItemIdAscStartTimeBookingAsc(
                        itemIds, Status.APPROVED, now
                );
        return firstBookingPerItemId(candidates);
    }

    private Map<Long, Booking> firstBookingPerItemId(List<Booking> candidates) {
        Map<Long, Booking> byItemId = new HashMap<>();
        for (Booking b : candidates) {
            byItemId.putIfAbsent(b.getItem().getId(), b);
        }
        return byItemId;
    }
}