package ru.example.account.business.service.impl;

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
import ru.example.account.business.entity.Account;
import ru.example.account.business.repository.AccountRepository;
import ru.example.account.user.service.PageProcessor;
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
            retryFor = { OptimisticLockingFailureException.class }, // <-- МЕНЯЕМ ТИП ИСКЛЮЧЕНИЯ на правильный
            maxAttempts = 3,
            backoff = @Backoff(delay = 200, multiplier = 2) // Немного увеличим паузу
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW) //REQUIRES_NEW - критически важно, чтобы каждая пачка была в своей транзакции
    public Page<Account> processPage(int page, int pageSize) {
        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by("id"));
        Page<Account> accountPage = accountRepository.findAllNotBiggerThanMax(MAX_PERCENT, pageRequest);

        if (accountPage.isEmpty()) {
            return accountPage; // Если страница пуста, просто выходим
        }

        List<Account> updatedAccounts = accountPage.getContent().stream()
                .filter(account -> account.getBalance().signum() > 0)
                .map(this::applyInterest)
                .toList();

        if (!updatedAccounts.isEmpty()) {
            // Hibernate сам проверит поле @Version при сохранении
            accountRepository.saveAll(updatedAccounts);
        }

        return accountPage;
    }

    private Account applyInterest(Account account) {
        BigDecimal maxAllowed = account.getInitialBalance().multiply(MAX_PERCENT);

        // Проверка перед вычислением, чтобы не делать лишнюю работу
        if (account.getBalance().compareTo(maxAllowed) >= 0) {
            log.trace("Cap for Account {} has already been reached.", account.getId());
            return account;
        }

        BigDecimal newBalance = account.getBalance()
                .multiply(INCREASE_RATE)
                .setScale(SCALE, ROUNDING_MODE);

        // Устанавливаем либо новый баланс, либо максимальный, если вышли за пределы
        account.setBalance(newBalance.min(maxAllowed));
        return account;
    }

    @Recover
    public Page<Account> handleOptimisticLockFailure(
            OptimisticLockingFailureException ex,
            int page,
            int pageSize
    ) {
        // Логируем, что даже ретраи не помогли
        log.error("Optimistic lock conflict on page {} couldn't be resolved after multiple retries. Message: {}", page, ex.getMessage());
        return Page.empty(); // Возвращаем пустую страницу, чтобы главный цикл мог ее обработать и завершиться
    }
}
