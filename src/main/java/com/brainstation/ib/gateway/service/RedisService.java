package com.brainstation.ib.gateway.service;

import com.brainstation.ib.gateway.domain.enums.UserStatus;
import com.brainstation.ib.gateway.domain.enums.UserType;
import com.brainstation.ib.gateway.model.RedisAccessToken;
import com.brainstation.ib.gateway.util.JacksonUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {
    private static final String TOKEN_FOLDER = "token:user-";
    private static final String SESSION_FOLDER = "session:jwt-";

    public static final String REDIS_API_ROUTE_KEY = "urls-api-access-route";
    public static final String REDIS_API_ACCESS_PUBLIC_KEY = "urls-api-access-public";
    public static final String REDIS_API_ACCESS_USER_KEY = "urls-api-access-user:%s-%s";
    public static final String REDIS_API_ACCESS_USER_NOT_KEY = "urls-api-access-user-not";
    public static final String REDIS_API_ACCESS_DECRYPT_KEY = "urls-api-access-decrypt";
    public static final String REDIS_API_ACCESS_ADMIN_KEY = "urls-api-access-admin:%s";
    public static final String REDIS_API_ACCESS_EXTERNAL_KEY = "urls-api-access-external";

    private final RedisTemplate<String, String> redisTemplate;

    public void cleanCaches() {
        redisTemplate.delete(REDIS_API_ACCESS_PUBLIC_KEY);
        redisTemplate.delete(REDIS_API_ACCESS_EXTERNAL_KEY);
        redisTemplate.delete(REDIS_API_ACCESS_USER_NOT_KEY);
        redisTemplate.delete(REDIS_API_ACCESS_DECRYPT_KEY);
        for (UserType userType : UserType.values()) {
            for (UserStatus userStatus : UserStatus.values()) {
                redisTemplate.delete(String.format(REDIS_API_ACCESS_USER_KEY, userType.name(), userStatus.name()));
                redisTemplate.delete(String.format(REDIS_API_ACCESS_ADMIN_KEY, userStatus.name()));
            }
        }
    }

    public String get(String key) {
        var data = redisTemplate.opsForValue().get(key);
        if (StringUtils.isEmpty(data)) {
            return null;
        }
        return data;
    }

    public <T> T get(String key, Class<T> clazz) {
        var data = redisTemplate.opsForValue().get(key);
        if (StringUtils.isEmpty(data)) {
            return null;
        }
        return JacksonUtil.jsonToInstance(data, clazz);
    }

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, Objects.requireNonNull(JacksonUtil.objectToJson(value)));
    }

    public RedisAccessToken accessToken(String userIdentity) {
        return JacksonUtil.jsonToInstance(redisTemplate.opsForValue().get(TOKEN_FOLDER.concat(userIdentity)), RedisAccessToken.class);
    }

    public String session(String accessToken) {
        return redisTemplate.opsForValue().get(SESSION_FOLDER.concat(accessToken));
    }

    public void sessionTTlUpdate(String accessToken, String value) {
        try {
            redisTemplate.opsForValue().set(SESSION_FOLDER.concat(accessToken), value, 5L, TimeUnit.MINUTES);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}
