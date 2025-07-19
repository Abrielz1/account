package ru.example.account.security.service;

import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RevocationReason;

public interface SessionRevocationService {

   void revoke(AuthSession sessionToRevoke, RevocationReason reason);

   void revokeAllSessionsForUser(Long userId, RevocationReason reason);
}
