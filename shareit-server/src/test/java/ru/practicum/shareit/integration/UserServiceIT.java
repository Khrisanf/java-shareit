package ru.practicum.shareit.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIT {

    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;

    @Test
    void createUser_persists_andRejectsDuplicateEmail() {
        User u1 = new User();
        u1.setName("A");
        u1.setEmail("a@mail.com");

        User saved1 = userService.createUser(u1);

        assertThat(saved1.getId()).isNotNull();
        assertThat(userRepository.findById(saved1.getId())).isPresent();

        User u2 = new User();
        u2.setName("B");
        u2.setEmail("a@mail.com");

        assertThatThrownBy(() -> userService.createUser(u2))
                .isInstanceOf(ConflictException.class);
    }
}
