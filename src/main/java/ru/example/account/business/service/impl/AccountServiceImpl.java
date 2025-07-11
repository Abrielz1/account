package ru.example.account.business.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.example.account.business.entity.Account;
import ru.example.account.business.model.response.AccountInitialResponseDto;
import ru.example.account.business.repository.AccountRepository;
import ru.example.account.business.service.AccountService;
import ru.example.account.security.model.request.UserRegisterRequestDto;
import ru.example.account.user.entity.User;
import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private AccountRepository accountRepository;

    @Override
    public Account createUserAccount(User owner, UserRegisterRequestDto request) {


        return null;
    }

    @Override
    public AccountInitialResponseDto AccountCreateRequestDto(String accountName, BigDecimal initialDeposit) {


        return null;
    }
}
