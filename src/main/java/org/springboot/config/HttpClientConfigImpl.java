package org.springboot.config;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClientBuilder;
import org.springboot.config.properties.ElasticsearchProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.net.ssl.SSLContext;
import java.io.File;

@Configuration
@Profile("prod")
public class HttpClientConfigImpl implements RestClientBuilder.HttpClientConfigCallback {

    private final ElasticsearchProperties properties;

    public HttpClientConfigImpl(ElasticsearchProperties properties) {
        this.properties = properties;
    }

    @Override
    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpAsyncClientBuilder) {
        try {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            UsernamePasswordCredentials usernamePasswordCredentials = new UsernamePasswordCredentials(properties.getUsername(), properties.getPassword());

            credentialsProvider.setCredentials(AuthScope.ANY, usernamePasswordCredentials);
            httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);

            File trustStoreFile = new File(properties.getTruststorePath());

            SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(trustStoreFile, properties.getTruststorePassword().toCharArray())
                    .build();

            httpAsyncClientBuilder.setSSLContext(sslContext);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return httpAsyncClientBuilder;
    }
}