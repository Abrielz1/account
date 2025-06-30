package ru.example.account.app.service;

import ru.example.account.app.entity.Account;
import java.util.List;

public interface AccountBatchProcessor {

    void processAccounts(List<Account> accounts);
}
