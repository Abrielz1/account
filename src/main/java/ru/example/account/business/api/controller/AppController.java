package ru.example.account.business.api.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.example.account.security.service.impl.AppUserDetails;
import ru.example.account.business.service.MoneyTransferService;
import ru.example.account.business.model.request.CreateMoneyTransferRequest;
import ru.example.account.business.model.response.CreateMoneyTransferResponse;

@Slf4j
@Tag(name = "Account Operations", description = "Handles money transfers")
@RestController
@RequestMapping("/api/v1/app")
@RequiredArgsConstructor
public class AppController {

    private final MoneyTransferService moneyTransferService;

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public CreateMoneyTransferResponse transferMoneyBetweenAccounts(@AuthenticationPrincipal AppUserDetails currentUser,
                                                                    @Valid @RequestBody CreateMoneyTransferRequest request) {
        log.info("Transfer requested: from user ID {}, to {}, amount {}", currentUser.getId(), request.to(), request.sum());
        return moneyTransferService.transferFromOneAccountToAnother(currentUser.getId(), request);
    }
}
