package ru.example.account.shared.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Component
public class String2CRConverter {

    public String convertIntoCRC(String teargetString) {
        try {
             return this.convertByteArrayToHexString(MessageDigest.getInstance("SHA-1")
                    .digest(teargetString.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
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
