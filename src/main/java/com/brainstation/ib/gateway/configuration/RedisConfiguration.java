package com.brainstation.ib.gateway.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SslOptions;
import io.lettuce.core.protocol.ProtocolVersion;
import lombok.RequiredArgsConstructor;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.util.StringUtils;

import java.io.IOException;

@EnableRedisRepositories
@Configuration
public class RedisConfiguration {

    @Value("${redis.host}")
    private String host;

    @Value("${redis.port}")
    private Integer port;

    @Value("${redis.password}")
    private String password;

    @Value("${redis.pool.max.connection}")
    private Integer maxConnection;

    @Value("${redis.pool.max.idle.connection}")
    private Integer maxIdleConnection;

    @Value("${redis.pool.min.idle.connection}")
    private Integer minIdleConnection;

    @Value("${redis.database.index}")
    private int databaseIndex;

    @Value("${redis.ssl.enabled}")
    private boolean sslEnabled;

    @Value("${redis.ssl.ca}")
    private String caCert;

    private final ResourceLoader resourceLoader;

	public RedisConfiguration(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@Bean
    public GenericObjectPoolConfig<Void> genericObjectPoolConfig() {
        final GenericObjectPoolConfig<Void> genericObjectPoolConfig = new GenericObjectPoolConfig<>();
        genericObjectPoolConfig.setMaxTotal(maxConnection);
        genericObjectPoolConfig.setMaxIdle(maxIdleConnection);
        genericObjectPoolConfig.setMinIdle(minIdleConnection);
        return genericObjectPoolConfig;
    }

    @Bean
    public RedisConnectionFactory getConnectionFactory(GenericObjectPoolConfig<Void> genericObjectPoolConfig) throws IOException {
        final RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(host);
        redisStandaloneConfiguration.setPort(port);
        if (StringUtils.hasLength(password)) {
            redisStandaloneConfiguration.setPassword(password);
        }
        redisStandaloneConfiguration.setDatabase(databaseIndex);

        if (sslEnabled) {
            SslOptions sslOptions = SslOptions.builder()
                    .trustManager(resourceLoader.getResource("file:" + caCert)
                            .getFile())
                    .build();

            ClientOptions clientOptions = ClientOptions
                    .builder()
                    .sslOptions(sslOptions)
                    .protocolVersion(ProtocolVersion.RESP3)
                    .build();
            final LettuceClientConfiguration lettuceClientConfiguration = LettucePoolingClientConfiguration.builder()
                    .poolConfig(
                            genericObjectPoolConfig)
                    .clientOptions(
                            clientOptions)
                    .useSsl()
                    .disablePeerVerification()
                    .build();
            return new LettuceConnectionFactory(redisStandaloneConfiguration, lettuceClientConfiguration);
        } else {
            final LettuceClientConfiguration lettuceClientConfiguration = LettucePoolingClientConfiguration.builder().poolConfig(genericObjectPoolConfig).build();
            return new LettuceConnectionFactory(redisStandaloneConfiguration, lettuceClientConfiguration);
        }
    }

    @Primary
    @Bean(name = "redisTemplate")
    public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        final RedisTemplate<?, ?> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        return template;
    }
}
