package ru.example.account.security.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.example.account.security.service.TimezoneService;
import ru.example.account.shared.util.TimezoneUtils;

import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimezoneServiceImpl implements TimezoneService {

    private final TimezoneUtils timezoneUtils;

    @Override
    public ZoneId getZoneIdFromRequest(HttpServletRequest request) {

        return this.timezoneUtils.getZoneIdFromRequest(request);
    }
}
