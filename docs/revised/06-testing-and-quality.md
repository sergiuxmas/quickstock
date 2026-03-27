# Testing and Quality

## Testing Strategy

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

- Core flows operate correctly (`create`, `confirm`, `pay`, `cancel`, `expire`).
- Idempotency is verified for retries and duplicate notifications.
- Security is enforced for protected endpoints.
- Local docker-based run is reproducible.

