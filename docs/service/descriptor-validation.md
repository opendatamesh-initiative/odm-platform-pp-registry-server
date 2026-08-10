# Descriptor validation

High-level view of what happens to a descriptor when a data product version is published.

Related: [Data product lifecycle](data-product-lifecycle.md) · [What's new in V2](v2-whats-new.md)

## Role

On **publish**, the Registry validates the submitted descriptor before storing the version as `PENDING`. Validation:

- Checks that required fields exist and have a valid format
- Fills in certain missing fields automatically (IDs, entity types, many component FQNs)
- Fails the publish if mandatory information is missing or invalid

Today only **DPDS** (Data Product Descriptor Specification) is supported, following [DPDS 1.0.0](https://dpds.opendatamesh.org/specifications/dpds/1.0.0/). The spec type comes from the version’s `spec` field (descriptor version defaults to `1.0.0` when `specVersion` is omitted).

## The one field you must always provide

`info.fullyQualifiedName` is **required** and **never auto-generated**. It is the root identity of the product in the descriptor and the base for generating other component FQNs.

Expected format:

```text
urn:dpds:{mesh-namespace}:dataproducts:{product-name}:{product-major-version}
```

Example: `urn:dpds:it.quantyca:dataproducts:tripExecution:1`

If it is missing, validation fails. It must also match the parent data product’s FQN in the Registry. All other component FQNs can be auto-generated if missing.

## What you provide vs what the Registry fills in

**You must provide** (among others):

- Root descriptor version and the `info` / `interfaceComponents` objects
- Product name, version, domain, owner, and **fullyQualifiedName**
- Port / component **names** and **versions** where those components exist
- `outputPorts` must be present as a field (the list may be empty)

**The Registry can generate if missing:**

- `entityType` values (e.g. `dataproduct`, `inputport`, `application`, …)
- Component and standard-definition **FQNs** derived from the root FQN (or mesh namespace rules)
- **IDs** as UUID v5 from each component’s FQN

Wrong `entityType` values are rejected; missing ones are corrected. Component names must be unique within their scope (e.g. within a port type). Versions follow semantic versioning when present.

## Field requirements and auto-generation

| Component/Field | Required | Auto-Generated | Notes |
|----------------|----------|----------------|-------|
| **Root Level** |
| `dataProductDescriptor` | Yes | No | Must be valid semantic version (MAJOR.MINOR.PATCH) |
| `info` | Yes | No | Root info object must exist |
| `interfaceComponents` | Yes | No | Root interface components object must exist |
| **Info Object** |
| `info.name` | Yes | No | Data product name |
| `info.version` | Yes | No | Must be valid semantic version |
| `info.domain` | Yes | No | Data product domain |
| `info.fullyQualifiedName` | Yes | No | **Cannot be auto-generated** — must be provided |
| `info.entityType` | No | Yes | Auto-set to `"dataproduct"` if missing |
| `info.id` | No | Yes | Generated as UUID v5 from `fullyQualifiedName` if missing |
| `info.owner` | Yes | No | Owner object must exist |
| **Owner Object** |
| `info.owner.id` | Yes | No | Owner identifier |
| **InterfaceComponents** |
| `interfaceComponents.outputPorts` | Yes* | No | Field must exist (list can be empty) |
| **Port Objects** (all types: inputPort, outputPort, discoveryPort, observabilityPort, controlPort) |
| `port.name` | Yes | No | Port name (must be unique within port type) |
| `port.version` | Yes | No | Must be valid semantic version |
| `port.entityType` | No | Yes | Auto-set based on port type (e.g., `"inputport"`, `"outputport"`) if missing |
| `port.fullyQualifiedName` | No | Yes | Generated from `info.fullyQualifiedName` + port type segment + name if missing |
| `port.id` | No | Yes | Generated as UUID v5 from `fullyQualifiedName` if missing |
| **ApplicationComponent** |
| `applicationComponent.name` | Yes | No | Component name (must be unique) |
| `applicationComponent.version` | Yes | No | Must be valid semantic version |
| `applicationComponent.entityType` | No | Yes | Auto-set to `"application"` if missing |
| `applicationComponent.fullyQualifiedName` | No | Yes | Generated from `info.fullyQualifiedName` + `"applications"` + name if missing |
| `applicationComponent.id` | No | Yes | Generated as UUID v5 from `fullyQualifiedName` if missing |
| **InfrastructuralComponent** |
| `infrastructuralComponent.name` | Yes | No | Component name (must be unique) |
| `infrastructuralComponent.version` | Yes | No | Must be valid semantic version |
| `infrastructuralComponent.entityType` | No | Yes | Auto-set to `"infrastructure"` if missing |
| `infrastructuralComponent.fullyQualifiedName` | No | Yes | Generated from `info.fullyQualifiedName` + `"infrastructure"` + name if missing |
| `infrastructuralComponent.id` | No | Yes | Generated as UUID v5 from `fullyQualifiedName` if missing |
| **StandardDefinition** (used in Promises, Expectations, Obligations, and Components) |
| `standardDefinition.name` | Yes | No | Standard definition name |
| `standardDefinition.version` | Yes | No | Must be valid semantic version |
| `standardDefinition.specification` | Yes | No | Specification identifier |
| `standardDefinition.definition` | Yes | No | Definition content (must contain `$href` or inline content) |
| `standardDefinition.entityType` | No | Yes | Auto-set to `"api"` or `"template"` based on context if missing |
| `standardDefinition.fullyQualifiedName` | No | Yes | Generated as `urn:dpds:{mesh-namespace}:{entity-type}s:{name}:{version}` if missing |
| `standardDefinition.id` | No | Yes | Generated as UUID v5 from `fullyQualifiedName` if missing |

## Additional validation rules

- **Semantic versioning**: The `dataProductDescriptor`, `info.version`, and all component `version` fields must follow semantic versioning specification (MAJOR.MINOR.PATCH[-PRERELEASE][+BUILD]) if present.

- **Entity type validation**: `entityType` values are validated against expected values. If an incorrect value is provided, an error is reported. If missing, the correct value is automatically set based on the component type and context.

- **ID generation**: All `id` fields are generated as UUID version 5 (SHA-1 hash) of the component's `fullyQualifiedName` when missing.

- **FQN generation formats**:
  - **Component FQN**: `{dataProductFqn}:{component-type-segment}:{component-name}`
    - Example: `urn:dpds:it.quantyca:dataproducts:tripExecution:1:inputports:orders`
  - **StandardDefinition FQN**: `urn:dpds:{mesh-namespace}:{entity-type}s:{name}:{version}`
    - Example: `urn:dpds:it.quantyca:apis:orders-api:1.0.0`

- **Uniqueness validation**: Component names must be unique within their scope (e.g., port names must be unique within each port type).

## Practical takeaway

Treat the root FQN, core `info`, owner, and component names/versions as author-owned. Let the Registry complete IDs, entity types, and nested FQNs when you omit them — but never omit `info.fullyQualifiedName`.
