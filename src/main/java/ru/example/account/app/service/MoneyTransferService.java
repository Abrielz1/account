package ru.example.account.app.service;

import ru.example.account.web.model.account.request.CreateMoneyTransferRequest;
import ru.example.account.web.model.account.response.CreateMoneyTransferResponse;

public interface MoneyTransferService {

    CreateMoneyTransferResponse transferFromOneAccountToAnother(Long currentUserId,
                                                                CreateMoneyTransferRequest request);
}
