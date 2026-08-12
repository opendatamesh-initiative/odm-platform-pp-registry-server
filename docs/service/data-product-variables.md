# Data product variables

High-level view of how descriptor variables work in Registry V2.

Related:

- [Data product lifecycle](data-product-lifecycle.md) — versions that own variables
- [Descriptor validation](descriptor-validation.md) — publish-time descriptor checks
- [V1 backward compatibility](v1-backward-compatibility.md) — legacy list/update paths

## Role

Descriptors can include **placeholders** of the form `${variableKey}`. A **descriptor variable** is a key/value pair stored against a specific **data product version**. Clients set those values separately from the descriptor content; the Registry can then **resolve** a version by substituting placeholders with the stored values.

Typical uses: environment-specific URLs, endpoints, or other config that should not be hard-coded in the published descriptor.

Variables do **not** change validation state (`PENDING` / `APPROVED` / `REJECTED`). Resolve is a read-side operation: it returns a version with substituted content and does not rewrite the stored descriptor.

## How placeholders work

In a DPDS descriptor (spec `DPDS`, version `1.*.*`), any string occurrence of `${myKey}` can be bound to a variable whose `variableKey` is `myKey`.

```text
Descriptor content (stored as published)
    └── "${apiBaseUrl}", "${ownerEmail}", …
              │
              ▼
Descriptor variables (per data product version)
    └── apiBaseUrl  →  https://…
    └── ownerEmail  →  team@…
              │
              ▼
Resolve → descriptor with placeholders replaced
```

On **store**, for DPDS 1.x the Registry checks that each variable key appears as `${variableKey}` in that version’s descriptor; unknown keys are rejected. Stored content keeps the placeholders until someone resolves.

## Managing variables

Variables are first-class resources under `/api/v2/pp/registry/descriptor-variables`:

| Capability | What it does |
|------------|--------------|
| **CRUD / search** | Create, read, update, delete, and filter variables (including by data product version UUID) |
| **Store (use case)** | Create or override one or more variables for a version, with presence validation against the descriptor |

Each variable has:

- A sequence id
- The parent **data product version UUID**
- A **key** (matches the placeholder name)
- A **value** (substituted on resolve)

## Resolve

`POST .../data-product-versions/resolve` (use-case API) loads the version and its variables, replaces each `${key}` with the corresponding value (JSON-escaped), and returns the version with **resolved** descriptor content.

- Only **DPDS 1.x** is supported for resolve today
- Unresolved placeholders (no stored value, or key never stored) remain as-is in the output
- The persisted descriptor on the version is unchanged

## Compared with V1

V1 exposes version-scoped list/update under `/api/v1/.../versions/{version}/variables` and resolves placeholders when fetching a version descriptor. V2 keeps variables as their own resources plus an explicit resolve use case. Details of the V1 surface: [V1 backward compatibility](v1-backward-compatibility.md) and the [`old` package README](../../src/main/java/org/opendatamesh/platform/pp/registry/old/README.md).
