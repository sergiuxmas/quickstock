# Next 3 Coding Actions — Focus on P1 and P2

**Source**: Developer audit of `quickstock-core-service` against Scope 1 runtime-impacting expectations  
**Focus**: Only the first two original actions from the previous backlog  
**Date**: 2026-03-31

This document narrows the runtime backlog to the first original action and splits the second original action by API. The currently relevant core-service APIs for this scope are:

- `POST /auth/login`
- `GET /products`

The resulting list contains **3 actions overall**.

| Priority | Action | Why it matters | Main files involved | Dependencies | Expected outcome |
| --- | --- | --- | --- | --- | --- |
| P1 | Complete login contract alignment and request validation | `POST /auth/login` is only partially aligned with the published contract. Runtime now uses `email`/`accessToken` and email-based lookup, but controller-boundary validation and final JSON-shape integration coverage are still missing. | `quickstock-core-service/src/main/java/com/quickstock/core/controller/AuthController.java`<br>`quickstock-core-service/src/main/java/com/quickstock/core/dto/auth/LoginRequest.java`<br>`quickstock-core-service/src/main/java/com/quickstock/core/dto/auth/LoginResponse.java`<br>`quickstock-core-service/src/main/java/com/quickstock/core/security/DatabaseUserDetailsService.java`<br>`quickstock-core-service/src/test/java/com/quickstock/core/controller/AuthControllerTest.java`<br>`quickstock-core-service/src/test/java/com/quickstock/core/controller/AuthControllerIT.java`<br>`quickstock-core-service/src/main/openapi/openapi.yaml` | None | `POST /auth/login` becomes contract-compatible and rejects malformed input predictably. |
| P2 | Add consistent API error handling for `POST /auth/login` | Auth currently needs contract-shaped handling for malformed input and invalid credentials. This is the first API where the shared error model should become visible. | New `quickstock-core-service/src/main/java/com/quickstock/core/web/error/*`<br>`quickstock-core-service/src/main/java/com/quickstock/core/controller/AuthController.java`<br>`quickstock-core-service/src/main/java/com/quickstock/core/config/SecurityConfig.java`<br>`quickstock-core-service/src/test/java/com/quickstock/core/controller/AuthControllerIT.java` | P1 recommended | Auth responses consistently use the agreed error shape for invalid payloads and authentication failures. |
| P3 | Add consistent API error handling for `GET /products` | Products currently need explicit unauthorized/forbidden behavior and a consistent error payload aligned with the published contract. | New `quickstock-core-service/src/main/java/com/quickstock/core/web/error/*`<br>`quickstock-core-service/src/main/java/com/quickstock/core/controller/ProductController.java`<br>`quickstock-core-service/src/main/java/com/quickstock/core/config/SecurityConfig.java`<br>`quickstock-core-service/src/test/java/com/quickstock/core/controller/*`<br>`quickstock-core-service/src/main/openapi/openapi.yaml` | P2 recommended | `GET /products` returns consistent contract-aligned error responses for security failures and other handled API errors. |

## Action Details

### P1 — Align login contract and request validation

**Current status: Partial**

**Audit summary**
- **Done**: request field is `email` in `LoginRequest`
- **Done**: response field is `accessToken` in `LoginResponse`
- **Done**: authentication lookup remains email-based in `DatabaseUserDetailsService`
- **Remaining**: add controller-boundary validation for blank/invalid payloads in `AuthController`
- **Remaining**: update/implement tests so final HTTP JSON request/response shape is asserted end-to-end

**Concrete implementation steps**
1. Decide the source of truth: either align runtime to the OpenAPI contract or update the OpenAPI contract to match runtime behavior.
2. ✅ Runtime request field has been aligned from `username` to `email`.
3. ✅ Runtime response field has been aligned from `token` to `accessToken`.
4. ✅ Authentication lookup remains email-based end-to-end.
5. Add validation at the controller boundary for malformed or blank input (for example via `@Valid @RequestBody`).
6. Update auth unit/integration tests to assert the final JSON request/response shape.

**Notes**
- Only one source of truth should remain after this step.
- This action should be completed before API-specific error handling is finalized.
- P1 should not be marked done until steps 5 and 6 are completed.

### P2 — Add consistent API error handling for `POST /auth/login`

**Concrete implementation steps**
1. Introduce a shared error response model matching the OpenAPI error schema used by core APIs.
2. Add exception handling for malformed JSON and request validation failures on login input.
3. Add exception handling for invalid credentials so login failures return a predictable auth error response.
4. Route auth/security failures through the same payload shape used by the controller layer.
5. Implement integration tests for at least:
   - malformed JSON -> expected error response
   - blank/invalid login payload -> expected validation/auth response
   - wrong credentials -> expected auth failure response

**Notes**
- Keep this action centered on the auth API only.
- Reuse the same shared error model later for products.

### P3 — Add consistent API error handling for `GET /products`

**Concrete implementation steps**
1. Reuse the shared error response model introduced for auth.
2. Decide whether `GET /products` should be protected by JWT or intentionally remain public; keep runtime and contract aligned.
3. If protection is required, update `SecurityConfig` so unauthorized requests produce consistent `401` responses and forbidden requests produce consistent `403` responses.
4. Ensure controller/security exceptions for products use the same error payload shape.
5. Add integration tests for the chosen security policy and its error responses.

**Notes**
- This action is API-specific and should not introduce the broader order/payment backlog.
- If products remain public, update the contract instead of forcing security behavior only in code.

## Recommended Execution Order

1. P1 — Align login contract and request validation
2. P2 — Add consistent API error handling for `POST /auth/login`
3. P3 — Add consistent API error handling for `GET /products`

## Delivery Guidance

- **Best immediate value**: P1 + P2
- **Best API consistency value**: complete all three actions in order

