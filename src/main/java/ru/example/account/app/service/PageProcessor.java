package ru.example.account.app.service;

import org.springframework.data.domain.Page;
import ru.example.account.app.entity.Account;

public interface PageProcessor {

    Page<Account> processPage(int page, int pageSize);
}
