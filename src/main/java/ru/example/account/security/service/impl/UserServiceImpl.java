package ru.example.account.security.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.security.service.UserService;
import ru.example.account.user.repository.UserRepository;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void updateLastLoginAsync(Long userId, ZoneId zoneIdFromRequest) {

        this.userRepository.findByUserId(userId)
                .ifPresent(user -> user.setLastLogin(ZonedDateTime.now(zoneIdFromRequest)));
    }
}
