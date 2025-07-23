package ru.example.account.security.service;

import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.entity.RevokedSessionArchive;

public interface SessionRevocationService {

   void revoke(RevokedSessionArchive newRevokedTokenArchive);

   void revokeAllSessionsForUser(Long userId, RevocationReason reason);
}
