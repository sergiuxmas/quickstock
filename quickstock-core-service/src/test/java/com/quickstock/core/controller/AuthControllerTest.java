package com.quickstock.core.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AuthControllerTest {

	@Test
	@DisplayName("returns token response when authentication succeeds")
	void login_returnsTokenResponse_whenAuthenticationSucceeds() {
		// Hint:
		// 1) Mock AuthenticationManager and TokenService.
		// 2) Stub authenticate(...) to return an authenticated Authentication.
		// 3) Stub tokenService.generate(auth) to return a token string.
		// 4) Call controller.login(request).
		// 5) Assert LoginResponse.token() matches the generated token.
	}

	@Test
	@DisplayName("builds UsernamePasswordAuthenticationToken from request credentials")
	void login_buildsUsernamePasswordAuthenticationToken_fromRequestCredentials() {
		// Hint:
		// 1) Create LoginRequest with known username and password.
		// 2) Capture the Authentication passed to authenticationManager.authenticate(...).
		// 3) Assert it is UsernamePasswordAuthenticationToken.
		// 4) Assert principal equals request.username() and credentials equals request.password().
	}

	@Test
	@DisplayName("calls token service with authenticated result from authentication manager")
	void login_callsTokenServiceWithAuthenticatedResult() {
		// Hint:
		// 1) Mock an Authentication returned by authenticationManager.authenticate(...).
		// 2) Invoke login(...).
		// 3) Verify tokenService.generate(...) is called exactly once with the same Authentication instance.
	}

	@Test
	@DisplayName("propagates authentication exception when credentials are invalid")
	void login_propagatesAuthenticationException_whenCredentialsAreInvalid() {
		// Hint:
		// 1) Stub authenticationManager.authenticate(...) to throw BadCredentialsException (or AuthenticationException).
		// 2) Call controller.login(...).
		// 3) Assert the same exception type is propagated.
	}

	@Test
	@DisplayName("does not generate token when authentication fails")
	void login_doesNotGenerateToken_whenAuthenticationFails() {
		// Hint:
		// 1) Stub authenticationManager.authenticate(...) to throw an authentication exception.
		// 2) Invoke login(...), asserting the exception.
		// 3) Verify tokenService.generate(...) is never called.
	}

	@Test
	@DisplayName("forwards blank credentials to authentication manager under current controller contract")
	void login_forwardsBlankCredentials_withoutControllerValidation() {
		// Hint:
		// 1) Note that AuthController.login(...) uses @RequestBody but not @Valid.
		// 2) Create LoginRequest with blank username/password.
		// 3) Capture the Authentication passed to authenticationManager.authenticate(...).
		// 4) Assert blank values are forwarded as-is, documenting current behavior.
	}
}
