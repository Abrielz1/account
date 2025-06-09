package ru.example.account.app.security.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.account.app.entity.Account;
import ru.example.account.app.entity.EmailData;
import ru.example.account.app.entity.PhoneData;
import ru.example.account.app.entity.RefreshToken;
import ru.example.account.app.entity.RoleType;
import ru.example.account.app.entity.User;
import ru.example.account.app.repository.AccountRepository;
import ru.example.account.app.repository.EmailDataRepository;
import ru.example.account.app.repository.PhoneDataRepository;
import ru.example.account.app.repository.UserRepository;
import ru.example.account.app.security.jwt.JwtUtils;
import ru.example.account.app.security.service.SecurityService;
import ru.example.account.util.exception.exceptions.RefreshTokenException;
import ru.example.account.web.model.auth.request.LoginRequest;
import ru.example.account.web.model.auth.request.RefreshTokenRequest;
import ru.example.account.web.model.auth.request.UserCredentialsRegisterRequestDto;
import ru.example.account.web.model.auth.response.AuthResponse;
import ru.example.account.web.model.auth.response.RefreshTokenResponse;
import ru.example.account.web.model.auth.response.UserCredentialsResponseDto;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityServiceImpl implements SecurityService {


    private final AuthenticationManager authenticationManager;

    private final AccountRepository accountRepository;

    private final EmailDataRepository emailDataRepository;

    private final PhoneDataRepository phoneDataRepository;

    private final JwtUtils jwtUtils;

    private final RefreshTokenServiceImpl refreshTokenService;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserCredentialsResponseDto register(UserCredentialsRegisterRequestDto requestDto) {

        Account account = new Account();
        account.setBalance(new BigDecimal("10.00"));
        account.setInitialBalance(new BigDecimal("10.00"));

        // Создаем пользователя
        User user = new User();
        user.setUsername(requestDto.username());
        user.setPassword(passwordEncoder.encode(requestDto.password()));
        user.setDateOfBirth(requestDto.birthDate());
        user.setRoles(new HashSet<>(requestDto.roles()));
        user.setUserAccount(account);  // Связь пользователя с аккаунтом

        // Создаем email
        EmailData emailData = new EmailData();
        emailData.setEmail(requestDto.email());
        emailData.setUser(user);  // Связь email с пользователем

        // Создаем телефон
        PhoneData phoneData = new PhoneData();
        phoneData.setPhone(requestDto.phoneNumber());
        phoneData.setUser(user);  // Связь телефона с пользователем

        // Устанавливаем коллекции
        user.setUserEmails(Set.of(emailData));
        user.setUserPhones(Set.of(phoneData));

        // Сохраняем все через каскадирование
        user = userRepository.save(user);

        return new UserCredentialsResponseDto(
                user.getUserEmails().stream().map(EmailData::getEmail).collect(Collectors.toSet()),
                user.getPassword()
        );
    }

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
