package com.brainstation.ib.gateway.approute;

import com.brainstation.ib.gateway.approute.filter.AuthorizationFilter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

@Component
public class CustomGatewayFilterFactory extends AbstractGatewayFilterFactory<CustomDataConfiguration> {

    final AuthorizationFilter authorizationFilter;
    final SecurityInterceptor securityInterceptor;

    public CustomGatewayFilterFactory(AuthorizationFilter authorizationFilter, SecurityInterceptor securityInterceptor) {
        super(CustomDataConfiguration.class);
        this.authorizationFilter = authorizationFilter;
        this.securityInterceptor = securityInterceptor;
    }

    @Override
    public GatewayFilter apply(CustomDataConfiguration customDataConfiguration) {
        return (exchange, chain) -> {
            securityInterceptor.applySecurityIntercept(exchange);
            return authorizationFilter.filter(exchange, chain);
        };
    }
}
