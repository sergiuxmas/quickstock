# Data Model: PRD-Aligned Spec-Kit Workflow Boundaries

## Entity: FeatureSpecificationArtifact

- Purpose: Canonical feature definition for scope, requirements, and governance boundaries.
- Fields:
  - `feature_id` (string, required, pattern `^[0-9]{3}-[a-z0-9-]+$`)
  - `title` (string, required)
  - `service_scope` (enum: `core`, `payments`, `both`, required)
  - `spec_version` (string, required, semantic version)
  - `versioning_policy` (string, required)
  - `requirements` (list, required, non-empty)
  - `acceptance_scenarios` (list, required, non-empty)
  - `success_criteria` (list, required, non-empty)
  - `assumptions` (list, optional)
  - `changelog` (list, required, non-empty)
- Validation rules:
  - `spec_version` must increment by semantic version rules based on changelog impact.
  - Every functional requirement must map to acceptance scenarios or checklist items.
  - Spec content must not claim full autonomous implementation by Spec-Kit.
- State transitions:
  - `Draft` -> `Validated` (all mandatory sections complete and checklist passes)
  - `Validated` -> `ReadyForPlan` (constitution gate pass recorded)

## Entity: QualityChecklistArtifact

- Purpose: Evidence that the feature spec is complete, testable, and ready for planning.
- Fields:
  - `checklist_id` (string, required)
  - `feature_id` (string, required, foreign key to `FeatureSpecificationArtifact.feature_id`)
  - `reviewed_spec_version` (string, required)
  - `items` (list of checklist item records, required)
  - `overall_status` (enum: `pass`, `fail`, required)
  - `open_issues` (list, optional)
  - `review_date` (date, required)
- Validation rules:
  - `reviewed_spec_version` must equal the current `Specification Version` in `spec.md`.
  - `overall_status` is `pass` only when all required checklist items pass.
- State transitions:
  - `InProgress` -> `Passed` (all required checks pass)
  - `InProgress` -> `Failed` (one or more required checks fail)

## Entity: GovernanceConstraintSet

- Purpose: Normalized set of rules enforced from PRD and constitution.
- Fields:
  - `constraint_id` (string, required)
  - `source` (enum: `prd`, `constitution`, `docs/revised`, required)
  - `statement` (string, required)
  - `applies_to` (enum: `core`, `payments`, `both`, `process`, required)
  - `mandatory` (boolean, required)
- Validation rules:
  - Conflicts are resolved by documented precedence (`docs/revised/` authoritative; legacy docs non-authoritative).
  - Service-boundary constraints cannot be weakened by feature-local artifacts.

## Relationships

- `FeatureSpecificationArtifact` 1 -> 1 `QualityChecklistArtifact`
- `FeatureSpecificationArtifact` N -> N `GovernanceConstraintSet`

## Notes for this feature

- No runtime domain entities or database schema changes are introduced.
- Data model exists to describe governance artifacts and their validation lifecycle only.

