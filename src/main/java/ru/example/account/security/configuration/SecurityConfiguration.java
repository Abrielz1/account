package ru.example.account.security.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import ru.example.account.security.jwt.JwtAuthenticationEntryPoint;
import ru.example.account.security.jwt.JwtTokenFilter;
import ru.example.account.security.service.impl.AccessDeniedHandlerImpl;
import java.util.Arrays;
import java.util.List;
import ru.example.account.user.entity.RoleType;

@Slf4j
@Configuration
@EnableMethodSecurity(prePostEnabled = true) // Включаем @PreAuthorize, @PostAuthorize
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtTokenFilter jwtTokenFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new AccessDeniedHandlerImpl(objectMapper);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Убираем магическое число `12`, используем дефолт
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Для продакшена здесь должен быть конкретный URL фронтенда
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:8080"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        // Добавляем наш кастомный заголовок для фингерпринта!
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Fingerprint"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler())
                )
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // --- Зона, открытая для всех ---
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**"
                        ).permitAll()

                        // --- Зона Службы Безопасности (СБ) ---
                        // Имена ролей теперь точно соответствуют их функциям.
                        .requestMatchers("/api/v1/security/officer/**").hasAnyAuthority(
                                RoleType.ROLE_SECURITY_OFFICER.name(),
                                RoleType.ROLE_SECURITY_SUPERVISOR.name(),
                                RoleType.ROLE_SECURITY_TOP_SUPERVISOR.name(),
                                RoleType.ROLE_ADMIN.name() // Админ может все
                        )
                        .requestMatchers("/api/v1/security/supervisor/**").hasAnyAuthority(
                                RoleType.ROLE_SECURITY_SUPERVISOR.name(),
                                RoleType.ROLE_SECURITY_TOP_SUPERVISOR.name(),
                                RoleType.ROLE_ADMIN.name()
                        )
                        // ... и так далее

                        // --- Зона Бизнес-Менеджмента ---
                        // Этот супервайзер "впаривает карты", а не ищет хакеров.
                        .requestMatchers("/api/v1/management/manager/**").hasAnyAuthority(
                                RoleType.ROLE_MANAGER.name(),
                                RoleType.ROLE_SENIOR_MANAGER.name(),
                                RoleType.ROLE_TOP_MANAGEMENT.name(),
                                RoleType.ROLE_ADMIN.name()
                        )
                        .requestMatchers("/api/v1/management/top/**").hasAnyAuthority(
                                RoleType.ROLE_TOP_MANAGEMENT.name(),
                                RoleType.ROLE_ADMIN.name()
                        )

                        // --- Другие зоны (пример) ---
                        .requestMatchers("/api/v1/support/**").hasAuthority(RoleType.ROLE_TECH_SUPPORT.name())
                        .requestMatchers("/api/v1/admin/**").hasAuthority(RoleType.ROLE_ADMIN.name())

                        // Политика по умолчанию: любой другой запрос требует аутентификации.
                        // Конкретные права доступа будут проверяться через @PreAuthorize на методах.
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}


//package ru.example.account.security.configuration;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.access.AccessDeniedHandler;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import ru.example.account.security.jwt.JwtAuthenticationEntryPoint;
//import ru.example.account.security.jwt.JwtTokenFilter;
//import ru.example.account.security.service.impl.AccessDeniedHandlerImpl;
//import ru.example.account.user.entity.RoleType;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//import java.util.List;
//
///**
// * Конфигурация безопасности Spring Security.
// * Настраивает JWT-аутентификацию, CORS и политику сессий.
// */
//@Slf4j
//@Configuration
//@EnableMethodSecurity
//@RequiredArgsConstructor
//public class SecurityConfiguration {
//
//    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
//
//    private final JwtTokenFilter jwtTokenFilter;
//
//    private final ObjectMapper objectMapper;
//
//    @Bean
//    public AccessDeniedHandler accessDeniedHandler() {
//        return new AccessDeniedHandlerImpl(objectMapper);
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder(12);
//    }
//
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
//
//        log.info("Configuring authentication manager");
//        return configuration.getAuthenticationManager();
//    }
//
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(List.of("http://localhost:8080", "http://localhost:3000"));
//        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
//        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Fingerprint"));
//        configuration.setAllowCredentials(true);
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
//
//    // --- ГЛАВНЫЙ КОНФИГУРАЦИОННЫЙ БИН ---
//    /**
//     * Настраивает всю цепочку фильтров безопасности.
//     */
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//                .csrf(AbstractHttpConfigurer::disable)
//                .httpBasic(AbstractHttpConfigurer::disable)
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//
//                .exceptionHandling(exceptions -> exceptions
//                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
//                        .accessDeniedHandler(accessDeniedHandler()) // Используем наш бин
//                )
//
//                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
//
//                .authorizeHttpRequests(auth -> auth
//                        // --- ПУБЛИЧНАЯ ЗОНА ---
//                        .requestMatchers(
//                                "/api/v1/auth/**",      // Все, что связано с регистрацией, логином, рефрешем
//                                "/v3/api-docs/**",      // OpenAPI спецификация
//                                "/swagger-ui/**",       // Интерфейс Swagger
//                                "/swagger-ui.html"
//                        ).permitAll()
//
//                        // --- ЗОНА БЕЗОПАСНОСТИ ---
//                        // Эндпоинты для младших офицеров (и всех, кто выше)
//                        .requestMatchers("/api/v1/security/officer/**").hasAnyAuthority(
//                                RoleType.ROLE_SECURITY_OFFICER.name(),
//                                RoleType.ROLE_SECURITY_SUPERVISOR.name(),
//                                RoleType.ROLE_SECURITY_TOP_SUPERVISOR.name(),
//                                RoleType.ROLE_ADMIN.name()
//                        )
//                        // Эндпоинты для супервайзеров СБ (и всех, кто выше)
//                        .requestMatchers("/api/v1/security/supervisor/**").hasAnyAuthority(
//                                RoleType.ROLE_SECURITY_SUPERVISOR.name(),
//                                RoleType.ROLE_SECURITY_TOP_SUPERVISOR.name(),
//                                RoleType.ROLE_ADMIN.name()
//                        )
//                        // ... и так далее для топ-безопасников
//
//                        // --- ЗОНА ТЕХПОДДЕРЖКИ ---
//                        // Могут, например, смотреть профили пользователей, но не менять их
//                        .requestMatchers("/api/v1/support/**").hasAnyAuthority(
//                                RoleType.ROLE_TECH_SUPPORT.name(),
//                                RoleType.ROLE_ADMIN.name()
//                        )
//
//                        // --- ЗОНА ОПЕРАЦИОННОГО УПРАВЛЕНИЯ ---
//                        // Супервайзеры (менеджеры) и их начальство
//                        .requestMatchers("/api/v1/supervisor/**").hasAnyAuthority(
//                                RoleType.ROLE_SUPERVISOR.name(),
//                                RoleType.ROLE_TOP_MANAGEMENT.name(),
//                                RoleType.ROLE_ADMIN.name()
//                        )
//
//                        // --- ЗОНА АДМИНИСТРАТОРОВ СИСТЕМЫ ---
//                        // Имеют доступ к своим эндпоинтам (и, по сути, ко всем остальным)
//                        .requestMatchers("/api/v1/admin/**").hasAuthority(RoleType.ROLE_ADMIN.name())
//
//                        // --- ЗОНА ОБЫЧНЫХ КЛИЕНТОВ ---
//                        // Самые ограниченные права, например, на управление своим профилем
//                        .requestMatchers("/api/v1/client/**").hasAuthority(RoleType.ROLE_CLIENT.name())
//
//                        // --- ПОЛИТИКА ПО УМОЛЧАНИЮ ---
//                        // Любой другой запрос (например, /api/some-other-feature), который я забыл описать,
//                        // требует, чтобы пользователь был просто аутентифицирован.
//                        // Дальнейшую, более тонкую проверку сделает @PreAuthorize на сервисе.
//                        .anyRequest().authenticated()
//                );
//
//        return http.build();
//    }
//}
