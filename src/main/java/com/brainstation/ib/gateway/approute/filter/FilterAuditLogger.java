package com.brainstation.ib.gateway.approute.filter;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@Order(value = Integer.MIN_VALUE)
@DependsOn({"authorizationFilter"})
public class FilterAuditLogger implements WebFilter {
    @Value("${ignore.header-param}")
    private String[] ignored;

    @NotNull
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, @NotNull WebFilterChain chain) {
        final long startTime = System.currentTimeMillis();
        if (exchange.getRequest().getMethod().equals(HttpMethod.OPTIONS)) {
            return chain.filter(exchange);
        }

        final var requestLog = new HashMap<String, Object>();
        requestLog.put("PATH", exchange.getRequest().getPath().value());
        requestLog.put("METHOD", exchange.getRequest().getMethod().name());
        requestLog.put("HEADER", getItem(exchange.getRequest().getHeaders()));
        requestLog.put("PARAM", getItem(exchange.getRequest().getQueryParams()));
        if (exchange.getRequest().getRemoteAddress() != null) {
            requestLog.put("REMOTE-ADDRESS", exchange.getRequest().getRemoteAddress().getHostString());
        }
        return chain.filter(exchange)
                .doOnSuccess(aVoid -> {
                    requestLog.put("DURATION", getDurations(startTime));
                    requestLog.put("STATUS", "SUCCESS");
                    log.info("REQUEST: {}", requestLog);
                })
                .doOnError(throwable -> {
                    requestLog.put("DURATION", getDurations(startTime));
                    requestLog.put("STATUS", "ERROR");
                    requestLog.put("STATUS-MESSAGE", throwable.getMessage());
                    log.info("REQUEST: {}", requestLog);
                });
    }

    private Map<String, String> getItem(MultiValueMap<String, String> multiValueMap) {
        Map<String, String> map = new HashMap<>();
        for (var m : multiValueMap.entrySet()) {
            try {
                map.put(m.getKey(), m.getValue().get(0));
                for (String k : ignored) {
                    if (k.equals(m.getKey().toLowerCase())) {
                        map.put(m.getKey(), "********");
                        break;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return map;
    }

    private long getDurations(Long startTime) {
        final long endTime = System.currentTimeMillis();
        return (endTime - startTime) / 1000;
    }
}
