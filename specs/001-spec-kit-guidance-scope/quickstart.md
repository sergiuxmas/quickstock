# Quickstart: PRD-Aligned Spec-Kit Workflow Boundaries

## Goal

Validate that the canonical feature specification is plan-ready and aligned with repository governance boundaries.

## 1) Confirm canonical inputs

- Feature spec: `specs/001-spec-kit-guidance-scope/spec.md`
- Plan: `specs/001-spec-kit-guidance-scope/plan.md`
- Constitution: `.specify/memory/constitution.md`
- Authoritative product scope: `prd.md`
- Authoritative domain docs: `docs/revised/01-overview.md` through `docs/revised/07-ci-cd-and-release.md`
- Authoritative contracts: `quickstock-core-service/src/main/openapi/openapi.yaml` and `payments-service/src/main/openapi/openapi.yaml`
- Historical reference only: `docs/architecture/`

## 1a) Confirm explicit domain-to-source mapping

Validate `specs/001-spec-kit-guidance-scope/spec.md` contains an explicit mapping for:

- overview -> `docs/revised/01-overview.md`
- API -> `docs/revised/02-api-specification.md`
- contracts -> `quickstock-core-service/src/main/openapi/openapi.yaml` and `payments-service/src/main/openapi/openapi.yaml`
- database -> `docs/revised/03-database-design.md`
- workflows -> `docs/revised/04-workflows-and-integration.md`
- structure -> `docs/revised/05-project-structure.md`
- testing -> `docs/revised/06-testing-and-quality.md`
- CI/CD -> `docs/revised/07-ci-cd-and-release.md`

## 2) Validate required planning artifacts

Ensure these files exist for feature `001-spec-kit-guidance-scope`:

- `spec.md`
- `plan.md`
- `research.md`
- `data-model.md`
- `quickstart.md`
- `contracts/README.md`

## 3) Review governance checks

- Service scope is `both`, with no direct cross-service DB ownership changes.
- `docs/revised/01..07` and the service-owned OpenAPI files under `src/main/openapi/` are explicitly assigned by domain in spec and reflected in plan.
- `docs/architecture/` is explicitly marked historical/non-authoritative.
- Spec-Kit is documented as guidance/validation only (no autonomous full implementation claims).
- Contract impact is explicitly documented as no runtime API/callback changes.

## 4) Validate checklist readiness

Open `specs/001-spec-kit-guidance-scope/checklists/requirements.md` and confirm required items are all marked as passing before moving to tasks.

## 5) Proceed to task generation

When checks pass, run:

```bash
/speckit.tasks
```

Expected outcome: `specs/001-spec-kit-guidance-scope/tasks.md` is generated with dependency-ordered implementation tasks.

## 6) Run consistency analysis and capture readiness output

Run:

```bash
/speckit.analyze
```

Expected readiness output includes:

- active branch name
- spec path (`specs/001-spec-kit-guidance-scope/spec.md`)
- checklist status (`checklists/requirements.md` pass/fail)
- explicit readiness for `/speckit.clarify` or `/speckit.plan`


