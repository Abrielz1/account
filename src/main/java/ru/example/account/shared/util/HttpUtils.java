package ru.example.account.shared.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class HttpUtils {

    // Список всех возможных заголовков, где может "прятаться" реальный IP
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
            "REMOTE_ADDR" // В самом конце проверяем стандартный Remote_Addr
    };

    /**
     * "Умный" метод для получения реального IP-адреса клиента.
     * Он последовательно проверяет список стандартных прокси-заголовков.
     * Это необходимо, когда приложение работает за Nginx, балансировщиком или в облаке.
     */

    public String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            log.warn("Attempt to get IP from a null HttpServletRequest.");
            return "unknown_ip";
        }

        // Итерируемся по списку заголовков
        for (String headerName : IP_HEADER_CANDIDATES) {
            String ipAddress = request.getHeader(headerName);

            // Если заголовок найден и он не пустой и не "unknown"
            if (StringUtils.hasText(ipAddress) && !"unknown".equalsIgnoreCase(ipAddress)) {

                // В заголовке 'X-Forwarded-For' может быть несколько IP, разделенных запятой.
                // Нам нужен самый первый (самый левый), так как это и есть реальный IP клиента.
                // Остальные - это IP промежуточных прокси.
                return ipAddress.split(",")[0].trim();
            }
        }

        // Если ни один из заголовков не найден, возвращаем стандартный RemoteAddr.
        String remoteAddr = request.getRemoteAddr();
        return StringUtils.hasText(remoteAddr) ? remoteAddr : "unknown_ip";
    }
}
