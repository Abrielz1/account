package ru.example.account.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.example.account.web.model.auth.request.LoginRequest;
import ru.example.account.web.model.auth.response.AuthResponse;
import ru.example.account.web.model.usr.request.ManageUserEmailRequestDto;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest extends BaseIntegrationTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

   @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void accessProtectedEndpoint_WithoutToken_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/app/user"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateOtherUserData_ReturnsOk() throws Exception {
        // 1. Получаем токен
        String token = obtainAccessToken("john_shepard@gmail.com", "password");
        assertNotNull(token, "Token should not be null");

        // 2. Создаем запрос
        ManageUserEmailRequestDto request = new ManageUserEmailRequestDto("new@mail.com");

        // 3. Выполняем запрос с логированием
        mockMvc.perform(put("/api/v1/user/edit/email")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(result -> {
                    System.out.println("Response status: " + result.getResponse().getStatus());
                    System.out.println("Response content: " + result.getResponse().getContentAsString());
                });
    }

    private String obtainAccessToken(String email, String password) throws Exception {
        LoginRequest request = new LoginRequest(email, password);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/signing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(r -> {
                    System.out.println("Login response: " + r.getResponse().getContentAsString());
                })
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthResponse.class
        );

        return response.token();
    }
}
