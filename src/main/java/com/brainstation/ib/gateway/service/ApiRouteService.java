package com.brainstation.ib.gateway.service;

import com.brainstation.ib.gateway.data.entity.ApiRoute;
import com.brainstation.ib.gateway.data.repository.ApiRouteRepository;
import com.brainstation.ib.gateway.domain.enums.RowStatus;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class ApiRouteService {

    final RedisService redisService;
    final ApiRouteRepository apiRouteRepository;


    /**
     * Cacheable data
     */
    public Flux<ApiRoute> getActiveApiRoute() {
        final var redisData = redisService.get(RedisService.REDIS_API_ROUTE_KEY, ApiRoute[].class);
        if (ArrayUtils.isEmpty(redisData)) {
            var item = apiRouteRepository.findAllByStatus(RowStatus.ACTIVE);
            if (item.isEmpty()) {
                return Flux.fromIterable(new ArrayList<>());
            }
            redisService.set(RedisService.REDIS_API_ROUTE_KEY, item);
            return Flux.fromIterable(item);
        }
        return Flux.just(redisData);
    }
}
