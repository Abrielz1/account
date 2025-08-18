package ru.example.account.security.service.impl.worker;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.example.account.security.service.worker.HttpUtilsService;
import ru.example.account.shared.util.HttpUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class HttpUtilsServiceImpl implements HttpUtilsService {

    private final HttpUtils httpUtils;

    @Override
    public String getClientIpAddress(HttpServletRequest request) {
        return httpUtils.getClientIpAddress(request);
    }
}
