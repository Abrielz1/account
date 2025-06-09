package ru.example.account.app.security.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.example.account.app.security.jwt.JwtUtils;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SecurityConfigurationTest {

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    void shouldGenerateAndValidateToken() {
        String token = jwtUtils.generateTokenFromUsername("testuser", 1L);
        assertTrue(jwtUtils.validateAccessToken(token));

        String username = jwtUtils.getUsernameFromToken(token);
        assertEquals("testuser", username);

        Long userId = jwtUtils.getUserIdFromClaimJwt(token);
        assertEquals(1L, userId.longValue());
    }
}