package com.brainstation.ib.gateway.approute;

import com.google.common.net.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
public class SecurityInterceptor {
    final String HEADER = "default-src 'self'; script-src 'self' 'unsafe-inline' https://code.jquery.com https://cdn.jsdelivr.net; style-src 'self' 'unsafe-inline' https://fonts.googleapis.com https://cdn.jsdelivr.net; font-src 'self' https://fonts.gstatic.com;";

    public void applySecurityIntercept(ServerWebExchange exchange) {
        setCookiesSecurity(exchange);
        addHstsHeader(exchange);
        addCspHeader(exchange);
    }

    private void setCookiesSecurity(ServerWebExchange exchange) {
        exchange.getResponse().getCookies().values().forEach(cookies -> cookies.forEach(cookie -> {
            final ResponseCookie httpOnlyCookie = ResponseCookie.from(cookie.getName(), cookie.getValue())
                    .httpOnly(true)
                    .sameSite("Strict")
                    .secure(true)
                    .build();
            exchange.getResponse().addCookie(httpOnlyCookie);
        }));
    }

    private void addHstsHeader(ServerWebExchange exchange) {
        exchange.getResponse().getHeaders().add(HttpHeaders.STRICT_TRANSPORT_SECURITY, "max-age=31536000; includeSubDomains");
    }

    private void addCspHeader(ServerWebExchange exchange) {
        exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_SECURITY_POLICY, HEADER);
    }

}
