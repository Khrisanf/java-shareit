package ru.practicum.shareit.user.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

/*TODO: add error-handler*/

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User create(User user) {
        validateCommonUser(user);
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("User is already registered");
        }
        return userRepository.save(user);
    }

    public User update(Long id, User patch) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid user id");
        }

        User existing = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (patch.getName() != null) {
            if (patch.getName().isBlank()) {
                throw new IllegalArgumentException("User's name cannot be empty");
            }
            existing.setName(patch.getName());
        }

        if (patch.getEmail() != null) {
            if (patch.getEmail().isBlank()) {
                throw new IllegalArgumentException("User's email cannot be empty");
            }
            if (userRepository.existsByEmailAndIdNot(patch.getEmail(), id)) {
                throw new IllegalArgumentException("Email is already used by another user");
            }
            existing.setEmail(patch.getEmail());
        }

        return userRepository.save(existing);
    }


    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Id is null or negative value");
        }
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found");
        }
        userRepository.deleteById(id);
    }

    public User findById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Id is null or negative value");
        }
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public void validateCommonUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            throw new IllegalArgumentException("User's name cannot be empty");
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("User's email cannot be empty");
        }
    }
}
