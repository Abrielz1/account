package ru.example.account.security.service.impl.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.example.account.security.dto.in.ClientRegisterRequestDto;
import ru.example.account.security.service.worker.TokenCreationWorker;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCreationWorkerImpl implements TokenCreationWorker {

    private static final String HEADER = "HVTd6J*y%_";

    private static final String TAIL = "_NdhfKid*";

    @Override
    public String createToken(ClientRegisterRequestDto request) {

        StringBuilder result = new StringBuilder();
        result.append(request.userName())
              .append("_")
              .append(request.phone())
              .append("_")
              .append(request.email())
              .append("_");


        String resultToken;

        try {
            resultToken = this.convertByteArrayToHexString(MessageDigest.getInstance("SHA-1")
                    .digest(result.toString().getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return resultToken;
    }

    private String convertByteArrayToHexString(byte[] arrayBytes) {

        var stringBuffer = new StringBuilder();

        for (byte arrayByte : arrayBytes) {
            stringBuffer.append(Integer.toString((arrayByte & 0xff) + 0x100, 16)
                    .substring(1));
        }


        return HEADER + stringBuffer + TAIL;
    }
}
