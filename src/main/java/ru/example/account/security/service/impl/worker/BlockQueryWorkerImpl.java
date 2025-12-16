package ru.example.account.security.service.impl.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.example.account.security.entity.enums.BlockedEntityType;
import ru.example.account.security.repository.BlockedTargetRepository;
import ru.example.account.security.service.worker.BlockQueryWorker;
import java.time.ZonedDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockQueryWorkerImpl implements BlockQueryWorker {

    private final BlockedTargetRepository blockedTargetRepository;

    @Override
    public boolean isIpBlocked(String givenIpAddress) {
        return blockedTargetRepository.isTargetCurrentlyBlocked(givenIpAddress, BlockedEntityType.IP_ADDRESS.name());
    }

    public boolean isIpBlocked(String targetValue, BlockedEntityType typeOfBlocking, ZonedDateTime timeOfReleaseBan) {
        return blockedTargetRepository.findActiveBlock(typeOfBlocking.name(), targetValue, timeOfReleaseBan).isPresent();
    }
}
