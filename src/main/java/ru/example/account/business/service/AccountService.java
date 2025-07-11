package ru.example.account.business.service;


import ru.example.account.business.entity.Account;
import ru.example.account.business.model.response.AccountInitialResponseDto;
import ru.example.account.security.model.request.UserRegisterRequestDto;
import ru.example.account.user.entity.User;
import java.math.BigDecimal;

public interface AccountService {

    Account createUserAccount(User owner, UserRegisterRequestDto request);

    AccountInitialResponseDto AccountCreateRequestDto(String accountName, BigDecimal initialDeposit);
}
