package com.quickstock.core.security;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TokenServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private Jwt mockJwt;

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService(jwtEncoder, "test-issuer", 60);
    }

    @Test
    void generate_returnsTokenAndWritesClaimsForAuthenticatedUser() {
        Authentication auth = new UsernamePasswordAuthenticationToken("admin@mail.com", "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(jwtEncoder.encode(any())).thenReturn(mockJwt);
        when(mockJwt.getTokenValue()).thenReturn("mock-token");
        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);

        String currentToken = tokenService.generate(auth);

        verify(jwtEncoder).encode(captor.capture());
        JwtEncoderParameters jwtEncoderParameters = captor.getValue();
        JwtClaimsSet claims = jwtEncoderParameters.getClaims();

        Assertions.assertEquals("mock-token", currentToken);
        Assertions.assertEquals("ROLE_ADMIN", claims.getClaim("role"));
        Assertions.assertEquals("admin@mail.com", claims.getSubject());
        Assertions.assertEquals("test-issuer", claims.getClaimAsString("iss"));
    }

    @Test
    @DisplayName("defaults role claim to ROLE_CUSTOMER when authentication has no authorities")
    void generate_defaultsRoleToCustomerWhenNoAuthorities() {
        Authentication auth = new UsernamePasswordAuthenticationToken("user@mail.com", "password");
        when(jwtEncoder.encode(any())).thenReturn(mockJwt);
        when(mockJwt.getTokenValue()).thenReturn("mock-token");
        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);

        tokenService.generate(auth);

        verify(jwtEncoder).encode(captor.capture());
        JwtEncoderParameters jwtEncoderParameters = captor.getValue();
        JwtClaimsSet claims = jwtEncoderParameters.getClaims();

        Assertions.assertEquals("ROLE_CUSTOMER", claims.getClaim("role"));
        Assertions.assertEquals("user@mail.com", claims.getSubject());
        Assertions.assertEquals("test-issuer", claims.getClaimAsString("iss"));
    }

    @Test
    @DisplayName("uses the first authority as the role claim when multiple authorities are present")
    void generate_usesFirstAuthorityWhenMultipleAuthoritiesExist() {
        Authentication auth = new UsernamePasswordAuthenticationToken("admin@mail.com", "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        when(jwtEncoder.encode(any())).thenReturn(mockJwt);
        when(mockJwt.getTokenValue()).thenReturn("mock-token");
        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);

        tokenService.generate(auth);

        verify(jwtEncoder).encode(captor.capture());
        JwtEncoderParameters jwtEncoderParameters = captor.getValue();
        JwtClaimsSet claims = jwtEncoderParameters.getClaims();

        Assertions.assertEquals("ROLE_ADMIN", claims.getClaim("role"));
    }

    @Test
    @DisplayName("sets expiration after issuedAt using configured TTL minutes")
    void generate_setsExpirationBasedOnConfiguredTtl() {
        Authentication auth = new UsernamePasswordAuthenticationToken("admin@mail.com", "password");
        when(jwtEncoder.encode(any())).thenReturn(mockJwt);
        when(mockJwt.getTokenValue()).thenReturn("mock-token");
        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);

        tokenService.generate(auth);

        verify(jwtEncoder).encode(captor.capture());
        JwtEncoderParameters jwtEncoderParameters = captor.getValue();
        JwtClaimsSet claims = jwtEncoderParameters.getClaims();

        Assertions.assertTrue(claims.getExpiresAt().isAfter(claims.getIssuedAt()));
        long minutesBetween = (claims.getExpiresAt().toEpochMilli() - claims.getIssuedAt().toEpochMilli()) / (60 * 1000);
        Assertions.assertTrue(minutesBetween >= 59 && minutesBetween <= 61,
                "Expiration should be approximately 60 minutes after issuedAt");
    }
}
