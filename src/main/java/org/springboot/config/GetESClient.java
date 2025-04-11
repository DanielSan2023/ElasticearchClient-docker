package org.springboot.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;


@Component
@Profile("prod")
public class GetESClient {
    private final HttpClientConfigImpl httpClientConfigImpl;

    public GetESClient(HttpClientConfigImpl httpClientConfigImpl) {
        this.httpClientConfigImpl = httpClientConfigImpl;
    }

    @Bean(name = "elasticsearchClient")
    @Primary
    public ElasticsearchClient getElasticsearchClient() {
        RestClientBuilder builder = RestClient.builder(new HttpHost("localhost", 9200, "https"));

        builder.setHttpClientConfigCallback(httpClientConfigImpl);

        RestClient restClient = builder.build();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        JacksonJsonpMapper jsonpMapper = new JacksonJsonpMapper(objectMapper);
        ElasticsearchTransport transport = new RestClientTransport(restClient, jsonpMapper);

        return new ElasticsearchClient(transport);
    }
}
