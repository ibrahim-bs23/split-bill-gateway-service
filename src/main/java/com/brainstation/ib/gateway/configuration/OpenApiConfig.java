package com.brainstation.ib.gateway.configuration;

import com.brainstation.ib.gateway.data.repository.ApiRouteRepository;
import com.brainstation.ib.gateway.domain.enums.RowStatus;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = {"springdoc.swagger-ui.enabled"}, matchIfMissing = true)
public class OpenApiConfig {
    private static final String MAX_AGE = "3600";
    private static final String ALLOWED_ORIGIN = "*";
    private static final String ALLOWED_HEADERS = "*";
    private static final String ALLOWED_METHODS = "*";

    final ApiRouteRepository apiRouteRepository;

    @Bean
    public WebFilter corsFilter() {
        return (ServerWebExchange ctx, WebFilterChain chain) -> {
            var request = ctx.getRequest();
            var response = ctx.getResponse();
            if (CorsUtils.isCorsRequest(request)) {
                HttpHeaders headers = response.getHeaders();
                headers.add("Access-Control-Max-Age", MAX_AGE);
                headers.add("Access-Control-Allow-Origin", ALLOWED_ORIGIN);
                headers.add("Access-Control-Allow-Methods", ALLOWED_METHODS);
                headers.add("Access-Control-Allow-Headers", ALLOWED_HEADERS);
                if (request.getMethod() == HttpMethod.OPTIONS) {
                    response.setStatusCode(HttpStatus.OK);
                    return Mono.empty();
                }
            }
            return chain.filter(ctx);
        };
    }

    @Bean
    public OpenAPI openAPI() {
        final var info = new Info().title("BS IB").description("BS IB Microservice").version("1.0.0");
        return new OpenAPI().info(info);
    }

    @Bean
    @Primary
    public SwaggerUiConfigParameters swaggerUiConfigParameters(final SwaggerUiConfigProperties swaggerUiConfigProperties) {
        final Map<String, String> routs = new HashMap<>();
        try {
            for (var item : apiRouteRepository.findAllByStatus(RowStatus.ACTIVE)) {
                final String name = item.getUri().replace("lb://", "");
                routs.put(name, String.format("/%s/v3/api-docs", name));
            }
        } catch (Exception ignored) {
        }

        final var urls = routs.entrySet().stream().map(m -> new AbstractSwaggerUiConfigProperties.SwaggerUrl(m.getKey(), m.getValue(), m.getKey())).collect(Collectors.toSet());
        swaggerUiConfigProperties.setUrls(urls);
        return new SwaggerUiConfigParameters(swaggerUiConfigProperties);
    }
}