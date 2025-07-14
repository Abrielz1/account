package ru.example.account.user.service;

import ru.example.account.user.entity.Client;
import ru.example.account.user.entity.User;
import java.util.Optional;

public interface UserProcessor {

    User getUserByUserId(Long userId);

    boolean isFreeEmail(String newEmail);

    boolean isFreePhone(String newPhone);

    boolean isFreeUsername(String Username);

    Optional<Client> getReferrer(Long referrerId);
}
