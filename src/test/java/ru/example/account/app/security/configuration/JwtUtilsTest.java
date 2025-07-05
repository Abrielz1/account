//package ru.example.account.app.security.configuration;
//
//
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.test.context.ActiveProfiles;
//import ru.example.account.security.jwt.JwtUtils;
//import java.time.Duration;
//
//@ActiveProfiles("test")
//@ExtendWith(MockitoExtension.class)
//class JwtUtilsTest {
//
//    @InjectMocks
//    private JwtUtils jwtUtils;
//
//    @BeforeEach
//    void setup() {
//
//        String temp = jwtUtils.getRawSecret();
//        if (temp == null) {
//            jwtUtils.setRawSecret("2LOwXuIKFaUCpDLTELLlbMDKyYVegPSPl2rbDMZw44ELCtmb53miY5KngrxfDWFPNsazESPPFHFM+bEdMGHtdg==");
//        }
//        jwtUtils.setTokenExpiration(Duration.ofMinutes(5));
//        // Если требуются подготовительные действия, выполняйте их здесь
//    }
//
//    @Test
//    void testDecodeValidSecret() {
//        String validSecret = "2LOwXuIKFaUCpDLTELLlbMDKyYVegPSPl2rbDMZw44ELCtmb53miY5KngrxfDWFPNsazESPPFHFM+bEdMGHtdg==";
//        jwtUtils.setRawSecret(validSecret);
//        jwtUtils.init();
//        Assertions.assertNotNull(jwtUtils.getSecretKey());
//    }
//
//    @Test
//    void testDecodeInvalidSecret() {
//        String invalidSecret = "INVALID_SECRET_WITH_WRONG_CHARACTERS";
//        jwtUtils.setRawSecret(invalidSecret);
//        Throwable thrown = Assertions.assertThrows(IllegalStateException.class, () -> {
//            jwtUtils.init();
//        });
//        Assertions.assertTrue(thrown.getMessage().contains("Invalid Base64"));
//    }
//}
