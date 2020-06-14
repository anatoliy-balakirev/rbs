package com.rbs.service;

import com.rbs.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtProperties properties;
    private final Clock clock;

    public UUID extractClientId(final String token) {
        final var claims = extractClaims(token);
        return UUID.fromString(claims.getSubject());
    }

    public String generateToken(final UUID clientId) {
        final long now = clock.millis();
        return Jwts.builder()
                .setClaims(new HashMap<>())
                .setSubject(clientId.toString())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + (properties.getValiditySeconds() * 1000)))
                .signWith(SignatureAlgorithm.HS512, properties.getSecret()).compact();
    }

    private Claims extractClaims(final String token) {
        return Jwts.parser()
                .setClock(() -> new Date(clock.millis()))
                .setSigningKey(properties.getSecret())
                .parseClaimsJws(token)
                .getBody();
    }
}
