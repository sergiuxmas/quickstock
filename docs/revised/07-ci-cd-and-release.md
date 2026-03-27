# CI/CD and Release

## Pipeline Objectives

- Keep build, test, and migration checks deterministic.
- Enforce contract-first API governance.
- Prevent release of incompatible schema or API changes.

## Recommended CI Stages

1. Build and dependency resolution for parent and service modules.
2. Unit tests for each service module.
3. Integration tests (profile or container-based).
4. Flyway migration validation against clean databases.
5. OpenAPI/contract validation for changed APIs.
6. Packaging and image build.

## Required Quality Gates

- Fail pipeline if unit/integration tests fail.
- Fail pipeline if migration validation fails.
- Fail pipeline if contract validation fails.
- Fail pipeline if compatibility checks detect unapproved breaking changes.

## Release Checklist

- Migration scripts for all schema changes.
- API contract updates for API/callback changes.
- Backward compatibility note or version bump for breaking changes.
- Updated docs in `docs/revised/` for architecture-affecting changes.

## Suggested Ownership

- Service owner updates code + migrations + contracts.
- Reviewer verifies compatibility and operational impact.
- PR checklist includes a docs/contract/migration confirmation section.

