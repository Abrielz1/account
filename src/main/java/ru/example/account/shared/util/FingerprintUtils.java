package ru.example.account.shared.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.example.account.security.service.HttpUtilsService;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
@RequiredArgsConstructor
public class FingerprintUtils {

    private final HttpUtilsService httpUtils;

    public String generate(HttpServletRequest request) {

        String ipAddress = httpUtils.getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");

        String source = (ipAddress != null ? ipAddress : "") + (userAgent != null ? userAgent : "");

        if (source.isEmpty()) return "default_fingerprint";

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(source.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
