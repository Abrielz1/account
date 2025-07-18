package ru.example.account.security.service;

import java.util.UUID;

public interface IdGenerationService {

    UUID generateSessionId();

    String generateRefreshToken();
}
