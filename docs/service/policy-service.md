# Policy service integration

High-level view of how Policy participates in data product and version approval.

Related:

- [Data product lifecycle](data-product-lifecycle.md) — states Policy decisions change
- [Events](events.md) — the bus Policy and Registry share
- [Configuration](../setup/configuration.md) — flags and addresses
- [V1 backward compatibility](v1-backward-compatibility.md) — Policy Service V1 bridge

## Role

Policy is the optional **governance gate** on init and publish.

After the Registry creates a product or version as `PENDING` and emits a `*_REQUESTED` event, something must decide approve or reject:

- **Policy active** — an external Policy service evaluates the request and emits `*_APPROVED` or `*_REJECTED`
- **Policy inactive** — the Registry auto-approves by emitting `*_APPROVED` itself

In both cases, Notification carries the messages, and the Registry Observer applies the final state change.

Policy does **not** replace Notification. Notification is still required for the event loop.

## Active vs inactive

| Mode | When | What happens |
|------|------|--------------|
| **Active** | `odm.product-plane.policy-service.active=true` (with Policy V1: also `version=1` + `address`) | Registry waits for Policy decisions on `*_REQUESTED` |
| **Inactive** | `active=false` (or unset — auto-approve is the default when inactive) | Registry auto-approves `*_REQUESTED` so local/dev setups can complete without Policy |

```text
Init / Publish → PENDING + *_REQUESTED
                      │
        ┌─────────────┴─────────────┐
        ▼                           ▼
  Policy active               Policy inactive
  evaluate & decide           auto-approve
        │                           │
        └─────────────┬─────────────┘
                      ▼
              APPROVED or REJECTED
```

## What you need to configure

- Always (for the lifecycle loop): Notification active + reachable, and `server.baseUrl` reachable by Notification
- Policy on: `policy-service.active`, plus `address` and `version` (and optional descriptor parser version) — see [Configuration](../setup/configuration.md)
- Policy off: set `active=false`; no Policy address required

How this maps onto events: [Events](events.md). Legacy Policy V1 payloads and parsers: [V1 backward compatibility](v1-backward-compatibility.md).
