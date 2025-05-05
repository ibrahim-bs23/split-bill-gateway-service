package com.brainstation.ib.gateway.controller;


import com.brainstation.ib.gateway.data.repository.PublicApiAccessRepository;
import com.brainstation.ib.gateway.data.repository.UserApiAccessRepository;
import com.brainstation.ib.gateway.domain.dto.ApiAccessRequest;
import com.brainstation.ib.gateway.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ApiReaderController {
    final RedisService redisService;
    final UserApiAccessRepository userApiAccessRepository;
    final PublicApiAccessRepository publicApiAccessRepository;

    @Value("${ib-service.auth-service-path}")
    private String authServiceBasePath;

    @GetMapping(value = "/api/caches/clean", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> cleanCaches() {
        redisService.cleanCaches();
        return Mono.just("{\"status\":true}");
    }

    @PostMapping(value = "/api/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> post(@RequestBody List<ApiAccessRequest> requestList) {
        cleanCaches().subscribe();
        /*TODO for (var data : requestList) {
            if (!data.isPublic()) {
                for (var userType : data.getUserType()) {
                    for (var userStatus : data.getUserStatus()) {
                        userApiAccessRepository.existsByUrlAndUserTypeAndUserStatus(data.getPath(), UserType.valueOf(userType), UserStatus.valueOf(userStatus)).map(m -> {
                            if (!m) {
                                userApiAccessRepository.save(new UserApiAccess().setUserType(UserType.valueOf(userType)).setUserStatus(UserStatus.valueOf(userStatus)).setUrl(data.getPath())).subscribe();
                            }
                            return Mono.empty();
                        }).subscribe();
                    }
                }
            } else {
                publicApiAccessRepository.saveIfNotExist(data.getPath()).subscribe();
            }
        }*/
        return Mono.just("{\"status\":true}");
    }

    @GetMapping(value = "/api/well-known", consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> wellKnown(@RequestHeader(required = false, name = "x-forwarded-host") String xHost, @RequestHeader(required = false) String host, @RequestHeader(required = false) String referer) throws Exception {
        String newSchema = (referer != null ? new URL(referer).getProtocol() : "http");
        String newHost = (referer != null ? new URL(referer).getHost() + ":" + (new URL(referer).getPort() > 0 ? new URL(referer).getPort() : new URL(referer).getProtocol().equals("http") ? 80 : 443) : (xHost != null ? xHost : host));
        final String openIdUrl = String.format(
                "%s://%s/%s",
                newSchema,
                newHost,
                authServiceBasePath);
        return Mono.just("""
                {
                  "issuer": "{origin}",
                  "jwks_uri": "{origin}/auth/jwks",
                  "token_endpoint": "{origin}/dev-test/login",
                  "revocation_endpoint": "{origin}/auth/revoke",
                  "introspection_endpoint": "{origin}/auth/introspect",
                  "authorization_endpoint": "{origin}/auth/authorize",
                  "token_endpoint_auth_methods_supported": [
                    "client_secret_post"
                  ],
                  "response_types_supported": [
                    "password"
                  ],
                  "grant_types_supported": [
                    "password"
                  ],
                  "revocation_endpoint_auth_methods_supported": [
                    "client_secret_post"
                  ],
                  "introspection_endpoint_auth_methods_supported": [
                    "client_secret_post"
                  ],
                  "code_challenge_methods_supported": [
                    "S256"
                  ]
                }
                """
                .replace("{origin}", openIdUrl)
        );
    }
}