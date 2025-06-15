package ru.example.account.app.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.app.entity.Account;
import ru.example.account.app.repository.AccountRepository;
import ru.example.account.app.service.PageProcessor;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PageProcessorImpl implements PageProcessor {

    private final AccountRepository accountRepository;

    private static final BigDecimal MAX_PERCENT = new BigDecimal("2.07");
    private static final BigDecimal INCREASE_RATE = new BigDecimal("1.10");
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final int SCALE = 2;

    @Override
    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Page<Account> processPage(int page, int pageSize) {
        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by("id"));
        Page<Account> accountPage = accountRepository.findAllNotBiggerThanMax(MAX_PERCENT, pageRequest);

        if (accountPage.isEmpty()) {
            return accountPage;
        }

        List<Account> updatedAccounts = accountPage.getContent().stream()
                .filter(account -> account.getBalance().signum() > 0)
                .map(this::applyInterest)
                .filter(account -> {
                    BigDecimal maxAllowed = account.getInitialBalance().multiply(MAX_PERCENT);
                    return account.getBalance().compareTo(maxAllowed) < 0;
                })
                .toList();

        if (!updatedAccounts.isEmpty()) {
            accountRepository.saveAll(updatedAccounts);
        }

        return accountPage;
    }

    private Account applyInterest(Account account) {

        BigDecimal maxAllowed = account.getInitialBalance().multiply(MAX_PERCENT);
        BigDecimal newBalance = account.getBalance()
                .multiply(INCREASE_RATE)
                .setScale(SCALE, ROUNDING_MODE);


        if (account.getBalance().compareTo(maxAllowed) >= 0) {
            log.info("Cap of Account reached!");
            return account;
        }

        account.setBalance(newBalance.min(maxAllowed));
        return account;
    }

    @Recover
    public Page<Account> handleOptimisticLockFailure(
            OptimisticLockingFailureException ex,
            int page,
            int pageSize
    ) {
        log.error("Optimistic lock failed after 3 retries for page {}: {}", page, ex.getMessage());
        return null;
    }
}
