package com.brainstation.ib.gateway.configuration;

import com.brainstation.ib.gateway.util.JacksonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClientsProperties;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;

@Configuration
public class WebConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return JacksonUtil.objectMapper();
    }

    @Bean
    @ConditionalOnMissingBean
    public LoadBalancerClientFactory loadBalancerClientFactory(LoadBalancerClientsProperties properties) {
        return new LoadBalancerClientFactory(properties) {
            @Override
            public GenericApplicationContext createContext(String name) {
                ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
                GenericApplicationContext context = super.createContext(name);
                Thread.currentThread().setContextClassLoader(originalClassLoader);
                return context;
            }
        };
    }
}
