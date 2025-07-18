package ru.example.account.security.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.example.account.security.service.FingerprintService;
import ru.example.account.shared.util.FingerprintUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class FingerprintServiceImpl implements FingerprintService {

    private final FingerprintUtils fingerprintUtils;

    @Override
    public String generateUsersFingerprint(HttpServletRequest request) {

        return fingerprintUtils.generate(request);
    }
}
