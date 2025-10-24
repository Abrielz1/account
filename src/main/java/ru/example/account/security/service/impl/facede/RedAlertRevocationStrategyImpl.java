package ru.example.account.security.service.impl.facede;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.example.account.security.entity.RevocationReason;
import ru.example.account.security.entity.SessionStatus;
import ru.example.account.security.service.facade.RedAlertRevocationStrategy;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedAlertRevocationStrategyImpl implements RedAlertRevocationStrategy {

    @Override
    public CompletableFuture<Boolean> executeRedAlertProtocol(Long authSessionList, SessionStatus status, RevocationReason reason) {

        return null;
    }
}
