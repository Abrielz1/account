package ru.example.account.security.service;

import java.time.ZoneId;

public interface UserService {
    void updateLastLoginAsync(Long id, ZoneId zoneIdFromRequest);
}
