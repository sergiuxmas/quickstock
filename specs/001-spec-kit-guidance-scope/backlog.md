# Scope 1 Runtime Backlog

This backlog belongs to feature `001-spec-kit-guidance-scope` and tracks the remaining runtime-impacting follow-up work identified during documentation and implementation analysis.

## Active Backlog Items

| ID | Priority | Task | Depends on | Status | Notes |
| --- | --- | --- | --- | --- | --- |
| BL-001 | P1 | Complete login contract alignment and request validation | None | Open | `POST /auth/login` still needs controller-boundary validation and final end-to-end JSON shape verification against the published contract. |
| BL-002 | P2 | Add consistent API error handling for `POST /auth/login` | BL-001 recommended | Open | Establish the shared core-service error model first on auth, including malformed input and invalid credentials. |
| BL-003 | P3 | Add consistent API error handling for `GET /products` | BL-002 recommended | Open | Reuse the same error model for product API behavior and align security/runtime behavior with the contract. |
| BL-004 | Supporting | Option B — implement real app-level `@RestControllerAdvice` | BL-002, BL-003 | Open | Recommended implementation mechanism for centralized exception-to-response mapping across auth and product APIs. |

## Backlog Details

### BL-001 — Complete login contract alignment and request validation

**Source**: Developer audit of `quickstock-core-service` against Scope 1 runtime-impacting expectations  
**Current status**: Partial

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

**Main files involved**
- `quickstock-core-service/src/main/java/com/quickstock/core/controller/AuthController.java`
- `quickstock-core-service/src/main/java/com/quickstock/core/dto/auth/LoginRequest.java`
- `quickstock-core-service/src/main/java/com/quickstock/core/dto/auth/LoginResponse.java`
- `quickstock-core-service/src/main/java/com/quickstock/core/security/DatabaseUserDetailsService.java`
- `quickstock-core-service/src/test/java/com/quickstock/core/controller/AuthControllerTest.java`
- `quickstock-core-service/src/test/java/com/quickstock/core/controller/AuthControllerIT.java`
- `quickstock-core-service/src/main/openapi/openapi.yaml`

**Expected outcome**
- `POST /auth/login` becomes contract-compatible and rejects malformed input predictably.

### BL-002 — Add consistent API error handling for `POST /auth/login`

**Concrete implementation steps**
1. Introduce a shared error response model matching the OpenAPI error schema used by core APIs.
2. Add exception handling for malformed JSON and request validation failures on login input.
3. Add exception handling for invalid credentials so login failures return a predictable auth error response.
4. Route auth/security failures through the same payload shape used by the controller layer.
5. Implement integration tests for at least:
   - malformed JSON -> expected error response
   - blank/invalid login payload -> expected validation/auth response
   - wrong credentials -> expected auth failure response

**Main files involved**
- New `quickstock-core-service/src/main/java/com/quickstock/core/web/error/*`
- `quickstock-core-service/src/main/java/com/quickstock/core/controller/AuthController.java`
- `quickstock-core-service/src/main/java/com/quickstock/core/config/SecurityConfig.java`
- `quickstock-core-service/src/test/java/com/quickstock/core/controller/AuthControllerIT.java`

**Expected outcome**
- Auth responses consistently use the agreed error shape for invalid payloads and authentication failures.

**Relationship notes**
- Keep this item centered on the auth API only.
- BL-004 is a recommended implementation mechanism for this backlog item.

### BL-003 — Add consistent API error handling for `GET /products`

**Concrete implementation steps**
1. Reuse the shared error response model introduced for auth.
2. Decide whether `GET /products` should be protected by JWT or intentionally remain public; keep runtime and contract aligned.
3. If protection is required, update `SecurityConfig` so unauthorized requests produce consistent `401` responses and forbidden requests produce consistent `403` responses.
4. Ensure controller/security exceptions for products use the same error payload shape.
5. Add integration tests for the chosen security policy and its error responses.

**Main files involved**
- New `quickstock-core-service/src/main/java/com/quickstock/core/web/error/*`
- `quickstock-core-service/src/main/java/com/quickstock/core/controller/ProductController.java`
- `quickstock-core-service/src/main/java/com/quickstock/core/config/SecurityConfig.java`
- `quickstock-core-service/src/test/java/com/quickstock/core/controller/*`
- `quickstock-core-service/src/main/openapi/openapi.yaml`

**Expected outcome**
- `GET /products` returns consistent contract-aligned error responses for security failures and other handled API errors.

**Relationship notes**
- BL-003 should reuse the shared model introduced by BL-002.
- BL-004 is also relevant here if exception mapping is centralized.

### BL-004 — Option B: implement real app-level `@RestControllerAdvice`

**Purpose**
- Provide a centralized exception-to-response mapping layer for core-service REST APIs.

**Why it matters**
- `BadCredentialsException`, request validation failures, malformed JSON, and future controller exceptions should resolve into consistent contract-shaped HTTP responses instead of leaking framework exceptions into tests or clients.

**Recommended use**
1. Implement the advice in the main application, not only in tests.
2. Use it to support BL-002 first, then extend/reuse it for BL-003.
3. Keep the produced payload aligned with `quickstock-core-service/src/main/openapi/openapi.yaml`.

**Main files involved**
- New `quickstock-core-service/src/main/java/com/quickstock/core/web/error/*`
- `quickstock-core-service/src/test/java/com/quickstock/core/controller/AuthControllerIT.java`
- `quickstock-core-service/src/test/java/com/quickstock/core/controller/*`
- `quickstock-core-service/src/main/openapi/openapi.yaml`

## Recommended Execution Order

1. BL-001 — Complete login contract alignment and request validation
2. BL-002 — Add consistent API error handling for `POST /auth/login`
3. BL-004 — Implement real app-level `@RestControllerAdvice` as the preferred shared mechanism for BL-002/BL-003
4. BL-003 — Add consistent API error handling for `GET /products`


