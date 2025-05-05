package com.brainstation.ib.gateway.approute;

import com.brainstation.ib.gateway.data.entity.ApiRoute;
import com.brainstation.ib.gateway.service.ApiRouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

//TODO CURRENTLY IT USE application.yml AND DISABLE FOR PROGRAMMATICALLY
//@Configuration
@RequiredArgsConstructor
public class CustomRouteLocator implements RouteLocator {
    final ApiRouteService apiRouteService;
    final RouteLocatorBuilder routeLocatorBuilder;
    final CustomGatewayFilterFactory gatewayFilterFactory;

    @Override
    public Flux<Route> getRoutes() {
        final GatewayFilter gatewayFilter = gatewayFilterFactory.apply(new CustomDataConfiguration());
        final RouteLocatorBuilder.Builder routesBuilder = routeLocatorBuilder.routes();
        return apiRouteService.getActiveApiRoute()
                .map(apiRoute -> routesBuilder.route(String.valueOf(apiRoute.getId()), predicateSpec -> setPredicateSpec(apiRoute, predicateSpec, gatewayFilter)))
                .collectList()
                .flatMapMany(builders -> routesBuilder.build().getRoutes());
    }

    private Buildable<Route> setPredicateSpec(ApiRoute apiRoute, PredicateSpec predicateSpec, GatewayFilter gatewayFilter) {
        final BooleanSpec booleanSpec = predicateSpec.path(apiRoute.getPath());
        if (StringUtils.hasLength(apiRoute.getMethod())) {
            booleanSpec.and().method(apiRoute.getMethod());
        }
        final UriSpec uriSpec = booleanSpec.filters(gatewayFilterSpec -> {
            gatewayFilterSpec.filter(gatewayFilter);
            return gatewayFilterSpec;
        });
        return uriSpec.uri(apiRoute.getUri());
    }
}