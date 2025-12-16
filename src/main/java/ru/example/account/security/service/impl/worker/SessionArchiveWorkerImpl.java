package ru.example.account.security.service.impl.worker;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.security.entity.AuthSession;
import ru.example.account.security.entity.enums.RevocationReason;
import ru.example.account.security.entity.RevokedSessionArchive;
import ru.example.account.security.entity.enums.SessionStatus;
import ru.example.account.security.repository.RevokedSessionArchiveRepository;
import ru.example.account.security.service.worker.SessionArchiveWorker;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionArchiveWorkerImpl implements SessionArchiveWorker {

    @PersistenceContext
    private final EntityManager entityManager;

    private final RevokedSessionArchiveRepository revokedSessionArchiveRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void archive(AuthSession sessionToRevoke, RevocationReason revocationReason, SessionStatus sessionStatus) {

       AuthSession sessionComeAlive = this.entityManager.merge(sessionToRevoke);

        RevokedSessionArchive revokedSessionArchive = new RevokedSessionArchive();
        revokedSessionArchive.setUp(sessionComeAlive, revocationReason, sessionStatus);

        this.revokedSessionArchiveRepository.save(revokedSessionArchive);
    }
}
