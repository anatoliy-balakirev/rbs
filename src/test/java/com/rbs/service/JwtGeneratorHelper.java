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
        final var token = jwtService.generateToken(UUID.fromString("e4473f24-55e6-4e7b-b11a-8211744fbdfa"));
        System.out.println("Generated token is: " + token);
    }
}