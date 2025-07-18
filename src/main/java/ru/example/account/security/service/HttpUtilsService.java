package ru.example.account.security.service;

import jakarta.servlet.http.HttpServletRequest;

public interface HttpUtilsService {

    String getClientIpAddress(HttpServletRequest request);
}
