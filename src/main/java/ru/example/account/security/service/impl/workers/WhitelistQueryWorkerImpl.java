package ru.example.account.security.service.impl.workers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.example.account.security.service.WhitelistQueryWorker;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhitelistQueryWorkerImpl implements WhitelistQueryWorker {



    @Override
    public boolean isDeviceTrusted(Long userId, String accessToken, String fingerprint) {
        return false;
    }
}
