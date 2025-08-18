package ru.example.account.security.service.worker;

import jakarta.servlet.http.HttpServletRequest;

public interface HttpUtilsService {

    String getClientIpAddress(HttpServletRequest request);
}
