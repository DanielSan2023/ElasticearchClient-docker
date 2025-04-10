package org.springboot.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import static org.springboot.service.ProductServiceImplTest.elasticsearchContainer;

@TestConfiguration
@Profile("test")
public class ElasticsearchTestConfig {

    @Bean(name = "elasticsearchClientForTest")
    @Qualifier("elasticsearchClientForTest")
    public ElasticsearchClient getElasticsearchClientForTest() {
        String containerHost = elasticsearchContainer.getHost();
        Integer containerPort = elasticsearchContainer.getMappedPort(9200);
        String containerUris = "http";

        RestClientBuilder builder = RestClient.builder(new HttpHost(containerHost, containerPort, containerUris));
        RestClient restClient = builder.build();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        JacksonJsonpMapper jsonpMapper = new JacksonJsonpMapper(objectMapper);
        ElasticsearchTransport transport = new RestClientTransport(restClient, jsonpMapper);

        return new ElasticsearchClient(transport);
    }
}