package ru.example.account.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.example.account.user.entity.Client;
import ru.example.account.user.entity.User;
import ru.example.account.user.repository.ClientRepository;
import ru.example.account.user.repository.EmailDataRepository;
import ru.example.account.user.repository.PhoneDataRepository;
import ru.example.account.user.repository.UserRepository;
import ru.example.account.user.service.UserProcessor;
import ru.example.account.shared.exception.exceptions.UserNotFoundException;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProcessorImpl implements UserProcessor {

    private final ClientRepository clientRepository;

    private final UserRepository userRepository;

    private final EmailDataRepository emailDataRepository;

    private final PhoneDataRepository phoneDataRepository;

    @Override
    @Cacheable(value = "users", key = "#userId", unless = "#result == null")
    public User getUserByUserId(Long userId) {

        return clientRepository.findById(userId).orElseThrow(() -> {
            log.error("No user with such id: {}", userId);
            return new UserNotFoundException("No user with such id: %d".formatted(userId));
        });
    }

    @Override
    public boolean isFreeEmail(String newEmail) {
        return !emailDataRepository.checkUserByEmail(newEmail);
    }

    @Override
    public boolean isFreePhone(String newPhone) {
        return !phoneDataRepository.checkUserByPhone(newPhone);
    }

    @Override
    public boolean isFreeUsername(String Username) {
       return !userRepository.checkUserByUsername(Username);
    }

    @Override
    public Optional<Client> getReferrer(Long referrerId) {
        return clientRepository.findClientById(referrerId);
    }
}
