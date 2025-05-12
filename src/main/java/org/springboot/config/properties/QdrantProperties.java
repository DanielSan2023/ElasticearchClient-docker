package org.springboot.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "spring.ai.vectorstore.qdrant")
public class QdrantProperties {

    private String host;
    private int port;
    private String collectionName;

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }
}
