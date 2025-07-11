package ru.example.account.security.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.example.account.security.service.AccessTokenBlacklistService;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccessTokenBlacklistServiceImpl implements AccessTokenBlacklistService {

    @Override
    public void addToBlacklist(UUID sessionId, Duration duration) {

    }

    @Override
    public boolean isBlacklisted(UUID sessionId) {
        return false;
    }
}
