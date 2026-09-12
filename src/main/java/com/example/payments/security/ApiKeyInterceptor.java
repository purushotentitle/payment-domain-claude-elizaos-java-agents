package com.example.payments.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public final class ApiKeyInterceptor implements HandlerInterceptor {
    private final String configuredApiKey;

    public ApiKeyInterceptor(@Value("${payments.api-key:}") String configuredApiKey) {
        this.configuredApiKey = configuredApiKey;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (configuredApiKey == null || configuredApiKey.isBlank()) {
            return true;
        }

        String path = request.getRequestURI();
        if (isPublicPath(path)) {
            return true;
        }

        String providedApiKey = request.getHeader("X-API-Key");
        if (configuredApiKey.equals(providedApiKey)) {
            return true;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"unauthorized\",\"message\":\"Missing or invalid X-API-Key header\"}");
        return false;
    }

    private boolean isPublicPath(String path) {
        return path.equals("/health")
                || path.startsWith("/actuator")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }
}
