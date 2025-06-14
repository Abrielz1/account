package ru.example.account.app.service;

import org.springframework.data.domain.Page;
import ru.example.account.app.entity.Account;
import ru.example.account.app.security.service.impl.AppUserDetails;
import ru.example.account.web.model.account.request.CreateMoneyTransferRequest;
import ru.example.account.web.model.account.response.CreateMoneyTransferResponse;

public interface AccountService {
    CreateMoneyTransferResponse transferFromOneAccountToAnother(AppUserDetails currentUser,
                                                                CreateMoneyTransferRequest request,
                                                                String authorizationHeader);

    Page<Account> processPage(int page, int pageSize);
}
