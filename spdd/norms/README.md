# SPDD Norms — odm-platform-pp-blueprint-server

This folder holds **project-specific engineering norms** for the blueprint server. They describe *how* to implement features in this codebase, not *what* to build (that lives in `spdd/prompt/` and `spdd/analysis/`).

## SPDD artifacts

| Folder | Purpose |
|--------|---------|
| `spdd/analysis/` | Problem analysis, proposals, and testable specs (merged) |
| `spdd/prompt/` | Implementation-ready feature prompts (Requirements, Operations, Norms, Safeguards) |
| `spdd/norms/` | Reusable engineering conventions |

## Who should read this

Agents and developers running **SPDD** (Structured Prompt-Driven Development) commands should load these norms when:

- Drafting or updating a REASONS Canvas prompt (**N — Norms** section)
- Generating or syncing implementation code from a prompt
- Reviewing whether new code matches existing patterns

Treat this directory as the **canonical reference** for blueprint-server conventions referenced by prompts. Prefer linking to a norm file here over duplicating long explanations inside every prompt.

## Which norm file applies?

| You are building… | Read |
|-------------------|------|
| REST endpoint with orchestration, ports, commands, presenters (non-trivial workflow) | [`USE_CASE_IMPLEMENTATION.md`](./USE_CASE_IMPLEMENTATION.md) |
| Entity CRUD with optional DTO mapping and/or filtered list APIs | [`GENERIC-CRUD-GUIDELINES.md`](./GENERIC-CRUD-GUIDELINES.md) |
| Both (e.g. use case that delegates persistence to core CRUD services) | **Both** — use cases call core services **via outbound ports**, not directly from the use case class |

## Norm index

| File | Topic |
|------|--------|
| [USE_CASE_IMPLEMENTATION.md](./USE_CASE_IMPLEMENTATION.md) | Hexagonal use-case flow: controller → use-cases service → factory → ports/adapters |
| [GENERIC-CRUD-GUIDELINES.md](./GENERIC-CRUD-GUIDELINES.md) | Template-method CRUD (`GenericCrud*` hierarchy), mapping, filtering, hooks |

## Keeping norms up to date

Update a norm file when a **reusable pattern** changes (e.g. new use-case package layout, CRUD hook convention). After updating:

1. Adjust affected `spdd/prompt/*.md` **N — Norms** references if needed
2. Use `/spdd-sync` if generated code already exists and the prompt must reflect the new standard

Feature-specific rules (exact error messages, API contracts, permissions for one endpoint) belong in the prompt **Safeguards** section, not in this folder.
