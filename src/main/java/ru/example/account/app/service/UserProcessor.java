package ru.example.account.app.service;

import ru.example.account.app.entity.User;

public interface UserProcessor {

    User getUserByUserId(Long userId);

    boolean isFreeEmail(String newEmail);

    boolean isFreePhone(String newPhone);
}
