package ru.example.account.exchange.dto;

public record SecurityAlertEmailEvent(
        Long userId,

        String userEmail,

        String fingerPrintHash,

        String alertMessage,

        String ipAddress
        ) {
}
