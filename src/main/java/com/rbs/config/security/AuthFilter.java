package com.rbs.config.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * Filter, initiating JWT authentication process.
 */
@Slf4j
@RequiredArgsConstructor
public class AuthFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthenticationManager authenticationManager;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {
        try {
            final var token = extractJwtToken(request);
            // Assuming debug is disabled by default. Otherwise remove this logging statement to not leak tokens:
            LOGGER.debug("Trying to authenticate user with token: '{}'", token);
            final var authentication = authenticate(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            LOGGER.debug("Auth is done. Passing request further");
            filterChain.doFilter(request, response);
        } catch (final AuthenticationException ex) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response, ex);
        }
    }

    private Authentication authenticate(final String token) {
        final var authRequest = new PreAuthenticatedAuthenticationToken(token, null);
        final var authResult = authenticationManager.authenticate(authRequest);
        if (authResult == null || !authResult.isAuthenticated()) {
            throw new AuthenticationServiceException("Unable to authenticate client with provided token");
        }
        LOGGER.info("Client was successfully authenticated. Client id: '{}' ", authResult.getPrincipal().toString());
        return authResult;
    }

    private static String extractJwtToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(AUTHORIZATION_HEADER))
                .filter(header -> header.startsWith(BEARER_PREFIX))
                .map(header -> header.replaceFirst(BEARER_PREFIX, StringUtils.EMPTY))
                .orElseThrow(() -> new BadCredentialsException("No '" + AUTHORIZATION_HEADER + "' header provided"));
    }

}
