package ru.example.account.security.service.worker;

import java.util.UUID;

public interface IdGenerationService {

    UUID generateSessionId();

    String generateUniqueTokenId();
}
