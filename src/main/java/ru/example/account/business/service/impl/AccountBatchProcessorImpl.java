package ru.example.account.business.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.business.entity.Account;
import ru.example.account.business.repository.AccountRepository;
import ru.example.account.business.service.AccountBatchProcessor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class AccountBatchProcessorImpl implements AccountBatchProcessor {

    private final AccountRepository accountRepository;
    private static final BigDecimal MAX_PERCENT = new BigDecimal("2.07");
    private static final BigDecimal INCREASE_RATE = new BigDecimal("1.10");
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final int SCALE = 2;

    @Override
    @Retryable(
            retryFor = { OptimisticLockingFailureException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 200, multiplier = 2)
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processAccounts(List<Account> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            return;
        }

        List<Account> processedAccountsList = accounts.stream().map(this::applyInterest).toList();
        accountRepository.saveAll(processedAccountsList); // Используем saveAllAndFlush для немедленной записи
        log.debug("Processed and saved a batch of {} accounts.", accounts.size());
    }

    private Account applyInterest(Account account) {
        BigDecimal maxAllowed = account.getInitialBalance().multiply(MAX_PERCENT);
        if (account.getBalance().compareTo(maxAllowed) >= 0) {
            return account;
        }
        BigDecimal newBalance = account.getBalance()
                .multiply(INCREASE_RATE)
                .setScale(SCALE, ROUNDING_MODE);
        account.setBalance(newBalance.min(maxAllowed));
        return account;
    }

    @Recover
    public void handleRetriesExhausted(OptimisticLockingFailureException ex, List<Account> accounts) {
        log.error("Failed to process account batch after all retries. First account ID in batch: {}.",
                accounts.isEmpty() ? "N/A" : accounts.get(0).getId(), ex);
        // Пробрасываем исключение наверх, чтобы основной шедулер его поймал
        throw ex;
    }
}
