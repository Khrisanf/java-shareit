package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User createUser(User user) {
        validateCommonUser(user);

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ConflictException("Email is already used");
        }

        user.setId(null);
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, User patch) {
        validateUserId(id);
        validatePatch(patch);

        User existing = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (patch.getName() != null) {
            if (patch.getName().isBlank()) {
                throw new ValidationException("User's name cannot be empty");
            }
            existing.setName(patch.getName());
        }

        if (patch.getEmail() != null) {
            if (patch.getEmail().isBlank()) {
                throw new ValidationException("User's email cannot be empty");
            }
            if (userRepository.existsByEmailAndIdNot(patch.getEmail(), id)) {
                throw new ConflictException("Email is already used");
            }
            existing.setEmail(patch.getEmail());
        }

        return userRepository.save(existing);
    }

    @Transactional
    public void deleteUser(Long id) {
        validateUserId(id);

        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User not found");
        }

        userRepository.deleteById(id);
    }

    public User findById(Long id) {
        validateUserId(id);

        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private void validateCommonUser(User user) {
        if (user == null) {
            throw new ValidationException("User cannot be null");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            throw new ValidationException("User's name cannot be empty");
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ValidationException("User's email cannot be empty");
        }
    }

    private void validateUserId(Long id) {
        if (id == null || id <= 0) {
            throw new ValidationException("Invalid user id");
        }
    }

    private void validatePatch(User patch) {
        if (patch == null) {
            throw new ValidationException("Patch cannot be null");
        }
    }
}
