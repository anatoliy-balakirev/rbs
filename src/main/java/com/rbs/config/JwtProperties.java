package com.rbs.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(value = "jwt")
public class JwtProperties {

    private long validitySeconds;

    private String secret;
}
