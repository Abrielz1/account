package ru.example.account.app.security.configuration;

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
import ru.example.account.app.security.jwt.JwtAuthenticationEntryPoint;
import ru.example.account.app.security.jwt.JwtTokenFilter;
import ru.example.account.app.security.service.impl.AccessDeniedHandlerImpl;
import java.util.List;
/**
 * Конфигурация безопасности Spring Security.
 * Настраивает JWT-аутентификацию, CORS и политику сессий.
 */
@Slf4j
@Configuration
@EnableMethodSecurity
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
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        log.info("Configuring authentication manager");
        return configuration.getAuthenticationManager();
    }
    /**
     * Настройка цепочки фильтров:
     * - Разрешает доступ к эндпоинтам аутентификации
     * - Требует роли USER для /api/v1/app/**
     * - Отключает CSRF и сессии
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity security) throws Exception {
        log.info("Configuring security filter chain");
        security.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**",
                                         "/v3/api-docs/**",
                                         "/swagger-ui/**",
                                         "/swagger-ui.html").permitAll()

                        .requestMatchers("/api/v1/app/user/**")
                        .hasRole("USER")

                        .anyRequest().authenticated())
                .exceptionHandling(configurer -> configurer
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler()))

                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("http://localhost:8080"));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE")); //
                    config.setAllowedHeaders(List.of("Content-Type", "Cache-Control", "Authorization"));
                    config.setAllowCredentials(true);
                    return config;
                }));

        return security.build();
    }
}
