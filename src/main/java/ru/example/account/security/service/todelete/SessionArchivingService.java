package ru.example.account.security.service.todelete;


import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RevocationReason;

public interface SessionArchivingService {
    void archive(AuthSession session, RevocationReason reason);
}
