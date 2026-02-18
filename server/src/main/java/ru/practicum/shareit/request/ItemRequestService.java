package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public ItemRequest create(ItemRequest request, Long userId) {
        request.setId(null);
        request.setRequestor(getUserOrThrow(userId));
        request.setCreated(LocalDateTime.now());
        return itemRequestRepository.save(request);
    }

    public ItemRequest findById(Long userId, Long requestId) {
        assertUserExists(userId);
        return itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found: " + requestId));
    }

    public List<Item> findItemsByRequestId(Long requestId) {
        return itemRepository.findAllByItemRequest_Id(requestId);
    }

    public List<ItemRequest> findAllByRequestor(Long userId) {
        assertUserExists(userId);
        return itemRequestRepository.findAllByRequestor_IdOrderByCreatedDesc(userId);
    }

    public List<Item> findAllItemsByRequestsIds(Collection<Long> requestIds) {
        return itemRepository.findAllByItemRequest_IdIn(requestIds);
    }

    @Transactional(readOnly = true)
    public List<ItemRequest> findAllOtherRequests(Long userId, int from, int size) {
        assertUserExists(userId);
        Pageable pageable = toPageable(from, size);
        return itemRequestRepository
                .findAllByRequestor_IdNotOrderByCreatedDesc(userId, pageable)
                .getContent();
    }

    // ===== Helpers =====

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    private void assertUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found: " + userId);
        }
    }

    private Pageable toPageable(int from, int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("size must be > 0");
        }
        if (from < 0) {
            throw new IllegalArgumentException("from must be >= 0");
        }
        int page = from / size;
        return PageRequest.of(page, size);
    }
}
