package ru.example.account.shared.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.example.account.security.jwt.JwtUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class String2CRConverter {

    private final JwtUtils jwtUtils;

    public String convertIntoCRC(String teargetString) {

        try {

             return this.jwtUtils.createFingerprintHash(teargetString);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    private String convertByteArrayToHexString(byte[] arrayBytes) {

        var stringBuffer = new StringBuilder();

        for (byte arrayByte : arrayBytes) {
            stringBuffer.append(Integer.toString((arrayByte & 0xff) + 0x100, 16)
                    .substring(1));
        }

        return stringBuffer.toString();
    }
}
