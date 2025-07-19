package ru.example.account.security.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.service.SessionRevocationService;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionRevocationServiceImpl implements SessionRevocationService {

    @Override
    public void revoke(AuthSession sessionToRevoke, RevocationReason reason) {

    }

    @Override
    public void revokeAllSessionsForUser(Long userId, RevocationReason reason) {

    }
}
