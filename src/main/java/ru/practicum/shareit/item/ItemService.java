package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

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
