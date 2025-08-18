package ru.example.account.business.api.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.example.account.business.service.MoneyTransferService;

@Slf4j
@Tag(name = "Account Operations", description = "Handles money transfers")
@RestController
@RequestMapping("/api/v1/app")
@RequiredArgsConstructor
public class AppController {

    private final MoneyTransferService moneyTransferService;

//    @PutMapping
//    @ResponseStatus(HttpStatus.OK)
//    public CreateMoneyTransferResponse transferMoneyBetweenAccounts(@AuthenticationPrincipal AppUserDetails currentUser,
//                                                                    @Valid @RequestBody CreateMoneyTransferRequest request) {
//        log.info("Transfer requested: from user ID {}, to {}, amount {}", currentUser.getId(), request.to(), request.sum());
//        return moneyTransferService.transferFromOneAccountToAnother(currentUser.getId(), request);
//    }
}
