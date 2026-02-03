package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemWithCommentsDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.booking.Status;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    public Item createItem(Item item, Long ownerId) {
        User owner = getUserOrThrow(ownerId);

        item.setId(null);
        item.setOwner(owner);

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

    public Item findById(Long ownerId, Long itemId) {
        getUserOrThrow(ownerId);
        return getItemOrThrow(itemId);
    }

    public List<Item> getAllByOwner(Long ownerId) {
        getUserOrThrow(ownerId);
        return itemRepository.findAllByOwnerId(ownerId);
    }

    // НОВОЕ: GET /items/{itemId} с comments
    @Transactional(readOnly = true)
    public ItemWithCommentsDto getItemWithComments(Long requesterId, Long itemId) {
        User requester = getUserOrThrow(requesterId);
        Item item = getItemOrThrow(itemId);

        List<CommentDto> comments = commentRepository.findAllByItemIdOrderByCreatedDesc(itemId)
                .stream()
                .map(this::toCommentDto)
                .toList();

        ItemWithCommentsDto.BookingShort last = null;
        ItemWithCommentsDto.BookingShort next = null;

        if (item.getOwner() != null && item.getOwner().getId() != null
                && item.getOwner().getId().equals(requester.getId())) {

            LocalDateTime now = LocalDateTime.now();

            List<ru.practicum.shareit.booking.Booking> lastList = bookingRepository.findLastApproved(itemId, now);
            if (!lastList.isEmpty()) {
                var b = lastList.get(0);
                last = new ItemWithCommentsDto.BookingShort(b.getBookingId(), b.getBooker().getId());
            }

            List<ru.practicum.shareit.booking.Booking> nextList = bookingRepository.findNextApproved(itemId, now);
            if (!nextList.isEmpty()) {
                var b = nextList.get(0);
                next = new ItemWithCommentsDto.BookingShort(b.getBookingId(), b.getBooker().getId());
            }
        }

        return toItemWithCommentsDto(item, comments, last, next);
    }


    // НОВОЕ: GET /items для владельца с comments (без N+1)
    @Transactional(readOnly = true)
    public List<ItemWithCommentsDto> getAllByOwnerWithComments(Long ownerId) {
        getUserOrThrow(ownerId);

        List<Item> items = itemRepository.findAllByOwnerId(ownerId);
        if (items.isEmpty()) return List.of();

        List<Long> itemIds = items.stream().map(Item::getId).toList();

        Map<Long, List<CommentDto>> commentsByItemId = commentRepository
                .findAllByItemIdInOrderByCreatedDesc(itemIds)
                .stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        c -> c.getItem().getId(),
                        java.util.stream.Collectors.mapping(this::toCommentDto, java.util.stream.Collectors.toList())
                ));

        return items.stream()
                .map(item -> {
                    List<CommentDto> comments = commentsByItemId.getOrDefault(item.getId(), List.of());

                    LocalDateTime now = LocalDateTime.now();

                    ItemWithCommentsDto.BookingShort last = bookingRepository.findLastApproved(item.getId(), now)
                            .stream()
                            .findFirst()
                            .map(b -> new ItemWithCommentsDto.BookingShort(b.getBookingId(), b.getBooker().getId()))
                            .orElse(null);

                    ItemWithCommentsDto.BookingShort next = bookingRepository.findNextApproved(item.getId(), now)
                            .stream()
                            .findFirst()
                            .map(b -> new ItemWithCommentsDto.BookingShort(b.getBookingId(), b.getBooker().getId()))
                            .orElse(null);

                    return toItemWithCommentsDto(item, comments, last, next);
                })
                .toList();

    }

    // НОВОЕ: POST /items/{itemId}/comment
    @Transactional
    public CommentDto addComment(Long authorId, Long itemId, CommentRequestDto request) {
        User author = getUserOrThrow(authorId);
        Item item = getItemOrThrow(itemId);

        LocalDateTime now = LocalDateTime.now();

        boolean ok = bookingRepository.hasFinishedBooking(
                itemId, authorId, Status.APPROVED, now
        );

        if (!ok) {
            throw new ValidationException("User has not finished approved booking for this item");
        }

        Comment saved = commentRepository.save(Comment.builder()
                .text(request.text())
                .item(item)
                .author(author)
                .created(now)
                .build());

        return toCommentDto(saved);
    }


    public List<Item> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.searchAvailableByText(text);
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }


    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));
    }

    private void assertOwner(Item item, Long ownerId, String action) {
        if (item.getOwner() == null || item.getOwner().getId() == null) {
            throw new NotFoundException("Item owner not found");
        }
        if (!item.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Only owner can " + action + " item");
        }
    }

    private void applyPatch(Item existingItem, Item patch) {
        if (patch.getName() != null) {
            if (patch.getName().isBlank()) {
                throw new ValidationException("Item's name cannot be empty");
            }
            existingItem.setName(patch.getName());
        }

        if (patch.getDescription() != null) {
            if (patch.getDescription().isBlank()) {
                throw new ValidationException("Item's description cannot be empty");
            }
            existingItem.setDescription(patch.getDescription());
        }

        if (patch.getIsAvailable() != null) {
            existingItem.setIsAvailable(patch.getIsAvailable());
        }
    }

    private CommentDto toCommentDto(Comment c) {
        return new CommentDto(
                c.getId(),
                c.getText(),
                c.getAuthor().getName(),
                c.getCreated()
        );
    }

    private ItemWithCommentsDto toItemWithCommentsDto(
            Item item,
            List<CommentDto> comments,
            ItemWithCommentsDto.BookingShort last,
            ItemWithCommentsDto.BookingShort next
    ) {
        return new ItemWithCommentsDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getUseCount(),
                item.getIsAvailable(),
                last,
                next,
                comments
        );
    }
}
