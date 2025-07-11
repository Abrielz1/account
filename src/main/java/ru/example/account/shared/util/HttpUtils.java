package ru.example.account.shared.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class HttpUtils {

    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    public String headerRipper(HttpServletRequest request) {

        if (request == null) {
            log.error("empty header was given!");
            return "";
        }

        for (String header: IP_HEADER_CANDIDATES) {

            if (StringUtils.hasText(request.getHeader(header)) && !"unknown".equalsIgnoreCase(request.getHeader(header))) {

                return request.getHeader(header).split(",")[0].trim();
            }
        }

        return  !(StringUtils.hasText(request.getRemoteAddr())) ? "" : request.getRemoteAddr();
    }
}
