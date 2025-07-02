package ru.example.account.business.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Schema(description = "Request for money transfer between accounts")
public record CreateMoneyTransferRequest(@Positive
                                         @NotNull
                                         @Schema(description = "Recipient account ID", example = "2")
                                         Long to,

                                         @NotNull @DecimalMin("0.01") @Digits(integer=12, fraction=2)@Digits(integer=12, fraction=2)
                                         @Max(value = 1000000, message = "Max transfer sum is 1,000,000")
                                         @Schema(description = "Transfer amount", example = "50.00")
                                         BigDecimal sum) {

}
