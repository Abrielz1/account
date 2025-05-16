package ru.example.account.app.security.service.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.example.account.app.entity.EmailData;
import ru.example.account.app.entity.User;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class AppUserDetails implements UserDetails {

    private final User user;

    private final String email;

    private final Collection<? extends GrantedAuthority> authorities;

    public AppUserDetails(User user, String email) {
        this.user = user;
        this.email = this.findVerifiedUserEmail(email);
        this.authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .toList();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .toList();
    }

    public Long getId() {
        return user.getId();
    }

    public String findVerifiedUserEmail(String email) {
        return Optional.ofNullable(user.getUserEmails())
                .orElse(Collections.emptySet())
                .stream()
                .map(EmailData::getEmail)
                .filter(Objects::nonNull)
                .filter(e -> e.equals(email))
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> list.stream()
                                .findFirst()
                                .orElseThrow(() -> {
                                    log.warn("Email verification failed: {}", email);
                                    return new UsernameNotFoundException(
                                            "Email not registered: " + email
                                    );
                                })
                ));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
