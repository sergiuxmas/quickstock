package com.quickstock.core.controller;

import com.quickstock.core.dto.auth.LoginRequest;
import com.quickstock.core.dto.auth.LoginResponse;
import com.quickstock.core.security.TokenService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import java.util.Objects;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private TokenService tokenService;

    @Test
    @DisplayName("returns token response when authentication succeeds")
    void login_returnsTokenResponse_whenAuthenticationSucceeds() {
        Mockito.when(authenticationManager.authenticate(Mockito.any())).thenReturn(Mockito.mock(Authentication.class));
        Mockito.when(tokenService.generate(Mockito.any())).thenReturn("mock-token");
        AuthController controller = new AuthController(authenticationManager, tokenService);
        LoginRequest request = new LoginRequest("user", "pass");
        LoginResponse response = controller.login(request);

        Assertions.assertEquals("mock-token", response.token());
    }

    @Test
    @DisplayName("builds UsernamePasswordAuthenticationToken from request credentials")
    void login_buildsUsernamePasswordAuthenticationToken_fromRequestCredentials() {
        LoginRequest request = new LoginRequest("user", "pass");
        ArgumentCaptor<Authentication> captor = ArgumentCaptor.forClass(Authentication.class);
        AuthController controller = new AuthController(authenticationManager, tokenService);
        controller.login(request);
        Mockito.verify(authenticationManager).authenticate(captor.capture());
        Authentication auth = captor.getValue();

        Assertions.assertEquals("user", auth.getName());
        Assertions.assertEquals("pass", Objects.requireNonNull(auth.getCredentials()).toString());
    }

    @Test
    @DisplayName("calls token service with authenticated result from authentication manager")
    void login_callsTokenServiceWithAuthenticatedResult() {
        Authentication authenticated = Mockito.mock(Authentication.class);
        Mockito.when(authenticationManager.authenticate(Mockito.any())).thenReturn(authenticated);
        AuthController controller = new AuthController(authenticationManager, tokenService);
        controller.login(new LoginRequest("user", "pass"));

        Mockito.verify(tokenService, Mockito.times(1)).generate(Mockito.same(authenticated));
    }

    @Test
    @DisplayName("propagates authentication exception when credentials are invalid")
    void login_propagatesAuthenticationException_whenCredentialsAreInvalid() {
        Mockito.when(authenticationManager.authenticate(Mockito.any()))
                .thenThrow(new BadCredentialsException("Authentication failed"));
        AuthController controller = new AuthController(authenticationManager, tokenService);

        Assertions.assertThrows(BadCredentialsException.class,
                () -> controller.login(new LoginRequest("user", "wrongpass")));
        // Verify tokenService.generate() is never called
        Mockito.verify(tokenService, Mockito.never()).generate(Mockito.any());
    }

    @Test
    @DisplayName("forwards blank credentials to authentication manager under current controller contract")
    void login_forwardsBlankCredentials_withoutControllerValidation() {
        LoginRequest request = new LoginRequest("", "");
        Authentication authenticated = Mockito.mock(Authentication.class);
        Mockito.when(authenticationManager.authenticate(Mockito.any())).thenReturn(authenticated);
        Mockito.when(tokenService.generate(Mockito.same(authenticated))).thenReturn("mock-token");

        AuthController controller = new AuthController(authenticationManager, tokenService);
        LoginResponse response = controller.login(request);

        ArgumentCaptor<Authentication> captor = ArgumentCaptor.forClass(Authentication.class);
        Mockito.verify(authenticationManager).authenticate(captor.capture());
        Authentication auth = captor.getValue();

        Assertions.assertEquals("", auth.getName());
        Assertions.assertEquals("", Objects.requireNonNull(auth.getCredentials()).toString());
        Assertions.assertEquals("mock-token", response.token());
        Mockito.verify(tokenService, Mockito.times(1)).generate(Mockito.same(authenticated));
    }
}
