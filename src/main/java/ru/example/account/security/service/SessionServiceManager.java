package ru.example.account.security.service;

import ru.example.account.security.model.response.AuthResponse;
import ru.example.account.security.principal.AppUserDetails;
import java.time.ZonedDateTime;

public interface SessionServiceManager {

    AuthResponse createSession(AppUserDetails currentUser,
                               String ipAddress,
                               String fingerprint,
                               String userAgent,
                               ZonedDateTime lastSeenAt);

    AuthResponse rotateSessionAndTokens(String refreshToken,
                                        String accessesToken,
                                        String fingerprint,
                                        String ipAddress,
                                        String userAgent,
                                        AppUserDetails currentUser);

    void logout(AppUserDetails userToLogOut);

    void logoutAll(AppUserDetails userToLogOut);
}
