package com.quickstock.core.security;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;

public class JwtAuthConverterTest {

    @Test
    @DisplayName("creates converter bean instance")
    void jwtAuthenticationConverter_createsBean() {
        JwtAuthConverter jwtAuthConverter = new JwtAuthConverter();
        Assertions.assertNotNull(jwtAuthConverter.jwtAuthenticationConverter());
    }

    @Test
    @DisplayName("maps role claim to a single authority")
    void convert_mapsRoleClaimToSingleAuthority() {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .claim("role", "ROLE_ADMIN")
                .build();
        Jwt jwt = new Jwt("token-value", null, null, Map.of("key", "value"), claims.getClaims());
        AbstractAuthenticationToken authToken = new JwtAuthConverter().jwtAuthenticationConverter().convert(jwt);

        Assertions.assertTrue(authToken instanceof JwtAuthenticationToken);
        Collection<GrantedAuthority> authorities = authToken.getAuthorities();
        Assertions.assertEquals(1, authorities.size());
        Assertions.assertEquals("ROLE_ADMIN", authorities.iterator().next().getAuthority());
    }

    @Test
    @DisplayName("returns empty authorities when role claim is missing")
    void convert_returnsEmptyAuthoritiesWhenRoleClaimMissing() {
        JwtClaimsSet claims = JwtClaimsSet.builder().subject("test").build();
        Jwt jwt = new Jwt("token-value", null, null, Map.of("key", "value"), claims.getClaims());
        AbstractAuthenticationToken authToken = new JwtAuthConverter().jwtAuthenticationConverter().convert(jwt);

        Assertions.assertTrue(authToken instanceof JwtAuthenticationToken);
        Collection<GrantedAuthority> authorities = authToken.getAuthorities();
        Assertions.assertTrue(authorities.isEmpty());
    }

    @Test
    @DisplayName("preserves original Jwt instance in authentication token")
    void convert_preservesOriginalJwtReference() {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .claim("role", "ROLE_ADMIN")
                .build();
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(3600);
        Jwt jwt = new Jwt("token-value", now, expiration, Map.of("Accept", "application/json"), claims.getClaims());
        JwtAuthenticationToken authToken = (JwtAuthenticationToken) new JwtAuthConverter().jwtAuthenticationConverter().convert(jwt);
        Jwt token = authToken.getToken();

        Assertions.assertNotNull(token);
        Assertions.assertEquals(jwt.getTokenValue(), token.getTokenValue());
        Assertions.assertEquals(jwt.getClaims(), token.getClaims());
        Assertions.assertSame(jwt, token, "Converter should keep the same Jwt instance");
        Assertions.assertEquals(jwt.getHeaders(), token.getHeaders(), "JWT headers should match");
        Assertions.assertEquals(jwt.getClaims(), token.getClaims(), "JWT claims should match");
    }

    @Test
    @DisplayName("uses role claim value as-is when creating authority")
    void convert_usesRoleClaimAsIs() {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .claim("role", "ADMIN")
                .build();
        Jwt jwt = new Jwt("token-value", null, null, Map.of("key", "value"), claims.getClaims());
        AbstractAuthenticationToken authToken = new JwtAuthConverter().jwtAuthenticationConverter().convert(jwt);

        Collection<GrantedAuthority> authorities = authToken.getAuthorities();
        Assertions.assertEquals("ADMIN", authorities.iterator().next().getAuthority());
    }

    @Test
    @DisplayName("throws when role claim is blank and authority cannot be created")
    void convert_throwsForBlankRoleClaim() {
        JwtClaimsSet claims = JwtClaimsSet.builder().claim("role", "").build();
        Jwt jwt = new Jwt("token-value", null, null, Map.of("key", "value"), claims.getClaims());
        Assertions.assertThrows(IllegalArgumentException.class, () -> new JwtAuthConverter().jwtAuthenticationConverter().convert(jwt));

    }

    @Test
    @DisplayName("converts non-string role claim value into a string authority")
    void convert_convertsNonStringRoleClaimToStringAuthority() {
        JwtClaimsSet claims = JwtClaimsSet.builder().claim("role", 1).build();
        Jwt jwt = new Jwt("token-value", null, null, Map.of("key", "value"), claims.getClaims());
        AbstractAuthenticationToken authToken = new JwtAuthConverter().jwtAuthenticationConverter().convert(jwt);

        Assertions.assertTrue(authToken instanceof JwtAuthenticationToken);
        Collection<GrantedAuthority> authorities = authToken.getAuthorities();
        Assertions.assertEquals(1, authorities.size());
        Assertions.assertEquals("1", authorities.iterator().next().getAuthority());
    }
}
