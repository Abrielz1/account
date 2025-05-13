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
import ru.example.account.util.exception.exceptions.UserNotFoundException;
import ru.example.account.web.model.auth.request.LoginRequest;
import ru.example.account.web.model.auth.request.RefreshTokenRequest;
import ru.example.account.web.model.auth.response.AuthResponse;
import ru.example.account.web.model.auth.response.RefreshTokenResponse;
import java.util.List;

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
                        loginRequest.username(),
                        loginRequest.password()
                ));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        User user = this.getUserByUsername(userDetails.getUsername());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        return new AuthResponse(user.getId(),
                jwtUtils.generateJwtToken(user.getUsername()),
                refreshToken.getTokenRefresh(),
                user.getUsername(),
                roles);
    }

    @Transactional()
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {

        String requestTokenRefresh = request.tokenRefresh();

        return refreshTokenService.getByRefreshToken(requestTokenRefresh)
                .map(refreshTokenService::checkRefreshToken)
                .map(RefreshToken::getUserId)
                .map(userId -> {
                    User user = userRepository.findById(userId).orElseThrow(() ->
                            new RefreshTokenException("Exception for userId: " + userId)); // падает

                    String token = jwtUtils.generateTokenFromUsename(user.getUsername());
                    return new RefreshTokenResponse(token,
                            refreshTokenService.createRefreshToken(user.getId()).getTokenRefresh());
                })
                .orElseThrow(() -> new RefreshTokenException("RefreshToken is not found!"));
    }

    @Transactional()
    public void logout() {

        var currentPrincipal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (currentPrincipal instanceof AppUserDetails userDetails) {
            refreshTokenService.deleteByUserId(userDetails.getId());
        }
    }

    private User getUserByUsername(String username) {

       return userRepository.findByUsername(username).orElseThrow(() -> {
            log.error("No user in db");
            return new UserNotFoundException("No user in db");
        });
    }
}
