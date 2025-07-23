package ru.example.account.security.service;

import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RevocationReason;

public interface SessionRevocationService {

   void revokeAndArchive(AuthSession sessionToRevoke, RevocationReason revocationReason);

   void revokeAllSessionsForUser(Long userId, RevocationReason reason);
}
