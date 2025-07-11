package ru.example.account.business.service;

import ru.example.account.business.model.request.CreateMoneyTransferRequest;
import ru.example.account.business.model.response.CreateMoneyTransferResponse;
import ru.example.account.security.service.impl.AppUserDetails;

public interface MoneyTransferService {

    CreateMoneyTransferResponse transferFromOneAccountToAnother(AppUserDetails currentUser,
                                                                CreateMoneyTransferRequest request,
                                                                String authorizationHeader);

}
