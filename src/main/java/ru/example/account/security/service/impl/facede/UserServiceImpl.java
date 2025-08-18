package ru.example.account.security.service.impl.facede;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.security.service.facade.UserService;
import ru.example.account.user.repository.UserRepository;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Async
    @Override
    @Transactional(value = "securityTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void updateLastLoginAsync(Long userId, ZoneId zoneIdFromRequest) {

        this.userRepository.findByUserId(userId)
                .ifPresent(user -> user.setLastLogin(ZonedDateTime.now(zoneIdFromRequest)));
    }
}
