package ru.example.account.business.service;

import ru.example.account.business.model.request.CreateMoneyTransferRequest;
import ru.example.account.business.model.response.CreateMoneyTransferResponse;

public interface MoneyTransferService {

    CreateMoneyTransferResponse transferFromOneAccountToAnother(Long currentUserId,
                                                                CreateMoneyTransferRequest request);
}
