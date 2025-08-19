package ru.example.account.security.service.impl.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.security.entity.BlockReason;
import ru.example.account.security.entity.BlockedEntityType;
import ru.example.account.security.entity.BlockedTarget;
import ru.example.account.security.entity.SecurityIncident;
import ru.example.account.security.repository.BlockedTargetRepository;
import ru.example.account.security.repository.SecurityIncidentRepository;
import ru.example.account.security.service.worker.BlockCommandWorker;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockCommandWorkerImpl implements BlockCommandWorker {

    private final BlockedTargetRepository blockedTargetRepository;

    private final SecurityIncidentRepository incidentRepository;

    @Override
    @Transactional(value = "securityTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public Optional<BlockedTarget> blockTarget(String targetValue,
                                                  Duration duration,
                                                  BlockReason reason,
                                                  BlockedEntityType blockType,
                                                  Long affectedUserid,
                                                  Long triggeringIncidentId,
                                                  ZonedDateTime expiresAt) {

        SecurityIncident incident = (triggeringIncidentId != null)
                ? this.incidentRepository.findById(triggeringIncidentId).orElse(null)
                : null;

        BlockedTarget toBanAttackerByIp = BlockedTarget
                .builder()
                .targetType(blockType)
                .targetValue(targetValue)
                .affectedUserId(affectedUserid)
                .reason(reason.name())
                .expiresAt(expiresAt)
                .triggeringIncident(incident)
                .build();

        return Optional.of(blockedTargetRepository.save(toBanAttackerByIp));
    }
}
