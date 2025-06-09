package ru.example.account.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.example.account.app.security.jwt.JwtUtils;
import ru.example.account.web.model.auth.request.LoginRequest;
import ru.example.account.web.model.auth.response.AuthResponse;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@SpringBootTest
//@AutoConfigureMockMvc
public class AuhTest {

//    @Autowired
//    private MockMvc mockMvc;
//
//   @Autowired
//   private JwtUtils jwtUtils;
//
//   @Autowired
//   private ObjectMapper objectMapper;
//
//    @Test
//    @Sql(scripts = {"/data/cleanUp.sql", "/data/insertData.sql"})
//    public void testLogin() throws Exception {
//
//        LoginRequest request = new LoginRequest("john_shepard@gmail.com", "password");
//        String userJason = objectMapper.writeValueAsString(request);
//
//        String tokenJson =  mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/signing")
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(userJason)
//                        ).andExpect(status().isOk())
//                                .andReturn()
//                                .getResponse()
//                                .getContentAsString();
//
//        AuthResponse response = objectMapper.readValue(tokenJson, AuthResponse.class);
//
//        Assertions.assertEquals(request.email(), jwtUtils.getUsernameFromToken(response.token()));
//    }

}
