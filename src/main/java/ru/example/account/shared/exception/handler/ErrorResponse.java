package ru.example.account.shared.exception.handler;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Error response structure")
public class ErrorResponse {

    @Schema(description = "Error type", example = "Validation Error")
    private String error;

    @Schema(description = "Detailed message", example = "Invalid input data")
    private String description;
}
