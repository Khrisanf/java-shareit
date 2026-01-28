package ru.practicum.shareit.item.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

/*TODO: add error-handlers*/

@Service
public class ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public ItemService(ItemRepository itemRepository, UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    public Item createItem(Item item, Long ownerId) {
        validateCreateItem(item);
        if (ownerId == null || ownerId <= 0) {
            throw new IllegalArgumentException("Owner id cannot be null or zero");
        }
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        item.setId(null);
        item.setOwner(owner);
        return itemRepository.save(item);
    }

    public Item updateItem(Long ownerId, Long itemId, Item patch) {
        validateUpdateItem(ownerId, itemId, patch);

        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        if(existingItem.getOwner() == null || existingItem.getOwner().getId() == null
        || !existingItem.getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("Owner id cannot be null or zero");
        }

        if (patch.getName() != null) {
            if (patch.getName().isBlank()) {
                throw new IllegalArgumentException("Name cannot be blank");
            }
            existingItem.setName(patch.getName());
        }

        if (patch.getDescription() != null) {
            if (patch.getDescription().isBlank()) {
                throw new IllegalArgumentException("Description cannot be blank");
            }
            existingItem.setDescription(patch.getDescription());
        }
        if (patch.getIsAvailable() != null) {
            existingItem.setIsAvailable(patch.getIsAvailable());
        }
        return itemRepository.save(existingItem);
    }

    public void deleteItem(Long itemId, Long ownerId) {
        if (ownerId == null || ownerId <= 0) {
            throw new IllegalArgumentException("Owner id cannot be null or zero");
        }
        if (itemId == null || itemId <= 0) {
            throw new IllegalArgumentException("Id cannot be null or negative!");
        }
        Item item = itemRepository.findById(itemId)
                        .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        if (!item.getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("Owner id mismatch");
        }

        itemRepository.delete(item);
    }

    public Item findById(Long ownerId, Long itemId) {
        if (ownerId == null || ownerId <= 0) {
            throw new IllegalArgumentException("Owner id cannot be null or zero");
        }
        if (itemId == null || itemId <= 0) {
            throw new IllegalArgumentException("Id cannot be null or negative!");
        }
        if (!userRepository.existsById(ownerId)) {
            throw new IllegalArgumentException("User not found");
        }
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found!"));
    }

    private void validateCreateItem(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }
        if (item.getName() == null || item.getName().isBlank()) {
            throw new IllegalArgumentException("Item's name cannot be null or empty");
        }
        if (item.getDescription() == null || item.getDescription().isBlank()) {
            throw new IllegalArgumentException("Item's description cannot be null or empty");
        }
        if (item.getIsAvailable() == null) {
            throw new IllegalArgumentException("Item's isAvailable cannot be null or empty");
        }
    }

    private void validateUpdateItem(Long ownerId, Long itemId, Item patch) {
        if (ownerId == null || ownerId <= 0) {
            throw new IllegalArgumentException("Owner id cannot be null or zero");
        }
        if (itemId == null || itemId <= 0) {
            throw new IllegalArgumentException("Item's id cannot be null or zero");
        }
        if (patch == null) {
            throw new IllegalArgumentException("Patch cannot be null");
        }
    }
}
