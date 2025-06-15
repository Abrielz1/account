package ru.example.account.app.service.impl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.LockTimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.app.entity.Account;
import ru.example.account.app.repository.AccountRepository;
import ru.example.account.app.security.jwt.JwtUtils;
import ru.example.account.app.security.service.impl.AppUserDetails;
import ru.example.account.app.service.AccountService;
import ru.example.account.app.service.PageProcessor;
import ru.example.account.util.exception.exceptions.AccountNotFoundException;
import ru.example.account.util.exception.exceptions.BadRequestException;
import ru.example.account.util.exception.exceptions.ProcessingInterruptedException;
import ru.example.account.util.exception.exceptions.UserNotFoundException;
import ru.example.account.web.model.account.request.CreateMoneyTransferRequest;
import ru.example.account.web.model.account.response.CreateMoneyTransferResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Сервис управления банковскими операциями.
 * Реализует логику переводов и периодического начисления процентов.
 */
@Tag(name = "Transfer service", description = "Handles money transfers between accounts")
@Slf4j
@Service
@CacheConfig(cacheNames = "accounts")
@Transactional
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private static final int MAX_LOCK_ATTEMPTS = 5;

    private static final int MAX_ATTEMPTS_PER_PAGE = 3;

    private final PageProcessor pageProcessor;

    private final AccountRepository accountRepository;

    private final JwtUtils jwtUtils;

    /**
     * Перевод средств с проверкой блокировок.
     * Использует пессимистичные блокировки уровня REPEATABLE_READ.
     *
     * @CacheEvict Инвалидирует кэш аккаунтов после операции
     */
    @Operation(
            summary = "Transfer money",
            description = "Transfer funds between two accounts with amount validation",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Transfer successful"),
                    @ApiResponse(responseCode = "400", description = "Invalid input data"),
                    @ApiResponse(responseCode = "409", description = "Concurrency conflict")
            }
    )
    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public CreateMoneyTransferResponse transferFromOneAccountToAnother(AppUserDetails currentUser,
                                                                       CreateMoneyTransferRequest request,
                                                                       String token) {

        if (request.sum() == null) {
            log.error("sum and balance must not be a null");
            throw new IllegalArgumentException("sum and balance must not be a null");
        }

        if (request.sum().compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Sum to transfer must be greater than zero!");
            throw new IllegalArgumentException("Sum to transfer must be greater than zero!");
        }

        Long userIdFromToken = this.getUserIdAndValidateToken(token);

        if (userIdFromToken == null) {
            log.error("Invalid user ID in token");
            throw new BadRequestException("Invalid user ID in token");
        }

        Long userIdFromDetails = currentUser.getId();

        if (Objects.equals(userIdFromToken, request.to())) {
            log.error("User tries to move money to them self");
            throw new IllegalStateException("User tries to move money to them self");
        }

        if (!Objects.equals(userIdFromToken, userIdFromDetails)) {
            log.error("User with id: {} and other id {} tries steel money!", userIdFromDetails, userIdFromToken);
            throw new BadRequestException("User with id: %d and other id %d tries steel money!".formatted(userIdFromDetails, userIdFromToken));
        }

        Long accountIdUserFromToken = this.getAccountIdFromUserId(userIdFromToken);
        Long accountIdUserRequestTo = this.getAccountIdFromUserId(request.to());

        var firstUserAccount = this.getAccountFromDb(Math.min(accountIdUserFromToken, accountIdUserRequestTo));
        var secondUserAccount = this.getAccountFromDb(Math.max(accountIdUserFromToken, accountIdUserRequestTo));

        return this.validateAndPrecededTransfer(firstUserAccount, secondUserAccount, request.sum());
    }

    private Long getUserIdAndValidateToken(String token) {

        if (token == null) {
            log.info("%Presented token are empty");
            throw new BadRequestException("Presented token are empty");
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        return jwtUtils.getUserIdFromClaimJwt(token);
    }

    private CreateMoneyTransferResponse validateAndPrecededTransfer(Account hostUserAccount,
                                                                    Account receiverUserAccount,
                                                                    BigDecimal moneyToTransfer) {
        if (hostUserAccount.getId().equals(receiverUserAccount.getId())) {
            throw new IllegalArgumentException("Cannot transfer to yourself");
        }

        if (hostUserAccount.getBalance().compareTo(moneyToTransfer) >= 0) {
            BigDecimal amount = moneyToTransfer.setScale(2, RoundingMode.HALF_UP);

            BigDecimal senderBalance = hostUserAccount.getBalance()
                    .subtract(amount)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal receiverBalance = receiverUserAccount.getBalance()
                    .add(amount)
                    .setScale(2, RoundingMode.HALF_UP);

            hostUserAccount.setBalance(senderBalance);
            receiverUserAccount.setBalance(receiverBalance);

            log.info("Operation was succeed");
            return new CreateMoneyTransferResponse(true, "Operation was succeed");
        } else {
            log.error("Operation was declined. User tries move more than possessed!");
            return new CreateMoneyTransferResponse(false, "Operation was declined. User tries move more than possessed!");
        }
    }

    /**
     * Ежедневное начисление 10% на баланс (макс. 207% от депозита).
     * Запускается каждые 30 секунд (для демонстрации).
     */
    private int currentPage = 0;
    private boolean processingComplete = false;
    private int lockConflictCount = 0;

    @Scheduled(fixedRate = 30_000)
    @CacheEvict(cacheNames = "accounts", allEntries = true)
    public void moneyRiser() {

        if (processingComplete) {
          this.resetProcessingState();
            return;
        }

        try {

            this.processPageWithRetries();
        } catch (InterruptedException e) {
            this.handleInterruption();
        } catch (ProcessingInterruptedException ex) {
            log.warn("Processing interrupted", ex);
            this.resetProcessingState();
        }
    }

    private void processPageWithRetries() throws InterruptedException {

        for (int attempt = 0; attempt < MAX_ATTEMPTS_PER_PAGE; attempt++) {

            try {

                this.processCurrentPage();
                lockConflictCount = 0;
                return;
            } catch (PessimisticLockingFailureException | LockTimeoutException ex) {
                this.handleLockConflict(attempt);
            }
        }

        this.handleMaxAttemptsReached();
    }

    private void processCurrentPage() {

        Page<Account> pageResult = pageProcessor.processPage(currentPage, 50);

        if (pageResult == null || pageResult.isEmpty()) {

            this.handlePageError();
            return;
        }

        this.updateProcessingState(pageResult);
        log.info("Processed page {}: {} accounts", currentPage, pageResult.getNumberOfElements());
    }

    private void handleLockConflict(int attempt) throws InterruptedException {
        lockConflictCount = Math.min(lockConflictCount + 1, MAX_LOCK_ATTEMPTS);
        log.warn("Lock conflict on page {} (attempt {}/{})",
                currentPage, attempt + 1, MAX_ATTEMPTS_PER_PAGE);

        long delay = delayCalculator(lockConflictCount);
        Thread.sleep(delay);
    }

    private void handleMaxAttemptsReached() {
        log.warn("Max attempts reached for page {}, skipping", currentPage);
        this.handlePageError();
    }

    private void handleInterruption() {
        Thread.currentThread().interrupt();
        log.warn("Thread interrupted during backoff delay");
        throw new ProcessingInterruptedException("Processing interrupted during lock resolution");
    }
    private void resetProcessingState() {
        currentPage = 0;
        processingComplete = false;
        lockConflictCount = 0;
    }

    private void handlePageError() {
        log.error("Page {} processing failed", currentPage);
        currentPage++;
        lockConflictCount = 0; // Сброс счетчика конфликтов
    }

    private void updateProcessingState(Page<Account> pageResult) {
        if (!pageResult.hasNext()) {
            processingComplete = true;
        } else {
            currentPage++;
        }
    }

    private long delayCalculator(int lockConflictCount) {

        long baseDelay = 100L;
        long maxDelay = 10_000;

        double multiplier = Math.pow(2, Math.min(lockConflictCount - 1, 30));
        long delay = (long) (baseDelay * multiplier);

        return Math.min(delay, maxDelay);
    }

    private Long getAccountIdFromUserId(Long userId) {

        return this.accountRepository.findAccountIdByUserIdSafe(userId).orElseThrow(() -> {
            log.error("No such entity as requested account!");
            return new AccountNotFoundException("No such entity as requested account!");
        });
    }

    private Account getAccountFromDb(Long userId) {

        return this.accountRepository.getAccountWithLocksByUserId(userId).orElseThrow(() -> {
            log.error("No user in db with such id: {}", userId);
            return new UserNotFoundException("No user in db with such id: %d".formatted(userId));
        });
    }
}

