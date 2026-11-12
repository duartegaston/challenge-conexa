package com.conexa.challenge.config;

import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

/**
 * {@link RestClient} hacia SWAPI. Parte del builder de Spring Boot (conserva métricas y observabilidad)
 * y aplica los timeouts de {@link SwapiProperties}.
 */
@Configuration
public class RestClientConfig {

    @Bean
    RestClient swapiRestClient(RestClient.Builder builder, SwapiProperties properties) {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.defaults()
                .withConnectTimeout(properties.connectTimeout())
                .withReadTimeout(properties.readTimeout());

        return builder.baseUrl(StringUtils.trimTrailingCharacter(properties.baseUrl(), '/'))
                .requestFactory(ClientHttpRequestFactoryBuilder.detect().build(settings))
                .build();
    }
}
