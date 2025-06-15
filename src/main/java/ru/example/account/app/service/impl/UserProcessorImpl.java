package ru.example.account.app.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.example.account.app.entity.User;
import ru.example.account.app.repository.UserRepository;
import ru.example.account.app.service.UserProcessor;
import ru.example.account.util.exception.exceptions.UserNotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProcessorImpl implements UserProcessor {

    private final UserRepository userRepository;

    @Override
    @Cacheable(value = "users", key = "#userId", unless = "#result == null")
    public User getUserByUserId(Long userId) {

        return userRepository.findById(userId).orElseThrow(() -> {
            log.error("No user with such id: {}", userId);
            return new UserNotFoundException("No user with such id: %d".formatted(userId));
        });
    }

    @Override
    public boolean isFreeEmail(String newEmail) {
        return !userRepository.existsEmails(newEmail);
    }

    @Override
    public boolean isFreePhone(String newPhone) {
        return !userRepository.existsByPhones(newPhone);
    }
}
