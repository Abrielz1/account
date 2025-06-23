package ru.example.account.app.service.impl;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
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

    private final PageProcessor pageProcessor;

    private final AccountRepository accountRepository;

    private final JwtUtils jwtUtils;

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public CreateMoneyTransferResponse transferFromOneAccountToAnother(AppUserDetails currentUser,
                                                                       CreateMoneyTransferRequest request,
                                                                       String token) {

        if (request.sum() == null || request.sum().compareTo(BigDecimal.ZERO) <= 0) {
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
            throw new BadRequestException(String.format("User with id: %d and other id %d tries steel money!", userIdFromDetails, userIdFromToken));
        }

        // --- 2. Получаем ID счетов ---
        Long senderAccountId = this.getAccountIdByUserId(userIdFromToken);
        Long receiverAccountId = this.getAccountIdByUserId(request.to());

        // --- 3. Блокируем счета в правильном порядке, чтобы избежать дедлока ---
        Account lockedAccount1 = this.getAccountWithLocksByUserId(Math.min(senderAccountId, receiverAccountId));
        Account lockedAccount2 = this.getAccountWithLocksByUserId(Math.max(senderAccountId, receiverAccountId));

        // --- 4. логика определения отправителя и получателя ---
        if (lockedAccount1.getId().equals(senderAccountId)) {
            return this.validateAndProceedTransfer(lockedAccount1, lockedAccount2, request.sum());
        } else {
            return this.validateAndProceedTransfer(lockedAccount2, lockedAccount1, request.sum());
        }
    }

    private Long getUserIdAndValidateToken(String token) {

        if (token == null) {
            log.info("Presented token is empty");
            throw new BadRequestException("Presented token is empty");
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        return jwtUtils.getUserIdFromClaimJwt(token);
    }

    private CreateMoneyTransferResponse validateAndProceedTransfer(Account senderAccount,
                                                                   Account receiverAccount,
                                                                   BigDecimal moneyToTransfer) {

        if (senderAccount.getId().equals(receiverAccount.getId())) {
            throw new IllegalArgumentException("Cannot transfer to yourself");
        }

        if (senderAccount.getBalance().compareTo(moneyToTransfer) >= 0) {
            BigDecimal amount = moneyToTransfer.setScale(2, RoundingMode.HALF_UP);

            BigDecimal senderBalance = senderAccount.getBalance()
                    .subtract(amount)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal receiverBalance = receiverAccount.getBalance()
                    .add(amount)
                    .setScale(2, RoundingMode.HALF_UP);

            senderAccount.setBalance(senderBalance);
            receiverAccount.setBalance(receiverBalance);

            log.info("Operation was succeed");
            return new CreateMoneyTransferResponse(true, "Operation was succeed");
        } else {
            log.error("Operation was declined. User tries move more than possessed!");
            return new CreateMoneyTransferResponse(false, "Operation was declined. User tries move more than possessed!");
        }
    }

    @Scheduled(fixedRateString = "${app.scheduling.moneyRiser.rate:30000}")
    @CacheEvict(cacheNames = {"users", "accounts"}, allEntries = true)
    public void moneyRiser() {
        log.info("Starting scheduled balance increase job (while-loop strategy).");

        int pageNumber = 0;
        final int pageSize = 50;
        boolean hasNextPage = true;

        while (hasNextPage) {

            try {

                Page<Account> currentPage = pageProcessor.processPage(pageNumber, pageSize);

                if (!currentPage.hasContent()) {
                    if (pageNumber == 0) {
                        log.info("No accounts found for balance increase at all.");
                    } else {
                        log.info("No more accounts to process. Job finished.");
                    }
                    break;
                }

                log.debug("Page {} processed successfully.", pageNumber);


                hasNextPage = currentPage.hasNext();
                pageNumber++;

            } catch (Exception e) {
                // Если PageProcessor выбросил финальное исключение (даже после ретраев),
                // мы логируем это и немедленно прекращаем работу.
                log.error("Fatal error on page {} while processing balances. " +
                        "Stopping the job for this run. Error: {}", pageNumber, e.getMessage());
                // Прерываем весь цикл
                hasNextPage = false;
            }
        }

        log.info("Scheduled balance increase job finished for this run.");
    }

    private Long getAccountIdByUserId(Long userId) {
        return this.accountRepository.findAccountIdByUserIdSafe(userId).orElseThrow(() -> {
            log.error("No such entity as requested account!");
            return new AccountNotFoundException("No such entity as requested account!");
        });
    }

    private Account getAccountWithLocksByUserId(Long accountId) {
        return this.accountRepository.getAccountWithLocksByUserId(accountId).orElseThrow(() -> {
            log.error("Account in db with such id: {} was not found", accountId);
            return new AccountNotFoundException(String.format("Account in db with such id: %d not found", accountId));
        });
    }
}
