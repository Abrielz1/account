package ru.example.account.business.model;

import ru.example.account.business.entity.Account;
import java.math.BigDecimal;

public record AccountCacheDto(Long id,
                              BigDecimal balance) {
    public static AccountCacheDto fromEntity(Account account) {

        return new AccountCacheDto(
                account.getId(),
                account.getBalance()
        );
    }
}
