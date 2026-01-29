package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ConflictException("Email is already used");
        }

        user.setId(null);
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, User patch) {

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
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User not found");
        }

        userRepository.deleteById(id);
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

}
