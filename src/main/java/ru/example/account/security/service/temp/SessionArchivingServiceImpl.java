package ru.example.account.security.service.temp;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.entity.RevokedTokenArchive;
import ru.example.account.security.repository.AuthSessionRepository;
import ru.example.account.security.repository.RevokedTokenArchiveRepository;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SessionArchivingServiceImpl implements SessionArchivingService {

    private final AuthSessionRepository authSessionRepository;

    private final RevokedTokenArchiveRepository archiveRepository;

    // ВАЖНО: Этот метод должен выполняться в НОВОЙ транзакции,
    // чтобы его откат не влиял на основной процесс.
    @Override
    @Transactional(value = "securityTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void archive(AuthSession session, RevocationReason reason) {
        RevokedTokenArchive archiveEntry = RevokedTokenArchive.builder()
                .tokenValue(session.getRefreshToken())
                .sessionId(session.getId())
                .userId(session.getUserId())
                .revokedAt(Instant.now())
                .reason(reason)
                .build();

        archiveRepository.save(archiveEntry);
        authSessionRepository.delete(session); // Удаляем из активных сессий
    }
}
