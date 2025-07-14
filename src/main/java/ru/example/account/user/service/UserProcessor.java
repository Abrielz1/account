package ru.example.account.user.service;

import ru.example.account.user.entity.User;

public interface UserProcessor {

    User getUserByUserId(Long userId);

    boolean isFreeEmail(String newEmail);

    boolean isFreePhone(String newPhone);

    boolean isFreeUsername(String Username);
}
