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
                                                  Long affectedUserid,
                                                  UUID triggeringIncidentId,
                                                  Long userWhatSetBanId,
                                                  ZonedDateTime expiresAt) {
        BlockedTarget toBanAttackerByIp = BlockedTarget
                .builder()
                .blockedByUserId(userWhatSetBanId != null ? userWhatSetBanId : null)
                .targetValue(BlockedEntityType.IP_ADDRESS.name())
                .affectedUserId(affectedUserid != null ? affectedUserid : null)
                .reason(reason.name())
                .triggeringIncidentId(triggeringIncidentId)
                .expiresAt(expiresAt != null ? expiresAt : null)
                .build();

        return Optional.of(blockedTargetRepository.save(toBanAttackerByIp));
    }
}
