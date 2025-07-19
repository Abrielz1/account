package ru.example.account.security.service;

import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RevocationReason;

public interface SessionCommandService {

    void archive(AuthSession session, RevocationReason reason);

    void archiveAllForUser(Long userId, RevocationReason reason);

}
