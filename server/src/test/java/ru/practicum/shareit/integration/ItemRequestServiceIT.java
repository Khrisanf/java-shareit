package ru.practicum.shareit.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemRequestServiceIT {

    @Autowired
    private ItemRequestService itemRequestService;
    @Autowired
    private ItemRequestRepository itemRequestRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;

    private User u1;
    private User u2;

    @BeforeEach
    void setUp() {
        u1 = new User();
        u1.setName("U1");
        u1.setEmail("u1@mail.ru");
        u1 = userRepository.save(u1);

        u2 = new User();
        u2.setName("U2");
        u2.setEmail("u2@mail.ru");
        u2 = userRepository.save(u2);
    }

    @Test
    void create_persists_andSetsRequestorAndCreated() {
        ItemRequest req = new ItemRequest();
        req.setDescription("need a drill");

        ItemRequest created = itemRequestService.create(req, u1.getId());

        assertNotNull(created.getId());
        assertNotNull(created.getRequestor());
        assertEquals(u1.getId(), created.getRequestor().getId());
        assertNotNull(created.getCreated());

        ItemRequest fromDb = itemRequestRepository.findById(created.getId()).orElseThrow();
        assertEquals("need a drill", fromDb.getDescription());
        assertEquals(u1.getId(), fromDb.getRequestor().getId());

        assertTrue(fromDb.getCreated().isAfter(LocalDateTime.now().minusMinutes(1)));
        assertTrue(fromDb.getCreated().isBefore(LocalDateTime.now().plusMinutes(1)));
    }

    @Test
    void findById_whenUserNotExists_throwsNotFound() {
        ItemRequest req = new ItemRequest();
        req.setDescription("x");
        ItemRequest created = itemRequestService.create(req, u1.getId());

        assertThrows(NotFoundException.class,
                () -> itemRequestService.findById(999_999L, created.getId()));
    }

    @Test
    void findById_whenOk_returnsRequest() {
        ItemRequest req = new ItemRequest();
        req.setDescription("hello");
        ItemRequest created = itemRequestService.create(req, u1.getId());

        ItemRequest found = itemRequestService.findById(u2.getId(), created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals("hello", found.getDescription());
        assertEquals(u1.getId(), found.getRequestor().getId());
    }

    @Test
    void findAllOtherRequests_returnsOnlyNotMine_withPaging() {
        ItemRequest r1 = new ItemRequest();
        r1.setDescription("r1");
        ItemRequest cr1 = itemRequestService.create(r1, u1.getId());

        ItemRequest r2 = new ItemRequest();
        r2.setDescription("r2");
        ItemRequest cr2 = itemRequestService.create(r2, u1.getId());

        ItemRequest r3 = new ItemRequest();
        r3.setDescription("r3");
        ItemRequest cr3 = itemRequestService.create(r3, u2.getId());

        List<ItemRequest> otherForU1 = itemRequestService.findAllOtherRequests(u1.getId(), 0, 10);
        assertEquals(1, otherForU1.size());
        assertEquals(cr3.getId(), otherForU1.get(0).getId());

        List<ItemRequest> otherForU2 = itemRequestService.findAllOtherRequests(u2.getId(), 0, 10);
        assertEquals(2, otherForU2.size());
        assertTrue(otherForU2.stream().anyMatch(r -> r.getId().equals(cr1.getId())));
        assertTrue(otherForU2.stream().anyMatch(r -> r.getId().equals(cr2.getId())));
    }

    @Test
    void findItemsByRequestId_returnsItemsLinkedToRequest() {
        ItemRequest req = new ItemRequest();
        req.setDescription("need item");
        ItemRequest created = itemRequestService.create(req, u1.getId());

        Item item = new Item();
        item.setName("Item1");
        item.setDescription("desc");
        item.setOwner(u2);
        item.setIsAvailable(true);
        item.setItemRequest(created);
        item = itemRepository.save(item);

        List<Item> items = itemRequestService.findItemsByRequestId(created.getId());

        assertEquals(1, items.size());
        assertEquals(item.getId(), items.get(0).getId());
    }
}
