package ru.example.account.security.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.security.jwt.JwtUtils;
import ru.example.account.security.model.request.LoginRequest;
import ru.example.account.security.model.request.RefreshTokenRequest;
import ru.example.account.security.model.request.UserRegisterRequestDto;
import ru.example.account.security.model.response.AuthResponse;
import ru.example.account.security.service.AccessTokenBlacklistService;
import ru.example.account.security.service.AuthService;
import ru.example.account.security.service.SessionCreationService;
import ru.example.account.security.service.SessionQueryService;
import ru.example.account.security.service.SessionRevocationService;
import ru.example.account.shared.util.FingerprintUtils;
import ru.example.account.user.model.response.CreateUserAccountDetailResponseDto;
import ru.example.account.user.service.ClientService;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {


    // --- ЗАВИСИМОСТИ ---
    private final AuthenticationManager authenticationManager;

    private final ClientService clientService;

    private final JwtUtils jwtUtils;

    private final SessionCreationService sessionCreationService;

    private final SessionQueryService sessionQueryService;

    private final SessionRevocationService sessionRevocationService;

    private final AccessTokenBlacklistService blacklistService;

    private final FingerprintUtils fingerprintUtils;

    private final ApplicationEventPublisher eventPublisher;

    private final UserDetailsService userDetailsService;

    @Override
    @Transactional(value = "businessTransactionManager")
    public CreateUserAccountDetailResponseDto registerNewUserAccount(UserRegisterRequestDto request) {

        if (request == null ) {
            log.error("User suplied empty registeration form!");
             throw new IllegalArgumentException("User suplied empty registeration form!");
        }

        log.info("user accounr was created");
        return  clientService.registerNewUser(request);
    }

    @Override
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                ));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return null;
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request, HttpServletRequest httpRequest) {
        return null;
    }

    @Override
    public void logout(AppUserDetails userDetails) {

    }

    @Override
    public void logoutAll(Long userId) {

    }
}
