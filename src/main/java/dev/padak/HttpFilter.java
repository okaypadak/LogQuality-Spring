package dev.padak;


import jakarta.servlet.*;
import jakarta.servlet.FilterConfig;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Slf4j
public class HttpFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {

            MDC.put("requestId", generateRequestId());

            log.info("HTTP isteği alındı");

            // Diğer filtreleri ve isteği işleme devam et
            chain.doFilter(request, response);

            log.info("HTTP isteği tamamlandı");
        } finally {
            MDC.remove("requestId");
        }
    }

    @Override
    public void destroy() {
        // İhtiyaç halinde destroy işlemleri
    }

    public static String generateRequestId() {

        UUID uuid = UUID.randomUUID();
        long timestamp = System.currentTimeMillis();
        int hashCode = (uuid.toString() + timestamp).hashCode();
        return String.format("%010d", Math.abs(hashCode % 10000000000L));

    }
}