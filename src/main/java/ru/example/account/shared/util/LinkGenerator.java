package ru.example.account.shared.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.example.account.user.entity.Client;

@Slf4j
@Component
@RequiredArgsConstructor
public class LinkGenerator {

    private final PasswordEncoder passwordEncoder;

    private final String SALT = "^&GOUYGOU%R&(*^FUTUY6ythrV&TDF08i9h8070bRUCVOTIY%DTFCVDcb2zOb";

    private final String HEADER = "ON(_N_(N(N_(*H_+(*h-98h";

    public String generateActivationLink(String data, Client client) {

        return "";
    }
}
