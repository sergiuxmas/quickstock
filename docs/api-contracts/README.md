# API Contracts

This directory contains the canonical OpenAPI contracts for QuickStock services.

## Files

- `quickstock-core-service.openapi.yaml`
- `payments-service.openapi.yaml`

## Governance

- These files are the API contract source of truth.
- Any API/callback behavior change must update the relevant OpenAPI file in the same change set.
- Contract changes should preserve backward compatibility unless a documented versioned breaking change is approved.

