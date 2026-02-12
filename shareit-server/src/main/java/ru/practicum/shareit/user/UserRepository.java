package ru.practicum.shareit.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    // занят кем-то другим
    boolean existsByEmailAndIdNot(String email, Long id);
}
