package com.brainstation.ib.gateway.service;

import com.brainstation.ib.gateway.approute.filter.FilterValidationAndMapper;
import com.brainstation.ib.gateway.data.entity.ExternalApiAccess;
import com.brainstation.ib.gateway.data.entity.PublicApiAccess;
import com.brainstation.ib.gateway.data.entity.UserApiAccess;
import com.brainstation.ib.gateway.data.entity.UserApiNotAccess;
import com.brainstation.ib.gateway.data.repository.*;
import com.brainstation.ib.gateway.domain.dto.RequestDetails;
import com.brainstation.ib.gateway.domain.enums.RowStatus;
import com.brainstation.ib.gateway.domain.enums.SpecialChars;
import com.brainstation.ib.gateway.domain.enums.UserStatus;
import com.brainstation.ib.gateway.domain.enums.UserType;
import com.brainstation.ib.gateway.util.StringsUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("all")
public class ApiAccessService {

    final RedisService redisService;

    final UserApiAccessRepository apiAccessRepository;
    final PublicApiAccessRepository publicApiAccessRepository;
    final DecryptApiAccessRepository decryptApiAccessRepository;
    final UserApiNotAccessRepository userApiNotAccessRepository;
    final ExternalApiAccessRepository externalApiAccessRepository;


    /**
     * Cacheable data
     */
    public Flux<String> getPublicApiAccess() {
        final var redisData = redisService.get(RedisService.REDIS_API_ACCESS_PUBLIC_KEY, String[].class);
        if (ArrayUtils.isEmpty(redisData)) {
            var item = publicApiAccessRepository.findPublicApisByStatus(RowStatus.ACTIVE).stream().map(PublicApiAccess::getUrl).toList();
            if (item.isEmpty()) {
                return Flux.fromIterable(List.of("/empty"));
            }
            redisService.set(RedisService.REDIS_API_ACCESS_PUBLIC_KEY, item);
            return Flux.fromIterable(item);
        }
        return Flux.just(redisData);
    }

    /**
     * Cacheable data
     */
    public Flux<String> getUserApiAccess(UserType userType, UserStatus userStatus) {
        String key = String.format(RedisService.REDIS_API_ACCESS_USER_KEY, userType.name(), userStatus.name());
        final var redisData = redisService.get(key, String[].class);
        if (ArrayUtils.isEmpty(redisData)) {
            var item = apiAccessRepository.findAllByUserTypeAndUserStatus(userType, userStatus).stream().map(UserApiAccess::getUrl).toList();
            if (item.isEmpty()) {
                return Flux.fromIterable(List.of("/empty"));
            }
            redisService.set(key, item);
            return Flux.fromIterable(item);
        }
        return Flux.just(redisData);
    }

    /**
     * Cacheable data
     */
    public Flux<String> getUserApiAccessNot() {
        String key = String.format(RedisService.REDIS_API_ACCESS_USER_NOT_KEY);
        final var redisData = redisService.get(key, String[].class);
        if (ArrayUtils.isEmpty(redisData)) {
            var item = userApiNotAccessRepository.findAllByStatus(RowStatus.ACTIVE).stream().map(UserApiNotAccess::getUrl).toList();
            if (item.isEmpty()) {
                return Flux.fromIterable(List.of("/empty"));
            }
            redisService.set(key, item);
            return Flux.fromIterable(item);
        }
        return Flux.just(redisData);
    }

    /**
     * Cacheable data
     */
    public Flux<RequestDetails> getDecryptApiAccess() {
        final var redisData = redisService.get(RedisService.REDIS_API_ACCESS_DECRYPT_KEY, RequestDetails[].class);
        if (ArrayUtils.isEmpty(redisData)) {
            var item = decryptApiAccessRepository.findAllByStatus(RowStatus.ACTIVE).stream().map(m -> new RequestDetails().setMethod(m.getMethod()).setPath(m.getPath())).toList();
            if (item.isEmpty()) {
                return Flux.fromIterable(List.of(new RequestDetails().setMethod("GET").setPath("/empty")));
            }
            redisService.set(RedisService.REDIS_API_ACCESS_DECRYPT_KEY, item);
            return Flux.fromIterable(item);
        }
        return Flux.just(redisData);
    }

    /**
     * Cacheable data
     */
    public Flux<String> getExternalApiAccess() {
        final var redisData = redisService.get(RedisService.REDIS_API_ACCESS_EXTERNAL_KEY, String[].class);
        if (ArrayUtils.isEmpty(redisData)) {
            var item = externalApiAccessRepository.findAllByStatus(RowStatus.ACTIVE).stream().map(ExternalApiAccess::getUri).toList();
            if (item.isEmpty()) {
                return Flux.fromIterable(List.of("/empty"));
            }
            redisService.set(RedisService.REDIS_API_ACCESS_EXTERNAL_KEY, item);
            return Flux.fromIterable(item);
        }
        return Flux.just(redisData);
    }

    public Mono<Boolean> isExternallyAccessibleApis(ServerHttpRequest request) {
        final String requestPath = request.getPath().toString();
        final Flux<String> externalCallApiList = this.getExternalApiAccess();
        final Flux<String> adminPermittedUrls = this.getUserApiAccess(UserType.ADMIN, UserStatus.ACTIVE);
        return adminPermittedUrls
                .any(url -> url.equals(requestPath))
                .defaultIfEmpty(false)
                .flatMap(adminPermittedCheck -> {
                    if (adminPermittedCheck) {
                        return Mono.just(false);
                    } else {
                        return externalCallApiList
                                .filter(url -> url.contains(Character.toString(SpecialChars.STAR.getCharacter())))
                                .next()
                                .map(url -> true)
                                .switchIfEmpty(Mono.just(requestPath)
                                        .map(path -> StringsUtils.trimTrailing(path, SpecialChars.FORWARD_SLASH.getCharacter()))
                                        .flatMap(trimmedPath ->
                                                externalCallApiList
                                                        .filter(sourceUrl -> FilterValidationAndMapper.isPathMatched(sourceUrl, trimmedPath))
                                                        .next().map(url -> true))
                                        .switchIfEmpty(Mono.just(false)));
                    }
                });
    }

    public Mono<Boolean> isUserAccessibleApis(String path, Flux<String> userAccessibleApis) {
        return userAccessibleApis.any(userApiPath -> {
            final String regex = userApiPath.replaceAll("\\{([^}]+)\\}", "(?<$1>[^/]+)");
            return path.matches(regex);
        });
    }

    public Mono<Boolean> isDecryptAccessibleApis(String path, String method, Flux<RequestDetails> decryptAccessibleApis) {
        return decryptAccessibleApis.collectList().map(list -> {
            if (!list.isEmpty()) {
                for (var userApiPath : list) {
                    if (path.matches(userApiPath.getPath().replaceAll("\\{([^}]+)\\}", "(?<$1>[^/]+)")) && method.equals(userApiPath.getMethod())) {
                        return true;
                    }
                }
            }
            return false;
        });
    }

    public Mono<Boolean> isPublicAccessibleApis(String requestPath, Flux<String> permittedUrls, Flux<String> adminPermittedUrls) {
        return adminPermittedUrls
                .any(url -> url.equals(requestPath))
                .defaultIfEmpty(false)
                .flatMap(adminPermittedCheck -> {
                    if (adminPermittedCheck) {
                        return Mono.just(false);
                    } else {
                        return permittedUrls
                                .filter(url -> url.endsWith("**"))
                                .map(url -> url.replace("*", ""))
                                .filter(requestPath::startsWith)
                                .next()
                                .map(url -> true)
                                .switchIfEmpty(
                                        Mono
                                                .just(requestPath)
                                                .map(path -> StringsUtils.trimTrailing(path, SpecialChars.FORWARD_SLASH.getCharacter()))
                                                .flatMap(trimmedPath -> permittedUrls.filter(sourceUrl -> FilterValidationAndMapper.isPathMatched(sourceUrl, trimmedPath)).next().map(url -> true))
                                                .switchIfEmpty(Mono.just(false))
                                );
                    }
                });
    }
}
