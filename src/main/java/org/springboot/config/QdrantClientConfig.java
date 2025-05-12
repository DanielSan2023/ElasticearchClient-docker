package org.springboot.config;

import org.springboot.config.properties.QdrantProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class QdrantClientConfig {

    @Bean
    public WebClient qdrantWebClient(QdrantProperties properties) {
        String baseUrl = String.format("http://%s:%d", properties.getHost(), properties.getPort());
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
