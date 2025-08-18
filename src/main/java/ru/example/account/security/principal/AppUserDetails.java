package ru.example.account.security.principal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.example.account.user.entity.User;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class AppUserDetails implements UserDetails {
    // Делаем final для неизменяемости после создания

    private final Long id;

    private final String email; // Используется как principal name

    @JsonIgnore // Пароль не должен сериализоваться
    private final String password;

    // Реальный username, может отличаться
    private final String username; // Можем хранить username (логин) отдельно от email (principal)

    private final Collection<? extends GrantedAuthority> authorities;

    // --- НОВЫЕ ПОЛЯ ДЛЯ КОНТЕКСТА ---
    private final UUID sessionId;

    private final Instant expiration;

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
                .map(roleEnum -> new SimpleGrantedAuthority(roleEnum.name()))
                .collect(Collectors.toSet());
        // В этом сценарии sessionId нам не известен, и это нормально
        this.sessionId = null;
        this.expiration = null;
    }

    // --- КОНСТРУКТОР №2: для JwtTokenFilter (когда Entity нет) ---
    public AppUserDetails(Long id, String email, Collection<? extends GrantedAuthority> authorities, UUID sessionId, Instant expiration) {
        this.userEntity = null; // Нет живой сущности!
        this.id = id;
        this.email = email;
        this.username = email;// Для простоты считаем их одинаковыми
        this.password = ""; // Пароль не хранится в токене и не нужен на этом этапе
        this.authorities = authorities;
        this.sessionId = sessionId;
        this.expiration = expiration;
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
