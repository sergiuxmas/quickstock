# Phase 0 Research: PRD-Aligned Spec-Kit Workflow Boundaries

## Decision 1: Canonical feature artifact path remains `specs/<feature>/`

- Decision: Keep planning artifacts under `specs/001-spec-kit-guidance-scope/`.
- Rationale: The active repository layout already uses `specs/`, and `.specify/scripts/bash/common.sh` resolves feature directories from `specs/` by default.
- Alternatives considered:
  - Move this feature to `docs/specs/...` now: rejected because it conflicts with current script behavior and existing feature history.
  - Duplicate artifacts in both `specs/` and `docs/specs/`: rejected due to drift risk.

## Decision 2: Use Java 17 as the planning baseline

- Decision: Record Java 17 in the technical context for planning metadata.
- Rationale: Root `pom.xml` defines `<java.version>17</java.version>`, and README now matches Java 17 for documentation consistency.
- Alternatives considered:
  - Mark language version as unresolved: rejected because build and docs are aligned to Java 17.

## Decision 3: Treat this as a governance/process-only feature with no runtime contract deltas

- Decision: No OpenAPI changes are required; document contract impact as no-change in `contracts/README.md`.
- Rationale: The feature modifies specification workflow boundaries and versioning governance, not service API behavior.
- Alternatives considered:
  - Force OpenAPI edits for traceability: rejected because it would create artificial contract churn unrelated to behavior.
  - Omit contracts folder entirely: rejected to keep explicit audit evidence for constitution checks.

## Decision 4: Validation focuses on checklist completeness and traceability, not service test execution

- Decision: Validation consists of requirements checklist completion, PRD-to-spec traceability, and constitution compliance checks.
- Rationale: No production code paths or runtime data flows are changed by this feature.
- Alternatives considered:
  - Run full unit/integration suites as mandatory gate: rejected as disproportionate for documentation-only scope.
  - Skip validation because no code changes: rejected because governance quality still needs measurable evidence.


