package ru.example.account.web.model.account.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreateMoneyTransferRequest(@Positive Long to,
                                         @NotNull @DecimalMin("0.01") @Digits(integer=12, fraction=2) BigDecimal sum) {

}
