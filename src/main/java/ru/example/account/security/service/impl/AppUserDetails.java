package ru.example.account.security.service.impl;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.example.account.user.entity.User;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class AppUserDetails implements UserDetails {
    // Делаем final для неизменяемости после создания
    private final Long id;
    private final String email;
    private final String password;
    private final String username; // Можем хранить username (логин) отдельно от email (principal)
    private final Collection<? extends GrantedAuthority> authorities;

    // --- НОВЫЕ ПОЛЯ ДЛЯ КОНТЕКСТА ---
    private final UUID sessionId;

    // Поле User делаем nullable. Оно будет заполнено, только если мы пришли из UserDetailsServiceImpl.
    private final transient User userEntity; // `transient` - чтобы не попадало в сериализацию

    // --- КОНСТРУКТОР №1: для UserDetailsServiceImpl (когда есть Entity) ---
    public AppUserDetails(User user) {
        this.userEntity = user;
        this.id = user.getId();
        this.email = user.getUsername(); // В нашей системе username это email
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.authorities = user.getRoles().stream()
                .map(GrantedAuthority.class::cast)
                .collect(Collectors.toSet());
        // В этом сценарии sessionId нам не известен, и это нормально
        this.sessionId = null;
    }

    // --- КОНСТРУКТОР №2: для JwtTokenFilter (когда Entity нет) ---
    public AppUserDetails(Long id, String email, Collection<? extends GrantedAuthority> authorities, UUID sessionId) {
        this.userEntity = null; // Нет живой сущности!
        this.id = id;
        this.email = email;
        this.username = email;
        this.password = ""; // Пароль не хранится в токене и не нужен на этом этапе
        this.authorities = authorities;
        this.sessionId = sessionId;
    }

    // --- Реализация методов интерфейса UserDetails ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        // Principal'ом в Spring Security является то, что возвращает этот метод
        return email;
    }

    // --- Всегда true, так как мы управляем этим через статусы сессий ---
    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
}

//package ru.example.account.security.service.impl;
//
//import lombok.Getter;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import ru.example.account.user.entity.EmailData;
//import ru.example.account.user.entity.User;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.Objects;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Getter
//public class AppUserDetails implements UserDetails {
//
//    private final User user;
//
//    private final String email;
//
//    private final Collection<? extends GrantedAuthority> authorities;
//
//    public AppUserDetails(User user, String email) {
//        this.user = user;
//        this.email = this.findVerifiedUserEmail(email);
//        this.authorities = user.getRoles().stream()
//                .map(role -> new SimpleGrantedAuthority(role.name()))
//                .toList();
//    }
//
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return user.getRoles().stream()
//                .map(role -> new SimpleGrantedAuthority(role.name()))
//                .toList();
//    }
//
//    public Long getId() {
//        return user.getId();
//    }
//
//    public String findVerifiedUserEmail(String email) {
//        return Optional.ofNullable(user.getUserEmails())
//                .orElse(Collections.emptySet())
//                .stream()
//                .map(EmailData::getEmail)
//                .filter(Objects::nonNull)
//                .filter(e -> e.equals(email))
//                .collect(Collectors.collectingAndThen(
//                        Collectors.toList(),
//                        list -> list.stream()
//                                .findFirst()
//                                .orElseThrow(() -> {
//                                    log.warn("Email verification failed: {}", email);
//                                    return new UsernameNotFoundException(
//                                            "Email not registered: " + email
//                                    );
//                                })
//                ));
//    }
//
//    @Override
//    public String getPassword() {
//        return user.getPassword();
//    }
//
//    @Override
//    public String getUsername() {
//        return user.getUsername();
//    }
//
//    @Override
//    public boolean isAccountNonExpired() {
//        return true;
//    }
//
//    @Override
//    public boolean isAccountNonLocked() {
//        return true;
//    }
//
//    @Override
//    public boolean isCredentialsNonExpired() {
//        return true;
//    }
//
//    @Override
//    public boolean isEnabled() {
//        return true;
//    }
//}
