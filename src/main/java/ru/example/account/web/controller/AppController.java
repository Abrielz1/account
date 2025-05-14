package ru.example.account.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.example.account.app.security.service.impl.AppUserDetails;
import ru.example.account.app.service.AccountService;
import ru.example.account.web.model.account.request.CreateMoneyTransferRequest;
import ru.example.account.web.model.account.response.CreateMoneyTransferResponse;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/app")
@RequiredArgsConstructor
public class AppController {

    private final AccountService accountService;

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public CreateMoneyTransferResponse transferMoneyBetweenAccounts(@AuthenticationPrincipal AppUserDetails currentUser,
                                                                    @Validated @RequestBody CreateMoneyTransferRequest request,
                                                                    @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {

        return accountService.transferFromOneAccountToAnother(currentUser, request, authorizationHeader);
    }
}
