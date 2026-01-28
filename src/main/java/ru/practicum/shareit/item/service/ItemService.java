package ru.practicum.shareit.item.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Service
public class ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public ItemService(ItemRepository itemRepository, UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Item createItem(Item item, Long ownerId) {
        validateCreateItem(item);
        validateUserId(ownerId);

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        item.setId(null);
        item.setOwner(owner);

        return itemRepository.save(item);
    }

    @Transactional
    public Item updateItem(Long ownerId, Long itemId, Item patch) {
        validateUserId(ownerId);
        validateItemId(itemId);
        validatePatch(patch);

        Item existingItem = getItemOrThrow(itemId);
        assertOwner(existingItem, ownerId, "updateUser");

        applyPatch(existingItem, patch);
        return itemRepository.save(existingItem);
    }

    @Transactional
    public void deleteItem(Long ownerId, Long itemId) {
        validateUserId(ownerId);
        validateItemId(itemId);

        Item item = getItemOrThrow(itemId);
        assertOwner(item, ownerId, "deleteUser");

        itemRepository.delete(item);
    }

    public Item findById(Long ownerId, Long itemId) {
        validateUserId(ownerId);
        validateItemId(itemId);

        requireUserExists(ownerId);
        return getItemOrThrow(itemId);
    }

    public List<Item> getAllByOwner(Long ownerId) {
        validateUserId(ownerId);
        requireUserExists(ownerId);
        return itemRepository.findAllByOwnerId(ownerId);
    }

    public List<Item> search(Long userId, String text) {
        validateUserId(userId);
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.searchAvailableByText(text);
    }

    private void requireUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }
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

    private void validateCreateItem(Item item) {
        if (item == null) {
            throw new ValidationException("Item cannot be null");
        }
        if (item.getName() == null || item.getName().isBlank()) {
            throw new ValidationException("Item's name cannot be null or empty");
        }
        if (item.getDescription() == null || item.getDescription().isBlank()) {
            throw new ValidationException("Item's description cannot be null or empty");
        }
        if (item.getIsAvailable() == null) {
            throw new ValidationException("Item's isAvailable cannot be null");
        }
    }

    private void validateUserId(Long ownerId) {
        if (ownerId == null || ownerId <= 0) {
            throw new ValidationException("Owner id cannot be null or <= 0");
        }
    }

    private void validateItemId(Long itemId) {
        if (itemId == null || itemId <= 0) {
            throw new ValidationException("Item id cannot be null or <= 0");
        }
    }

    private void validatePatch(Item patch) {
        if (patch == null) {
            throw new ValidationException("Patch cannot be null");
        }
    }
}
