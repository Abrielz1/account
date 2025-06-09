package ru.example.account.app.service.impl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.app.entity.Account;
import ru.example.account.app.entity.User;
import ru.example.account.app.repository.AccountRepository;
import ru.example.account.app.repository.UserRepository;
import ru.example.account.app.security.jwt.JwtUtils;
import ru.example.account.app.security.service.impl.AppUserDetails;
import ru.example.account.app.service.AccountService;
import ru.example.account.util.exception.exceptions.BadRequestException;
import ru.example.account.util.exception.exceptions.UserNotFoundException;
import ru.example.account.web.AccountCacheDto;
import ru.example.account.web.model.account.request.CreateMoneyTransferRequest;
import ru.example.account.web.model.account.response.CreateMoneyTransferResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
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

    private static final BigDecimal MAX_PERCENT = new BigDecimal("2.07");
    private static final BigDecimal INCREASE_RATE = new BigDecimal("1.10");
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final int SCALE = 2;

    private final UserRepository userRepository;

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
    @CacheEvict(value = "users", key = "{#firstUser.id, #secondUser.id}")
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

        List<Long> usersIdsList = new ArrayList<>(List.of(userIdFromToken, request.to())).stream()
                .sorted(Long::compareTo)
                .toList();

        User firstUser = this.getUserFromDb(usersIdsList.get(0));
        User secondUser = this.getUserFromDb(usersIdsList.get(1));


        return this.validateAndPrecededTransfer(firstUser, secondUser, request.sum());
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

    private CreateMoneyTransferResponse validateAndPrecededTransfer(User hostUserAccount,
                                                                    User receiverUserAccount,
                                                                    BigDecimal moneyToTransfer) {
        if (hostUserAccount.getId().equals(receiverUserAccount.getId())) {
            throw new IllegalArgumentException("Cannot transfer to yourself");
        }

        if (hostUserAccount.getUserAccount().getBalance().compareTo(moneyToTransfer) >= 0) {
            BigDecimal amount = moneyToTransfer.setScale(2, RoundingMode.HALF_UP);

            BigDecimal senderBalance = hostUserAccount.getUserAccount().getBalance()
                    .subtract(amount)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal receiverBalance = receiverUserAccount.getUserAccount().getBalance()
                    .add(amount)
                    .setScale(2, RoundingMode.HALF_UP);

            hostUserAccount.getUserAccount().setBalance(senderBalance);
            receiverUserAccount.getUserAccount().setBalance(receiverBalance);

            log.info("Operation was succeed");
            return new CreateMoneyTransferResponse(true, "Operation was succeed");
        } else {
            log.error("Operation was declined. User tries move more than possessed!");
            return new CreateMoneyTransferResponse(false, "Operation was declined. User tries move more than possessed!");
        }

    }

    private User getUserFromDb(Long userId) {

        return this.userRepository.findWithLockingById(userId).orElseThrow(() -> {
            log.error("No user in db with such id: {}", userId);
            return new UserNotFoundException("No user in db with such id: %d".formatted(userId));
        });
    }
    /**
     * Ежедневное начисление 10% на баланс (макс. 207% от депозита).
     * Запускается каждые 30 секунд (для демонстрации).
     */
    @Scheduled(fixedRate = 30_000)
    @CacheEvict(cacheNames = "accounts", allEntries = true)
    @Transactional()
    public void moneyRiser() {

        List<Account> accountList = accountRepository.findAllNotBiggerThanMax(MAX_PERCENT);

        if (accountList.isEmpty()) {
            log.info("No accounts to process");
            return;
        }

        accountRepository.saveAll(accountList.stream()
                .map(balance -> {

                    if (balance.getBalance().signum() <= 0) {
                     log.debug("Skipping account {} with zero/negative balance", balance.getId());
                        return balance;
                    }

                    BigDecimal maxAllowed = balance.getInitialBalance().multiply(MAX_PERCENT);
                    BigDecimal newBalance = balance.getBalance().multiply(INCREASE_RATE)
                            .setScale(SCALE, ROUNDING_MODE);

                    balance.setBalance(newBalance.min(maxAllowed));
                    return balance;
                })
                .toList());
    }

    @Cacheable(key = "#userId", unless = "#result == null")
    public AccountCacheDto getAccountForCache(Long id) {
        return accountRepository.findAccountById(id)
                .map(AccountCacheDto::fromEntity)
                .orElse(null);
    }
}
