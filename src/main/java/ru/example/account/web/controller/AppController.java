package ru.example.account.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import ru.example.account.util.exception.exceptions.UserNotFoundException;
import ru.example.account.web.model.account.request.CreateMoneyTransferRequest;
import ru.example.account.web.model.account.response.CreateMoneyTransferResponse;
/**
 * Контроллер финансовых операций.
 * Требует аутентификации с ролью USER.
 *
 * @endpoint /api/v1/app
 */
@Tag(name = "Account Operations", description = "Money transfer and balance management")
@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/app")
@RequiredArgsConstructor
public class AppController {

    private final AccountService accountService;
    /**
     * Перевод средств между аккаунтами.
     *
     * @param currentUser Данные аутентифицированного пользователя
     * @param request Запрос на перевод (ID получателя и сумма)
     * @param authorizationHeader JWT-токен
     * @return Результат операции
     *
     * @throws UserNotFoundException Если пользователь не найден
     */
    @Operation(
            summary = "Transfer money",
            description = "Transfer funds between accounts",
            security = @SecurityRequirement(name = "bearerAuth"),
            parameters = {
                    @Parameter(name = "Authorization", required = true, in = ParameterIn.HEADER)
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Transfer successful",
                            content = @Content(schema = @Schema(implementation = CreateMoneyTransferResponse.class))

                    )
            }
    )

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public CreateMoneyTransferResponse transferMoneyBetweenAccounts(@AuthenticationPrincipal AppUserDetails currentUser,
                                                                    @Validated @RequestBody CreateMoneyTransferRequest request,
                                                                    @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {

        return accountService.transferFromOneAccountToAnother(currentUser, request, authorizationHeader);
    }
}
