package ru.example.account.web.controller;

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
