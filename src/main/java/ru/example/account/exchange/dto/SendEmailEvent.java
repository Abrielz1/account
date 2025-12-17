package ru.example.account.exchange.dto;

public record SendEmailEvent(

        String recipientEmail,

        String subject,

        String body
) {
}
