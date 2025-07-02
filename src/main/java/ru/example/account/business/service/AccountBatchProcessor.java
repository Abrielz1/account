package ru.example.account.business.service;

import ru.example.account.business.entity.Account;
import java.util.List;

public interface AccountBatchProcessor {

    void processAccounts(List<Account> accounts);
}
