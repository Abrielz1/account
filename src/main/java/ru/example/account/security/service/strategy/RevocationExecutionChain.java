package ru.example.account.security.service.strategy;

import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.entity.SessionStatus;

public interface RevocationExecutionChain {

    void execute(AuthSession session, SessionStatus status, RevocationReason reason);
}
