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
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    @DisplayName("POST /auth/login propagates bad credentials failure when credentials are invalid")
    void postLogin_propagatesBadCredentialsException_whenCredentialsAreInvalid() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("invalid credentials"));

        ServletException exception = Assertions.assertThrows(ServletException.class,
                () -> mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "email@email.com",
                                  "password": "wrong"
                                }
                                """)));

        Assertions.assertInstanceOf(BadCredentialsException.class, exception.getCause());
        verify(authenticationManager).authenticate(any());
    }

    @Test
    @DisplayName("POST /auth/login returns token payload in expected JSON shape")
    void postLogin_returnsTokenPayload_withExpectedJsonShape() throws Exception {
        when(tokenService.generate(any())).thenReturn("mocked-token");
        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken("email@email.com", "password"));

        ResultActions result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "email": "email@email.com",
                          "password": "password"
                        }
                        """)
        );

        Assertions.assertDoesNotThrow(() -> result.andReturn().getResponse().getContentAsString());
        result.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "accessToken": "mocked-token"
                        }
                        """));
    }

    @Test
    @DisplayName("GET /auth/login returns 405 because only POST is supported")
    void getLogin_returns405_whenMethodNotSupported() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(status().is(405));
    }

    @Test
    @DisplayName("POST /auth/login returns 400 when request body is malformed JSON")
    void postLogin_returns400_whenRequestBodyIsMalformed() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "name@email.com"
                                  "password": "password"
                                }
                                """))
                .andExpect(status().is(400));
    }

    @Test
    @DisplayName("POST /auth/login returns 400 when credentials are blank")
    void postLogin_returns400_whenCredentialsAreBlank() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "",
                                  "password": "password"
                                }
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "name@email.com",
                                  "password": ""
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(authenticationManager, org.mockito.Mockito.never()).authenticate(any());
    }

    @Test
    @DisplayName("POST /auth/login returns 400 when credentials are blank")
    void postLogin_returns400_whenEmailIsWrong() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "text",
                                  "password": "password"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(authenticationManager, org.mockito.Mockito.never()).authenticate(any());
    }
}
