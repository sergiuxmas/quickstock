# Testing and Quality

## Testing Strategy

Testing scope must follow the current MVP contract baseline in the service-owned OpenAPI files under `quickstock-core-service/src/main/openapi/` and `payments-service/src/main/openapi/`. Additional workflow tests apply when those endpoints are moved in-scope by a feature spec and contract update.

### Unit Tests

- Service-level tests for inventory reserve/release and payment state transitions.
- Idempotency behavior tests for payment creation and finalization.
- Validation and exception handling tests.

### Integration Tests

- Persistence and API integration tests for both services.
- Reservation expiry behavior tests.
- Concurrency test to prove no overselling under simultaneous confirm operations.

### API and Contract Tests

- Verify endpoint status codes, schema shape, and auth constraints.
- Validate callback/event payload compatibility.
- Keep OpenAPI contracts in sync with implementation behavior.

## Quality Rules

- `ddl-auto=validate` to detect schema drift at startup.
- Flyway migrations must run automatically in supported profiles.
- New behavior changes require corresponding unit/integration test updates.
- Contract changes require OpenAPI updates and compatibility checks.

## Definition of Done Highlights

- In-scope contract flows operate correctly for the current release baseline.
- Idempotency is verified for retries and duplicate notifications.
- Security is enforced for protected endpoints.
- Local docker-based run is reproducible.

