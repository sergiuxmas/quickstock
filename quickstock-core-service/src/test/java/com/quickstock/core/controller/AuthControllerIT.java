package com.quickstock.core.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AuthControllerIT {

	@Test
	@DisplayName("POST /auth/login returns 200 and token when credentials are valid")
	void postLogin_returns200AndToken_whenCredentialsAreValid() {
		// Hint:
		// 1) Use a Spring Boot integration style with MockMvc.
		// 2) Mock AuthenticationManager to return an authenticated Authentication.
		// 3) Mock TokenService.generate(...) to return a fixed token.
		// 4) Perform POST /auth/login with JSON body containing username and password.
		// 5) Assert HTTP 200 and jsonPath("$.token") equals the mocked token.
	}

	@Test
	@DisplayName("POST /auth/login is accessible without bearer token")
	void postLogin_allowsAnonymousAccess_withoutBearerToken() {
		// Hint:
		// 1) SecurityConfig permits /auth/**.
		// 2) Perform POST /auth/login without Authorization header.
		// 3) Assert request is not rejected with 401/403 by the security filter chain.
		// 4) Depending on your mocked collaborators, expect 200 or downstream auth failure instead.
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
