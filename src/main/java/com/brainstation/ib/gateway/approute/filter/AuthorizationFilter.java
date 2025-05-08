package com.brainstation.ib.gateway.approute.filter;

import com.brainstation.ib.gateway.approute.CustomDataConfiguration;
import com.brainstation.ib.gateway.domain.dto.RequestDetails;
import com.brainstation.ib.gateway.domain.enums.ErrorMessages;
import com.brainstation.ib.gateway.domain.enums.SpecialChars;
import com.brainstation.ib.gateway.domain.enums.UserStatus;
import com.brainstation.ib.gateway.domain.enums.UserType;
import com.brainstation.ib.gateway.model.CurrentUserContext;
import com.brainstation.ib.gateway.model.ExternalTokenDto;
import com.brainstation.ib.gateway.model.RedisAccessToken;
import com.brainstation.ib.gateway.service.ApiAccessService;
import com.brainstation.ib.gateway.service.RedisService;
import com.brainstation.ib.gateway.util.*;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;

@Slf4j
@Order(-1)
@Component
@RequiredArgsConstructor
public class AuthorizationFilter implements GlobalFilter {

    final RedisService redisService;
    final ApiAccessService apiAccessService;

    @Value("${ib-service.encrypted-key}")
    private String encryptedKey;

    @Value("${ib-service.jwt-secret-key}")
    private String jwtSecretKey;

    @Value("${ENABLE_SESSION_DATA:false}")
    private boolean enableSessionData;

    @Value("${payload.encryption.secret.key}")
    protected String encryptionSecretKey;

    @Override
    @SneakyThrows
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return apiAccessService.isExternallyAccessibleApis(exchange.getRequest()).flatMap(isExternal -> {
            if (isExternal) {
                return checkExternalAuthorization(exchange, chain);
            } else {
                return manageUserAuthorization(chain, exchange);
            }
        });
    }

    private Mono<Void> checkExternalAuthorization(final ServerWebExchange exchange, final GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        final HttpHeaders httpHeaders = request.getHeaders();
        if (!httpHeaders.containsKey(CustomDataConfiguration.HEADER_TOKEN)) {
            return FilterValidationAndMapper.onError(exchange, ErrorMessages.AUTH_HEADER_MISSING);
        }

        try {
            final String token = httpHeaders.getFirst(CustomDataConfiguration.HEADER_TOKEN);
            final ExternalTokenDto externalTokenDto = AESUtils.extractToken(token, jwtSecretKey);
            assert externalTokenDto != null;
            final long expirationMillis = Long.parseLong(externalTokenDto.getExpiration());
            if (expirationMillis < System.currentTimeMillis()) {
                return FilterValidationAndMapper.onError(exchange, ErrorMessages.SESSION_TIMEOUT);
            }

            final CurrentUserContext currentUserContext = FilterValidationAndMapper.toUserContextDto(externalTokenDto.getClientIdentity(), null, null, null, null, null);
            final String jsonCurrentUserContext = JacksonUtil.objectToJson(currentUserContext);
            if (StringUtils.isBlank(jsonCurrentUserContext)) {
                return FilterValidationAndMapper.onError(exchange, ErrorMessages.INVALID_AUTH_TOKEN);
            }
            String base64UserCurrentContext = StringsUtils.toBase64(jsonCurrentUserContext.getBytes(StandardCharsets.UTF_8));

            if (StringUtils.isBlank(base64UserCurrentContext)) {
                return FilterValidationAndMapper.onError(exchange, ErrorMessages.INVALID_AUTH_TOKEN);
            }
            request.mutate().headers(h -> h.set(CustomDataConfiguration.HEADER_CURRENT_USER_CONTEXT, base64UserCurrentContext));
            request.mutate().headers(h -> h.set(CustomDataConfiguration.HEADER_CO_RELATION_ID, currentUserContext.getCoRelationId()));
            return chain.filter(exchange);
        } catch (Exception ex) {
            return FilterValidationAndMapper.onError(exchange, ErrorMessages.INVALID_AUTH_TOKEN);
        }
    }

    private Mono<Void> manageUserAuthorization(GatewayFilterChain chain, ServerWebExchange exchange) {
        final ServerHttpRequest request = exchange.getRequest();
        final String currentRequestPath = request.getPath().toString();
        final Flux<RequestDetails> allDecryptApiAccess = apiAccessService.getDecryptApiAccess();
        final Flux<String> allPublicAccessibleApiList = apiAccessService.getPublicApiAccess();
        final Flux<String> allAdminAccessibleApiList = apiAccessService.getUserApiAccess(UserType.ADMIN, UserStatus.ACTIVE);
        final Mono<Boolean> isPublicPath = apiAccessService.isPublicAccessibleApis(currentRequestPath, allPublicAccessibleApiList, allAdminAccessibleApiList);

        return isPublicPath.flatMap(isPublic -> {
            if (isPublic) {
                request.mutate().headers(h -> h.set(CustomDataConfiguration.HEADER_CO_RELATION_ID, CommonUtils.generateCorrelationId()));
                return chain.filter(exchange);
            }
            // Find user access token
            final var authorizationHeader = FilterValidationAndMapper.bearerAccessToken(request);
            if (authorizationHeader == null) {
                return FilterValidationAndMapper.onError(exchange, ErrorMessages.AUTH_HEADER_MISSING);
            }

            // Parse user access token
            final String[] authorizationValuePart = authorizationHeader.split(SpecialChars.SPACE.getText());
            if (FilterValidationAndMapper.isArrayLengthNotOk(authorizationValuePart, CustomDataConfiguration.TOKEN_HEADER_ARRAY_LENGTH) || FilterValidationAndMapper.isTokenPrefixNotOK(CustomDataConfiguration.TOKEN_PREFIX, authorizationValuePart)) {
                return FilterValidationAndMapper.onError(exchange, ErrorMessages.AUTH_HEADER_MISS_MATCH);
            }

            // Convert user identity from access token
            final String jwtToken = authorizationValuePart[1];
            final String userName;
            try {
                JWTClaimsSet claimsSet = JWTParser.parse(jwtToken).getJWTClaimsSet();
                if (claimsSet.getExpirationTime().getTime() < System.currentTimeMillis())
                    return FilterValidationAndMapper.onError(exchange, ErrorMessages.SESSION_TIMEOUT);
                userName = CryptoUtils.decrypt(claimsSet.getSubject(), encryptionSecretKey);
            } catch (ParseException e) {
                return FilterValidationAndMapper.onError(exchange, ErrorMessages.AUTH_HEADER_MISS_MATCH);
            }

            // Find AccessTokenRedis by userName
            // Create CurrentUserContext and put to header as 'CurrentContext'
            final RedisAccessToken redisAccessToken = redisService.accessToken(userName);
            if (redisAccessToken == null) {
                return FilterValidationAndMapper.onError(exchange, ErrorMessages.AUTH_HEADER_MISS_MATCH);
            }
            final CurrentUserContext currentUserContext = new CurrentUserContext();
            BeanUtils.copyProperties(redisAccessToken, currentUserContext);
            if (currentUserContext.getUserIdentity() == null) {
                return FilterValidationAndMapper.onError(exchange, ErrorMessages.INVALID_AUTH_TOKEN);
            }

            final String jsonCurrentUserContext = JacksonUtil.objectToJson(currentUserContext);
            if (jsonCurrentUserContext == null) {
                return FilterValidationAndMapper.onError(exchange, ErrorMessages.AUTH_HEADER_MISS_MATCH);
            }
            final String base64UserCurrentContext = StringsUtils.toBase64(jsonCurrentUserContext.getBytes(StandardCharsets.UTF_8));
            request.mutate().headers(h -> h.set(CustomDataConfiguration.HEADER_AUTHORIZATION, authorizationHeader));
            request.mutate().headers(h -> h.set(CustomDataConfiguration.HEADER_CURRENT_USER_CONTEXT, base64UserCurrentContext));
            request.mutate().headers(h -> h.set(CustomDataConfiguration.HEADER_CO_RELATION_ID, currentUserContext.getCoRelationId()));

            final Flux<String> userAccessibleApis = apiAccessService.getUserApiAccess(currentUserContext.getUserType(), UserStatus.ACTIVE);
            return apiAccessService.isUserAccessibleApis(currentRequestPath, userAccessibleApis).flatMap(isAccess -> {
                if (isAccess) {
                    return decryptHeaderSessionData(request, jwtToken).flatMap(canDecryptData -> {
                        if (canDecryptData) {
                            return isDecryptPass(request, allDecryptApiAccess).flatMap(isPass -> (isPass)
                                    ? chain.filter(exchange)
                                    : FilterValidationAndMapper.onError(exchange, ErrorMessages.DECRYPTION_FAILED));
                        }
                        return FilterValidationAndMapper.onError(exchange, ErrorMessages.SESSION_FAILED);
                    });
                }
                return FilterValidationAndMapper.onError(exchange, ErrorMessages.UNAUTHORIZED_RESOURCE_ACCESS);
            });
        });
    }

    private Mono<Boolean> isDecryptPass(ServerHttpRequest request, Flux<RequestDetails> decryptAccessibleApis) {
        return apiAccessService.isDecryptAccessibleApis(request.getPath().toString(), request.getMethod().name(), decryptAccessibleApis).map(m -> {
            if (m) {
                final var headerValue = request.getHeaders().getFirst(CustomDataConfiguration.HEADER_ENCRYPTED_DATA);
                final var decryptData = AESUtils.decrypt(headerValue, encryptedKey);
                return StringUtils.isNoneBlank(decryptData);
            }
            return true;
        });
    }

    private Mono<Boolean> decryptHeaderSessionData(ServerHttpRequest request, String token) {
        if (!enableSessionData) {
            return Mono.just(true);
        }
        try {
            final var sessionData = request.getHeaders().getFirst(CustomDataConfiguration.HEADER_SESSION_DATA);
            if (StringUtils.isEmpty(sessionData)) {
                return Mono.just(false);
            }

            final var sessionValue = redisService.session(token);
            if (StringUtils.isEmpty(sessionValue)) {
                return Mono.just(false);
            }

            final var decryptData = AESUtils.decrypt(sessionData, sessionValue);
            if (StringUtils.isEmpty(decryptData)) {
                return Mono.just(false);
            }

            final String[] parts = decryptData.split(SpecialChars.COLON.getText());
            if (parts.length >= 3) {
                redisService.sessionTTlUpdate(token, sessionValue);
                return Mono.just(true);
            }
        } catch (Exception ex) {
            log.error("Fail to process session id: {}", ex.getMessage());
        }
        return Mono.just(false);
    }
}
