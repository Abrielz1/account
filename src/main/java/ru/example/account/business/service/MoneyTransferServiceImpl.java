package ru.example.account.business.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.business.entity.Account;
import ru.example.account.business.repository.AccountRepository;
import ru.example.account.shared.exception.exceptions.AccountNotFoundException;
import ru.example.account.business.model.request.CreateMoneyTransferRequest;
import ru.example.account.business.model.response.CreateMoneyTransferResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class MoneyTransferServiceImpl implements MoneyTransferService {


    private final AccountRepository accountRepository;

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public CreateMoneyTransferResponse transferFromOneAccountToAnother(Long currentUserId,
                                                                       CreateMoneyTransferRequest request) {

        if (request.sum() == null || request.sum().compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Sum to transfer must be greater than zero!");
            throw new IllegalArgumentException("Sum to transfer must be greater than zero!");
        }

        // --- 2. Получаем ID счетов ---
        Long senderAccountId = this.getAccountIdByUserId(currentUserId);
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


    private CreateMoneyTransferResponse validateAndProceedTransfer(Account senderAccount,
                                                                   Account receiverAccount,
                                                                   BigDecimal moneyToTransfer) {

        if (senderAccount.getId().equals(receiverAccount.getId())) {
            throw new IllegalArgumentException("Cannot transfer to yourself");
        }

        if (senderAccount.getBalance().compareTo(moneyToTransfer) >= 0) {
            BigDecimal amount = moneyToTransfer.setScale(2, RoundingMode.HALF_EVEN);

            BigDecimal senderBalance = senderAccount.getBalance()
                    .subtract(amount)
                    .setScale(2, RoundingMode.HALF_EVEN);

            BigDecimal receiverBalance = receiverAccount.getBalance()
                    .add(amount)
                    .setScale(2, RoundingMode.HALF_EVEN);

            senderAccount.setBalance(senderBalance);
            receiverAccount.setBalance(receiverBalance);

            log.info("Operation was succeed");
            return new CreateMoneyTransferResponse(true, "Operation was succeed");
        } else {
            log.error("Operation was declined. User tries move more than possessed!");
            return new CreateMoneyTransferResponse(false, "Operation was declined. User tries move more than possessed!");
        }
    }

    public Long getAccountIdByUserId(Long userId) {
        return this.accountRepository.findAccountIdByUserIdSafe(userId).orElseThrow(() -> {
            log.error("No such entity as requested account!");
            return new AccountNotFoundException("No such entity as requested account!");
        });
    }

    public Account getAccountWithLocksByUserId(Long accountId) {
        return this.accountRepository.getAccountWithLocksByUserId(accountId).orElseThrow(() -> {
            log.error("Account in db with such id: {} was not found", accountId);
            return new AccountNotFoundException(String.format("Account in db with such id: %d not found", accountId));
        });
    }
}

