package ru.example.account.security.service;

import ru.example.account.security.model.response.AuthResponse;
import ru.example.account.security.service.impl.AppUserDetails;

public interface SessionServiceManager {

    AuthResponse createSession(AppUserDetails userDetails,
                               String ipAddress,
                               String fingerprint,
                               String userAgent);

    AuthResponse rotateSessionAndTokens(String refreshToken,
                                        String accessesToken,
                                        String fingerPrint,
                                        AppUserDetails currentUser);
}
