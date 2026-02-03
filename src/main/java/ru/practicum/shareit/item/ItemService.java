package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemDetails;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
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

    @Transactional(readOnly = true)
    public Item findById(Long ownerId, Long itemId) {
        getUserOrThrow(ownerId);
        return getItemOrThrow(itemId);
    }

    @Transactional(readOnly = true)
    public List<Item> getAllByOwner(Long ownerId) {
        getUserOrThrow(ownerId);
        return itemRepository.findAllByOwnerId(ownerId);
    }

    @Transactional(readOnly = true)
    public ItemDetails getItemWithComments(Long requesterId, Long itemId) {
        User requester = getUserOrThrow(requesterId);
        Item item = getItemOrThrow(itemId);

        List<Comment> comments = commentRepository
                .findItemComments(itemId);

        Booking last = null;
        Booking next = null;

        if (item.getOwner() != null && item.getOwner().getId() != null
                && item.getOwner().getId().equals(requester.getId())) {

            LocalDateTime now = LocalDateTime.now();

            last = bookingRepository.findLastApproved(itemId, now)
                    .stream()
                    .findFirst()
                    .orElse(null);

            next = bookingRepository.findNextApproved(itemId, now)
                    .stream()
                    .findFirst()
                    .orElse(null);
        }

        return new ItemDetails(item, last, next, comments);
    }

    @Transactional(readOnly = true)
    public List<ItemDetails> getAllByOwnerWithComments(Long ownerId) {
        getUserOrThrow(ownerId);

        List<Item> items = itemRepository.findAllByOwnerId(ownerId);
        if (items.isEmpty()) return List.of();

        List<Long> itemIds = items.stream().map(Item::getId).toList();

        Map<Long, List<Comment>> commentsByItemId = commentRepository
                .findCommentsForItems(itemIds)
                .stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId()));

        LocalDateTime now = LocalDateTime.now();

        return items.stream()
                .map(item -> {
                    List<Comment> comments = commentsByItemId.getOrDefault(item.getId(), List.of());

                    Booking last = bookingRepository.findLastApproved(item.getId(), now)
                            .stream().findFirst().orElse(null);

                    Booking next = bookingRepository.findNextApproved(item.getId(), now)
                            .stream().findFirst().orElse(null);

                    return new ItemDetails(item, last, next, comments);
                })
                .toList();
    }

    @Transactional
    public Comment addComment(Long authorId, Long itemId, CommentRequestDto request) {
        User author = getUserOrThrow(authorId);
        Item item = getItemOrThrow(itemId);

        LocalDateTime now = LocalDateTime.now();

        boolean ok = bookingRepository.hasFinishedBooking(itemId, authorId, Status.APPROVED, now);
        if (!ok) {
            throw new ValidationException("User has not finished approved booking for this item");
        }

        return commentRepository.save(Comment.builder()
                .text(request.text())
                .item(item)
                .author(author)
                .created(now)
                .build());
    }

    @Transactional(readOnly = true)
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
}
