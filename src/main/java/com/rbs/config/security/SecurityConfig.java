package com.rbs.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbs.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * Security-related configuration.
 * <p>
 * Steps to authenticate client are:
 * 1. AuthFilter is executed
 * 2. AuthFilter is calling auth manager, which is calling our AuthProvider
 * 3. AuthProvider is resolving client id based on the provided token
 * 4. AuthFilter is setting resolved client id to the security context, so that it is available afterwards
 */
@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final ObjectMapper objectMapper;
    private final JwtService jwtService;

    @Override
    public void configure(final WebSecurity web) {
        // Will use our custom filter, so asking default spring security to ignore everything
        web.ignoring().requestMatchers(EndpointRequest.toAnyEndpoint());
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http
                // Cross-Site Request Forgery is irrelevant here
                .csrf().disable()
                // Our service is stateless
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .anonymous().disable()
                .exceptionHandling().authenticationEntryPoint(unauthorizedEntryPoint());

        // Our filter to actually perfomr auth:
        http.addFilterBefore(new AuthFilter(authenticationManager(), unauthorizedEntryPoint()),
                BasicAuthenticationFilter.class);
    }

    @Override
    protected void configure(final AuthenticationManagerBuilder builder) {
        builder.authenticationProvider(authProvider());
    }

    /**
     * 'entry point' for all unauthorised requests. Just returns error payload to the client.
     */
    @Bean
    public AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) -> {
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            final var problem = Problem.builder()
                    .withTitle(Status.UNAUTHORIZED.getReasonPhrase())
                    .withStatus(Status.UNAUTHORIZED)
                    .withDetail("You are not authorized to access this endpoint")
                    .build();
            objectMapper.writeValue(response.getOutputStream(), problem);
        };
    }

    private AuthenticationProvider authProvider() {
        return new PreAuthenticatedAuthenticationProvider() {
            @Override
            public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
                // We are getting here from our filter, so principal here is our JWT token.
                final var token = (String) authentication.getPrincipal();
                if (StringUtils.isBlank(token)) {
                    final String message = "Token wasn't provided";
                    LOGGER.warn(message);
                    throw new BadCredentialsException(message);
                }
                try {
                    final UUID clientId = jwtService.extractClientId(token);
                    final var result = new PreAuthenticatedAuthenticationToken(clientId, null);
                    result.setAuthenticated(true);
                    return result;
                } catch (final Exception ex) {
                    throw new AuthenticationServiceException("Unable to authenticate user.", ex);
                }
            }
        };
    }
}
