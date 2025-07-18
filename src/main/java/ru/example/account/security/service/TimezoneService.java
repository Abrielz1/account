package ru.example.account.security.service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.ZoneId;

public interface TimezoneService {

    ZoneId getZoneIdFromRequest(HttpServletRequest request);
}
