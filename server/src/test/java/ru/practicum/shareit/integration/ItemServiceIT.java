package ru.practicum.shareit.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ItemServiceIT {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createUser_persistsToDb_andCanBeFound() {
        User u = new User();
        u.setName("Ivan");
        u.setEmail("ivan@mail.ru");

        User created = userService.createUser(u);

        assertThat(created.getId()).isNotNull();

        User fromDb = userRepository.findById(created.getId()).orElseThrow();
        assertThat(fromDb.getName()).isEqualTo("Ivan");
        assertThat(fromDb.getEmail()).isEqualTo("ivan@mail.ru");
    }

    @Test
    void createUser_duplicateEmail_throwsConflict() {
        User u1 = new User();
        u1.setName("A");
        u1.setEmail("dup@mail.ru");
        userService.createUser(u1);

        User u2 = new User();
        u2.setName("B");
        u2.setEmail("dup@mail.ru");

        assertThatThrownBy(() -> userService.createUser(u2))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void updateUser_updatesPersistedEntity() {
        User u = new User();
        u.setName("Old");
        u.setEmail("old@mail.ru");
        User created = userService.createUser(u);

        User patch = new User();
        patch.setName("New");
        patch.setEmail("new@mail.ru");

        User updated = userService.updateUser(created.getId(), patch);

        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getName()).isEqualTo("New");
        assertThat(updated.getEmail()).isEqualTo("new@mail.ru");

        User fromDb = userRepository.findById(created.getId()).orElseThrow();
        assertThat(fromDb.getName()).isEqualTo("New");
        assertThat(fromDb.getEmail()).isEqualTo("new@mail.ru");
    }

    @Test
    void deleteUser_removesRowFromDb() {
        User u = new User();
        u.setName("ToDelete");
        u.setEmail("del@mail.ru");
        User created = userService.createUser(u);

        userService.deleteUser(created.getId());

        assertThat(userRepository.findById(created.getId())).isEmpty();
        assertThatThrownBy(() -> userService.findById(created.getId()))
                .isInstanceOf(NotFoundException.class);
    }
}
