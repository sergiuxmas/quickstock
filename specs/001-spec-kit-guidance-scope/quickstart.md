# Quickstart: PRD-Aligned Spec-Kit Workflow Boundaries

## Goal

Validate that the canonical feature specification is plan-ready and aligned with repository governance boundaries.

## 1) Confirm canonical inputs

- Feature spec: `specs/001-spec-kit-guidance-scope/spec.md`
- Plan: `specs/001-spec-kit-guidance-scope/plan.md`
- Constitution: `.specify/memory/constitution.md`
- Authoritative product scope: `prd.md`

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
- `docs/revised/` authority rule is preserved in spec and plan content.
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


