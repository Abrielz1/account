package ru.example.account.security.service.facade;

import ru.example.account.security.entity.enums.RevocationReason;
import ru.example.account.security.entity.enums.SessionStatus;

import java.util.concurrent.CompletableFuture;

public interface RedAlertRevocationStrategy {
    CompletableFuture<Boolean> executeRedAlertProtocol(Long authSessionList,
                                                       SessionStatus status,
                                                       RevocationReason reason);
}
