package ru.example.account.security.service.impl.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.example.account.security.entity.BlockReason;
import ru.example.account.security.entity.BlockedEntityType;
import ru.example.account.security.entity.BlockedTarget;
import ru.example.account.security.repository.BlockedTargetRepository;
import ru.example.account.security.service.worker.BlockCommandWorker;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockCommandWorkerImpl implements BlockCommandWorker {

    private final BlockedTargetRepository blockedTargetRepository;

    @Override
    public Optional<BlockedTarget> blockIpAddress(String ipAddress,
                                                  Duration duration,
                                                  BlockReason reason,
                                                  BlockedEntityType blockType,
                                                  Long affectedUserid,
                                                  UUID triggeringIncidentId,
                                                  ZonedDateTime expiresAt) {
        BlockedTarget toBanAttackerByIp = BlockedTarget
                .builder()
                .targetType(blockType)
                .targetValue(ipAddress)
                .affectedUserId(affectedUserid)
                .reason(reason.name())
                .triggeringIncidentId(triggeringIncidentId)
                .expiresAt(expiresAt)
                .build();

        return Optional.of(blockedTargetRepository.save(toBanAttackerByIp));
    }
}
