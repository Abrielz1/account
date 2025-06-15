package ru.example.account.app.service;

import ru.example.account.app.security.service.impl.AppUserDetails;
import ru.example.account.web.model.account.request.CreateMoneyTransferRequest;
import ru.example.account.web.model.account.response.CreateMoneyTransferResponse;

public interface AccountService {
    CreateMoneyTransferResponse transferFromOneAccountToAnother(AppUserDetails currentUser,
                                                                CreateMoneyTransferRequest request,
                                                                String authorizationHeader);
}
