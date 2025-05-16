package ru.example.account.app.security.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.app.entity.RefreshToken;
import ru.example.account.app.entity.User;
import ru.example.account.app.repository.UserRepository;
import ru.example.account.app.security.jwt.JwtUtils;
import ru.example.account.app.security.service.SecurityService;
import ru.example.account.util.exception.exceptions.RefreshTokenException;
import ru.example.account.web.model.auth.request.LoginRequest;
import ru.example.account.web.model.auth.request.RefreshTokenRequest;
import ru.example.account.web.model.auth.response.AuthResponse;
import ru.example.account.web.model.auth.response.RefreshTokenResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityServiceImpl implements SecurityService {


    private final AuthenticationManager authenticationManager;

    private final JwtUtils jwtUtils;

    private final RefreshTokenServiceImpl refreshTokenService;

    private final UserRepository userRepository;

    @Transactional()
    public AuthResponse authenticationUser(LoginRequest loginRequest) {

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(
                        loginRequest.email(),
                        loginRequest.password()
                ));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                userDetails.getUser().getId()
        );


        return new AuthResponse(
                userDetails.getUser().getId(),
                jwtUtils.generateJwtToken(userDetails.getEmail(), userDetails.getUser().getId()),
                refreshToken.getTokenRefresh(),
                userDetails.getUsername(),
                userDetails.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList());
    }

    @Transactional()
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {

        return refreshTokenService.getByRefreshToken(request.tokenRefresh())
                .map(refreshTokenService::checkRefreshToken)
                .map(RefreshToken::getUserId)
                .map(userId -> {
                    User user = userRepository.findById(userId).orElseThrow(() ->
                            new RefreshTokenException("User not found with userId: " + userId));
                    refreshTokenService.deleteByUserId(userId);
                    String token = jwtUtils.generateTokenFromUsername(user.getUsername(), userId);
                    return new RefreshTokenResponse(token,
                            refreshTokenService.createRefreshToken(user.getId()).getTokenRefresh());
                })
                .orElseThrow(() -> new RefreshTokenException("Invalid token"));
    }

    @Transactional()
    public void logout() {

        var currentPrincipal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (currentPrincipal instanceof AppUserDetails userDetails) {
            refreshTokenService.deleteByUserId(userDetails.getId());
        }
    }
}
