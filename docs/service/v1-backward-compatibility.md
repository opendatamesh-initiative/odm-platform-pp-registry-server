# V1 backward compatibility

High-level view of what the Registry still exposes for legacy V1 clients, and where the technical detail lives.

Related:

- [What's new in V2](v2-whats-new.md) — conceptual differences
- [Policy service](policy-service.md) — how Policy plugs into approval
- [Data product variables](data-product-variables.md) — V2 variable model
- **Technical reference:** [`old` package README](../../src/main/java/org/opendatamesh/platform/pp/registry/old/README.md)

## Role

Registry V2 is the primary surface (`/api/v2/...`). The **`old` package** keeps a **compatibility bridge** so existing V1 clients and Policy Service V1 deployments can keep working without migrating immediately.

That bridge:

- Mirrors selected **Registry V1** REST contracts under `/api/v1/pp/registry`
- Integrates **Policy Service V1** into the same event-driven approve/reject loop used by V2
- Maps V1 DTOs onto current entities and services (no parallel domain)

Code outside `old` must not depend on it; the package may call into core services. It is intended to shrink as clients move to V2.

## What is supported (at a glance)

### Registry API V1 (`/api/v1/pp/registry`)

| Area | Supported behavior |
|------|--------------------|
| **Products** | List/find by FQN; get product metadata in the V1 DTO shape |
| **Versions** | Get a version descriptor (canonical/normalized), with `${variable}` placeholders resolved from stored values |
| **Variables** | List variables for a version (auto-creates missing keys found in the descriptor); update a variable value by id |
| **Validate report** | Syntax and/or Policy V1 validation of a descriptor; outcome in the body (HTTP 200), not only via status codes |
| **Identity** | Path `id` accepts both legacy FQN-derived ids and V2 data product UUIDs |

Not exposed as V1 use-case APIs: init / publish / approve lifecycle, Git / repository providers, and the broader V2 CRUD surface. Prefer V2 for those.

### Policy Service V1

When `odm.product-plane.policy-service.active=true` and `version=1`, the Registry listens for `*_REQUESTED` events, evaluates them against Policy Service V1, and emits `*_APPROVED` / `*_REJECTED`. Descriptors sent to Policy can use the old or current parser depending on configuration.

### Parsers and IDs

Compatibility often relies on the **old DPDS parser** (v1.x) so JSON shape matches what V1 clients and OPA policies expect. Organization name (`odm.organization.name`) drives FQN → legacy id generation so existing clients keep finding the same products.

## Where to go next

For endpoints, request/response fields, dual-id lookup, parser versions, and full configuration tables, use the package README:

→ [`src/main/java/org/opendatamesh/platform/pp/registry/old/README.md`](../../src/main/java/org/opendatamesh/platform/pp/registry/old/README.md)

Property reference in context of the whole service: [Configuration](../setup/configuration.md).
