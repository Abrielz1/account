package ru.example.account.user.model.response;

public record ActivationClientAccountRequest(String email, String activationLink) {
}
