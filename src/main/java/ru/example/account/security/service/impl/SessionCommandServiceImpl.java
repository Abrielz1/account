package ru.example.account.security.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.entity.RevokedClientData;
import ru.example.account.security.entity.SessionStatus;
import ru.example.account.security.repository.RevokedDataRepository;
import ru.example.account.security.service.worker.IdGenerationService;
import ru.example.account.security.service.SessionCommandService;
import ru.example.account.security.service.facade.SessionRevocationServiceFacade;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionCommandServiceImpl implements SessionCommandService {

    private final RevokedDataRepository revokedDataRepository;

    private final IdGenerationService idGenerationService;

    private final SessionRevocationServiceFacade sessionRevocationService;

    @Override
    @Transactional(value = "securityTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void archiveAllForUser(Long userId,
                                  String fingerprint,
                                  String ipAddress,
                                  String userAgent,
                                  RevocationReason revocationReason) {

        RevokedClientData data = RevokedClientData.builder()
                .id(this.idGenerationService.generateSessionId()) // Уникальный Id инцидента
                .userId(userId)
                .fingerprint(fingerprint)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAlertAt(Instant.now())
                .revocationReason(revocationReason)
                .build();
        revokedDataRepository.save(data);

        sessionRevocationService.revokeAllSessionsForUser(userId, SessionStatus.STATUS_COMPROMISED, RevocationReason.REASON_RED_ALERT);
    }
}
