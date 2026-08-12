# Events

High-level view of how the Registry uses the Notification service to drive lifecycle workflows, plus a complete catalog of event signatures.

Related:

- [Data product lifecycle](data-product-lifecycle.md) — states these events move
- [Policy service](policy-service.md) — who decides approve vs reject
- [Configuration](../setup/configuration.md) — Notification and observer settings

## Role of events

The Registry does not approve init/publish synchronously in the happy path. It **emits** events when something important happens, and **reacts** to events when an approval or rejection decision arrives.

The Notification service is the bus:

- Registry **publishes** events (e.g. “initialization requested”, “version published”)
- Other services (notably Policy) or the Registry itself (auto-approve) **consume** and respond
- Notification **delivers** decisions back to the Registry Observer callback

Without Notification active, that loop does not run (the client becomes a no-op). Prefer keeping Notification enabled wherever you rely on init/publish approval.

## How the Registry joins the bus

At startup (when Notification is active), the Registry:

1. Checks that Notification is reachable
2. Registers as an **observer** (callback URL = `server.baseUrl`, plus observer name/display name)
3. Subscribes to the event types it can handle

Inbound decisions arrive at `POST /api/v2/up/observer/notifications`. Property details: [Configuration](../setup/configuration.md).

```text
Registry emits *_REQUESTED
        │
        ▼
  Policy or auto-approve
        │
        ▼
Registry receives *_APPROVED / *_REJECTED
        │
        ▼
  State change (+ INITIALIZED / PUBLISHED on approve)
```

## Event catalog

| Event type                                   | Direction                                                | Resource type          |
| -------------------------------------------- | -------------------------------------------------------- | ---------------------- |
| `DATA_PRODUCT_INITIALIZATION_REQUESTED`      | Emitted (also received when Policy inactive / Policy V1) | `DATA_PRODUCT`         |
| `DATA_PRODUCT_INITIALIZED`                   | Emitted                                                  | `DATA_PRODUCT`         |
| `DATA_PRODUCT_DELETED`                       | Emitted                                                  | `DATA_PRODUCT`         |
| `DATA_PRODUCT_VERSION_PUBLICATION_REQUESTED` | Emitted (also received when Policy inactive / Policy V1) | `DATA_PRODUCT_VERSION` |
| `DATA_PRODUCT_VERSION_PUBLISHED`             | Emitted                                                  | `DATA_PRODUCT_VERSION` |
| `DATA_PRODUCT_VERSION_DELETED`               | Emitted                                                  | `DATA_PRODUCT_VERSION` |
| `DATA_PRODUCT_INITIALIZATION_APPROVED`       | Received (also emitted by auto-approve / Policy)         | `DATA_PRODUCT`         |
| `DATA_PRODUCT_INITIALIZATION_REJECTED`       | Received (emitted by Policy)                             | `DATA_PRODUCT`         |
| `DATA_PRODUCT_VERSION_PUBLICATION_APPROVED`  | Received (also emitted by auto-approve / Policy)         | `DATA_PRODUCT_VERSION` |
| `DATA_PRODUCT_VERSION_PUBLICATION_REJECTED`  | Received (emitted by Policy)                             | `DATA_PRODUCT_VERSION` |

Rejectors do **not** emit a dedicated completion event; rejection is carried by `*_REJECTED`.

### Common envelope

When the Registry **emits**, Notification receives:

```json
{
  "event": {
    "resourceType": "DATA_PRODUCT | DATA_PRODUCT_VERSION",
    "resourceIdentifier": "<uuid>",
    "type": "<EVENT_TYPE>",
    "eventTypeVersion": "V2.0.0",
    "eventContent": {}
  }
}
```

| Field                | Meaning                                                               |
| -------------------- | --------------------------------------------------------------------- |
| `resourceType`       | `DATA_PRODUCT` or `DATA_PRODUCT_VERSION`                              |
| `resourceIdentifier` | UUID of the resource                                                  |
| `type`               | Event type name                                                       |
| `eventTypeVersion`   | Schema version of the event — Registry V2 emit resources use `V2.0.0` |
| `eventContent`       | Event-specific payload (signatures below)                             |

When the Registry **receives** an event via the Observer, Notification wraps it in a dispatch notification (`sequenceId`, `event`, `subscription`). The inner `event` has the same fields (plus optional `sequenceId` on the event). Handlers map `eventContent` into the received types below.

`DataProductRes` / `DataProductVersionRes` below mean the full V2 API resources (see Swagger UI).

---

### Emitted by lifecycle use cases

#### `DATA_PRODUCT_INITIALIZATION_REQUESTED`

Emitted when a data product is initialized (`PENDING`). Asks the bus for an approval decision.

```json
{
  "resourceType": "DATA_PRODUCT",
  "resourceIdentifier": "<data-product-uuid>",
  "type": "DATA_PRODUCT_INITIALIZATION_REQUESTED",
  "eventTypeVersion": "V2.0.0",
  "eventContent": {
    "dataProduct": {
      /* DataProductRes */
    }
  }
}
```

#### `DATA_PRODUCT_INITIALIZED`

Emitted after the product is approved (`PENDING` → `APPROVED`).

```json
{
  "resourceType": "DATA_PRODUCT",
  "resourceIdentifier": "<data-product-uuid>",
  "type": "DATA_PRODUCT_INITIALIZED",
  "eventTypeVersion": "V2.0.0",
  "eventContent": {
    "dataProduct": {
      /* DataProductRes (updated status) */
    }
  }
}
```

#### `DATA_PRODUCT_DELETED`

Emitted when a data product is deleted.

```json
{
  "resourceType": "DATA_PRODUCT",
  "resourceIdentifier": "<data-product-uuid>",
  "type": "DATA_PRODUCT_DELETED",
  "eventTypeVersion": "V2.0.0",
  "eventContent": {
    "dataProductUuid": "<data-product-uuid>",
    "dataProductFqn": "<fully-qualified-name>"
  }
}
```

#### `DATA_PRODUCT_VERSION_PUBLICATION_REQUESTED`

Emitted when a version is published (`PENDING`). Asks the bus for an approval decision.

`previousDataProductVersion` is the latest other version for the same product (by `createdAt` descending), or `null` if this is the first version.

```json
{
  "resourceType": "DATA_PRODUCT_VERSION",
  "resourceIdentifier": "<data-product-version-uuid>",
  "type": "DATA_PRODUCT_VERSION_PUBLICATION_REQUESTED",
  "eventTypeVersion": "V2.0.0",
  "eventContent": {
    "dataProductVersion": { /* DataProductVersionRes */ },
    "previousDataProductVersion": { /* DataProductVersionRes */ } | null
  }
}
```

#### `DATA_PRODUCT_VERSION_PUBLISHED`

Emitted after the version is approved (`PENDING` → `APPROVED`).

```json
{
  "resourceType": "DATA_PRODUCT_VERSION",
  "resourceIdentifier": "<data-product-version-uuid>",
  "type": "DATA_PRODUCT_VERSION_PUBLISHED",
  "eventTypeVersion": "V2.0.0",
  "eventContent": {
    "dataProductVersion": {
      /* DataProductVersionRes (updated status) */
    }
  }
}
```

#### `DATA_PRODUCT_VERSION_DELETED`

Emitted when a data product version is deleted.

```json
{
  "resourceType": "DATA_PRODUCT_VERSION",
  "resourceIdentifier": "<data-product-version-uuid>",
  "type": "DATA_PRODUCT_VERSION_DELETED",
  "eventTypeVersion": "V2.0.0",
  "eventContent": {
    "dataProductVersionUuid": "<data-product-version-uuid>",
    "dataProductFqn": "<fully-qualified-name>",
    "dataProductVersionNumber": "<version-number>",
    "dataProductVersionTag": "<version-tag>"
  }
}
```

---

### Decision events (approve / reject)

These are **received** by the Registry Observer to complete the lifecycle. They are also **emitted** by:

- Registry auto-approve (when Policy is inactive) — approve only
- Policy Service V1 bridge (when Policy is active with `version=1`) — approve or reject

Received handlers expect a **lightweight** `eventContent` (identifiers), not the full `DataProductRes` / `DataProductVersionRes`.

#### `DATA_PRODUCT_INITIALIZATION_APPROVED`

```json
{
  "resourceType": "DATA_PRODUCT",
  "resourceIdentifier": "<data-product-uuid>",
  "type": "DATA_PRODUCT_INITIALIZATION_APPROVED",
  "eventTypeVersion": "V2.0.0",
  "eventContent": {
    "dataProduct": {
      "uuid": "<data-product-uuid>",
      "fqn": "<fully-qualified-name>"
    }
  }
}
```

Effect: approve product → emit `DATA_PRODUCT_INITIALIZED`.

#### `DATA_PRODUCT_INITIALIZATION_REJECTED`

```json
{
  "resourceType": "DATA_PRODUCT",
  "resourceIdentifier": "<data-product-uuid>",
  "type": "DATA_PRODUCT_INITIALIZATION_REJECTED",
  "eventTypeVersion": "V2.0.0",
  "eventContent": {
    "dataProduct": {
      "uuid": "<data-product-uuid>",
      "fqn": "<fully-qualified-name>"
    }
  }
}
```

Effect: reject product → `REJECTED` (no further emit from the Rejector).

#### `DATA_PRODUCT_VERSION_PUBLICATION_APPROVED`

```json
{
  "resourceType": "DATA_PRODUCT_VERSION",
  "resourceIdentifier": "<data-product-version-uuid>",
  "type": "DATA_PRODUCT_VERSION_PUBLICATION_APPROVED",
  "eventTypeVersion": "V2.0.0",
  "eventContent": {
    "dataProductVersion": {
      "uuid": "<data-product-version-uuid>",
      "tag": "<version-tag>",
      "dataProduct": {
        "uuid": "<data-product-uuid>",
        "fqn": "<fully-qualified-name>"
      }
    }
  }
}
```

Effect: approve version → emit `DATA_PRODUCT_VERSION_PUBLISHED`.

#### `DATA_PRODUCT_VERSION_PUBLICATION_REJECTED`

```json
{
  "resourceType": "DATA_PRODUCT_VERSION",
  "resourceIdentifier": "<data-product-version-uuid>",
  "type": "DATA_PRODUCT_VERSION_PUBLICATION_REJECTED",
  "eventTypeVersion": "V2.0.0",
  "eventContent": {
    "dataProductVersion": {
      "uuid": "<data-product-version-uuid>",
      "tag": "<version-tag>",
      "dataProduct": {
        "uuid": "<data-product-uuid>",
        "fqn": "<fully-qualified-name>"
      }
    }
  }
}
```

Effect: reject version → `REJECTED` (no further emit from the Rejector).

---

### Quick reference: `eventContent` signatures

| Event type                                   | `eventContent` signature                                                                                                      |
| -------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------- |
| `DATA_PRODUCT_INITIALIZATION_REQUESTED`      | `{ dataProduct: DataProductRes }`                                                                                             |
| `DATA_PRODUCT_INITIALIZED`                   | `{ dataProduct: DataProductRes }`                                                                                             |
| `DATA_PRODUCT_DELETED`                       | `{ dataProductUuid: string, dataProductFqn: string }`                                                                         |
| `DATA_PRODUCT_VERSION_PUBLICATION_REQUESTED` | `{ dataProductVersion: DataProductVersionRes, previousDataProductVersion: DataProductVersionRes \| null }`                    |
| `DATA_PRODUCT_VERSION_PUBLISHED`             | `{ dataProductVersion: DataProductVersionRes }`                                                                               |
| `DATA_PRODUCT_VERSION_DELETED`               | `{ dataProductVersionUuid: string, dataProductFqn: string, dataProductVersionNumber: string, dataProductVersionTag: string }` |
| `DATA_PRODUCT_INITIALIZATION_APPROVED`       | `{ dataProduct: { uuid: string, fqn: string } }`                                                                              |
| `DATA_PRODUCT_INITIALIZATION_REJECTED`       | `{ dataProduct: { uuid: string, fqn: string } }`                                                                              |
| `DATA_PRODUCT_VERSION_PUBLICATION_APPROVED`  | `{ dataProductVersion: { uuid: string, tag: string, dataProduct: { uuid: string, fqn: string } } }`                           |
| `DATA_PRODUCT_VERSION_PUBLICATION_REJECTED`  | `{ dataProductVersion: { uuid: string, tag: string, dataProduct: { uuid: string, fqn: string } } }`                           |
