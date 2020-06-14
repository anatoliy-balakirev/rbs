package com.rbs.service;

import com.rbs.config.JwtProperties;
import com.rbs.config.RbsConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.UUID;

/**
 * Helper class to generate valid JWT token for manual testing.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JwtService.class, RbsConfiguration.class},
        initializers = ConfigFileApplicationContextInitializer.class)
@EnableConfigurationProperties(JwtProperties.class)
class JwtGeneratorHelper {
    @Autowired
    private JwtService jwtService;

    @Test
    void generateToken() {
        final var token = jwtService.generateToken(UUID.fromString("f14e9d07-a7f8-42bc-87e6-be8d1ffde7d1"));
        System.out.println("Generated token is: " + token);
    }
}