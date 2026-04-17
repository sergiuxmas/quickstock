package com.quickstock.core.controller;

import com.quickstock.core.security.DatabaseUserDetailsService;
import com.quickstock.core.security.TokenService;
import com.quickstock.core.service.ProductService;
import com.quickstock.core.testconfig.MockMvcITConfig;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration"
})
@Import(MockMvcITConfig.class)
public class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private DatabaseUserDetailsService databaseUserDetailsService;

    @Test
    @DisplayName("POST /auth/login returns 200 and token when credentials are valid")
    void postLogin_returns200AndToken_whenCredentialsAreValid() throws Exception {
        when(tokenService.generate(any())).thenReturn("mocked-token");
        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken("email@email.com", "password"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "email@email.com",
                                  "password": "password"
                                }
                                """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mocked-token"));
    }

    @Test
    @DisplayName("POST /auth/login reaches authentication logic without an Authorization header")
    void postLogin_withoutAuthorizationHeader_reachesAuthenticationManager() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("invalid credentials"));

        ServletException exception = Assertions.assertThrows(ServletException.class,
                () -> mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "email@email.com",
                                  "password": "password"
                                }
                                """)));

        Assertions.assertInstanceOf(BadCredentialsException.class, exception.getCause());
        verify(authenticationManager).authenticate(any());
    }

    @Test
    @DisplayName("POST /auth/login returns 401 when credentials are invalid")
    void postLogin_returns401_whenCredentialsAreInvalid() {
        // Hint:
        // 1) Mock AuthenticationManager.authenticate(...) to throw BadCredentialsException.
        // 2) Perform POST /auth/login with invalid credentials.
        // 3) Assert the HTTP status produced by your exception handling/security setup (typically 401).
    }

    @Test
    @DisplayName("POST /auth/login returns token payload in expected JSON shape")
    void postLogin_returnsTokenPayload_withExpectedJsonShape() {
        // Hint:
        // 1) Stub successful authentication and token generation.
        // 2) Perform POST /auth/login.
        // 3) Assert response content type is JSON.
        // 4) Assert the payload contains only/at least the "token" field, matching LoginResponse.
    }

    @Test
    @DisplayName("GET /auth/login returns 405 because only POST is supported")
    void getLogin_returns405_whenMethodNotSupported() {
        // Hint:
        // 1) Perform GET /auth/login.
        // 2) Assert status is 405 Method Not Allowed.
        // 3) This protects the @PostMapping("/login") contract.
    }

    @Test
    @DisplayName("POST /auth/login returns 400 when request body is malformed JSON")
    void postLogin_returns400_whenRequestBodyIsMalformed() {
        // Hint:
        // 1) Send invalid JSON in the request body.
        // 2) Assert deserialization fails before controller logic and returns 400.
    }

    @Test
    @DisplayName("POST /auth/login documents current behavior for blank credentials")
    void postLogin_documentsCurrentBehavior_whenCredentialsAreBlank() {
        // Hint:
        // 1) LoginRequest fields have @NotBlank, but AuthController.login(...) does not use @Valid.
        // 2) Send JSON with blank username/password.
        // 3) Assert the actual current HTTP outcome in your application (likely authentication failure, not validation failure).
        // 4) Use this test to document current behavior or guide a later @Valid change.
    }
}
