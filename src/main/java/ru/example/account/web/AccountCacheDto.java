package ru.example.account.web;

import ru.example.account.app.entity.Account;
import java.math.BigDecimal;

public record AccountCacheDto(Long id,
                              BigDecimal balance,
                              BigDecimal initialBalance) {
    public static AccountCacheDto fromEntity(Account account) {

        return new AccountCacheDto(
                account.getId(),
                account.getBalance(),
                account.getInitialBalance()
        );
    }
}
