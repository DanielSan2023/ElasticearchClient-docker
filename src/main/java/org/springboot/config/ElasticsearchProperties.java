package org.springboot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "elasticsearch")
public class ElasticsearchProperties {
    private String username;
    private String password;
    private String truststorePath;
    private String truststorePassword;
}