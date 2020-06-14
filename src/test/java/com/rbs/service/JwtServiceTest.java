package com.rbs.service;

import com.rbs.config.JwtProperties;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Clock;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JwtService.class, JwtServiceTest.TestConfig.class},
        initializers = ConfigFileApplicationContextInitializer.class)
@EnableConfigurationProperties(JwtProperties.class)
class JwtServiceTest {
    private static final UUID CLIENT_ID = UUID.fromString("f14e9d07-a7f8-42bc-87e6-be8d1ffde7d1");
    private static final ZonedDateTime NOW =
            ZonedDateTime.from(ISO_OFFSET_DATE_TIME.parse("2020-06-14T10:54:27.6374357+02:00"));
    private static final String TOKEN = "eyJhbGciOiJIUzUxMiJ9" +
            ".eyJzdWIiOiJmMTRlOWQwNy1hN2Y4LTQyYmMtODdlNi1iZThkMWZmZGU3ZDEiLCJleHAiOjE1OTIxNDI4NjcsImlhdCI6MTU5MjEyNDg2N30" +
            ".hSiwGoPspbqdHDif7tIIh64q2z2robDdsO4v-JgT57aU22qEmEUCvFrB1ZnLqaNlGpvpYjfoDIn5irPWqN7FrA";
    // Expiration time for this token is: NOW - (5 minutes + 1 second). And because validity is defined as 5 hours
    // in our config - this token is considered expired when time is equal to NOW (which we use in this test):
    private static final String EXPIRED_TOKEN = "eyJhbGciOiJIUzUxMiJ9" +
            ".eyJzdWIiOiJmMTRlOWQwNy1hN2Y4LTQyYmMtODdlNi1iZThkMWZmZGU3ZDEiLCJleHAiOjE1OTIxMjQ4NjYsImlhdCI6MTU5MjEwNjg2Nn0" +
            ".y1VLjB4rUigLOZ0yWJnA3QRM7rnjzeN6XpVqxwRyVC8ZgSxPH0o8g7wNzfU_p-5LSOaFv4dKG55Ru8hWeFoerg";
    @Autowired
    private JwtService jwtService;
    @Autowired
    private JwtProperties properties;

    @Test
    void extractClientId() {
        final var clientId = jwtService.extractClientId(TOKEN);
        assertEquals(CLIENT_ID, clientId);
    }

    @Test
    void extractClientIdWithExpiredToken() {
        final var exception = assertThrows(ExpiredJwtException.class, () -> jwtService.extractClientId(EXPIRED_TOKEN));
        assertEquals("JWT expired at 2020-06-14T10:54:26Z. Current time: 2020-06-14T10:54:27Z, a difference " +
                "of 1637 milliseconds.  Allowed clock skew: 0 milliseconds.", exception.getMessage());
    }

    @Test
    @DirtiesContext
    void extractClientIdWithInvalidSecret() {
        properties.setSecret("some other secret");

        final var exception = assertThrows(SignatureException.class, () -> jwtService.extractClientId(TOKEN));
        assertEquals("JWT signature does not match locally computed signature. JWT validity cannot be " +
                "asserted and should not be trusted.", exception.getMessage());
    }

    @Test
    void generateToken() {
        final var token = jwtService.generateToken(CLIENT_ID);
        assertEquals(TOKEN, token);
    }

    @TestConfiguration
    public static class TestConfig {

        @Bean
        public Clock clock() {
            return Clock.fixed(NOW.toInstant(), ZoneOffset.UTC);
        }
    }
}