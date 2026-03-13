package com.quickstock.core.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class JwtAuthConverterTest {

	@Test
	@DisplayName("creates converter bean instance")
	void jwtAuthenticationConverter_createsBean() {
		// Hint:
		// 1) Instantiate JwtAuthConverter directly.
		// 2) Call jwtAuthenticationConverter().
		// 3) Assert converter is not null.
	}

	@Test
	@DisplayName("maps role claim to a single authority")
	void convert_mapsRoleClaimToSingleAuthority() {
		// Hint:
		// 1) Build Jwt with claim role = "ROLE_ADMIN".
		// 2) Call converter.convert(jwt).
		// 3) Assert result is JwtAuthenticationToken.
		// 4) Assert authorities contains exactly ROLE_ADMIN.
	}

	@Test
	@DisplayName("returns empty authorities when role claim is missing")
	void convert_returnsEmptyAuthoritiesWhenRoleClaimMissing() {
		// Hint:
		// 1) Build Jwt without role claim.
		// 2) Convert token.
		// 3) Assert authorities collection is empty.
	}

	@Test
	@DisplayName("preserves original Jwt instance in authentication token")
	void convert_preservesOriginalJwtReference() {
		// Hint:
		// 1) Build Jwt with any valid claims.
		// 2) Convert token.
		// 3) Cast to JwtAuthenticationToken and assert getToken() (or getTokenAttributes)
		//    corresponds to the same JWT used as input.
	}

	@Test
	@DisplayName("uses role claim value as-is when creating authority")
	void convert_usesRoleClaimAsIs() {
		// Hint:
		// 1) Use role claim like "role_admin" or "ADMIN" (without ROLE_ prefix).
		// 2) Convert token.
		// 3) Assert authority value is exactly the provided claim string (no normalization).
	}

	@Test
	@DisplayName("throws when role claim is blank and authority cannot be created")
	void convert_throwsForBlankRoleClaim() {
		// Hint:
		// 1) Build Jwt with role claim = "" (or only spaces).
		// 2) Convert and assert exception behavior from SimpleGrantedAuthority.
		// 3) If current behavior differs, document actual outcome and align expectation.
	}

	@Test
	@DisplayName("fails when role claim type is not a string")
	void convert_failsForNonStringRoleClaimType() {
		// Hint:
		// 1) Build Jwt with role claim as non-string (e.g., List or Integer).
		// 2) Call convert(jwt).
		// 3) Assert conversion/authority creation exception or actual framework behavior.
	}

	@Test
	@DisplayName("supports customer role claim")
	void convert_mapsCustomerRole() {
		// Hint:
		// 1) Build Jwt with role = "ROLE_CUSTOMER".
		// 2) Convert token.
		// 3) Assert single authority ROLE_CUSTOMER is present.
	}
}
