package ru.example.account.user.service;

import org.springframework.data.domain.Page;
import ru.example.account.business.entity.Account;

public interface PageProcessor {

    Page<Account> processPage(int page, int pageSize);
}
