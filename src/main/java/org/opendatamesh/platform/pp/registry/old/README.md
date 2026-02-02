# Backward Compatibility Package (v1.x.x)

## Overview

This package contains features and implementations to ensure backward compatibility with the API and functionalities
exposed by the Registry v1.x.x. It serves as a bridge between legacy clients and the current version of the Registry
service.

## Purpose

As the Registry evolves, breaking changes may be introduced in newer versions. This package provides compatibility
layers, adapters, and migration utilities to support clients that still rely on v1.x.x API contracts and behaviors.

## Package Structure

The `oldv1` package is organized to maintain clear separation between legacy compatibility code and the current
implementation:

- **Controllers**: REST endpoints that mirror v1.x.x API contracts
- **Mappers**: Conversion utilities between v1.x.x and current data models
- **Services**: Compatibility service implementations for v1.x.x workflows
- **Resources**: DTOs and response models matching v1.x.x specifications

## Dependency Rules

### ✅ Allowed Dependencies

**This package CAN depend on code outside this package.**

The backward compatibility layer may import and use:

- Core services and repositories
- Current data models and entities
- Utility classes and shared components
- Infrastructure components

### ❌ Restricted Dependencies

**Code outside this package CAN NOT depend on code inside this package.**

To maintain clean architecture and prevent coupling:

- Core packages must not import from `oldv1`
- New features should not depend on legacy compatibility code
- The main codebase should remain independent of v1.x.x implementations

## Development Guidelines

When adding backward compatibility features:

1. **Isolation**: Keep all v1.x.x compatibility code within this package
2. **Delegation**: Use existing core services rather than duplicating logic
3. **Mapping**: Create clear mapping layers between v1.x.x and current models
4. **Documentation**: Document any differences or limitations in compatibility
5. **Testing**: Include tests that verify v1.x.x API contract compliance

## Migration Path

This package is intended to be temporary. Over time:

- Clients should migrate to the current API version
- Deprecated endpoints should be clearly marked
- Removal timelines should be communicated to stakeholders

## Related Documentation

- [Main README](../../../../../../README.md) - Overall project documentation
- API Documentation - Current API specifications
- Migration Guide - Steps for migrating from v1.x.x to current version

## Registry Service Package (registryservice)

The `org.opendatamesh.platform.pp.registry.old.v1.registryservice` package implements REST endpoints that provide
backward compatibility with the **Registry V1 API**. These endpoints allow legacy clients to interact with the Registry
using the v1.x.x API contracts.

### Endpoints

The `RegistryV1Controller` exposes the following endpoints under the base path `/api/v1/pp/registry`:

| Method | Endpoint                                                                 | Description                                                                                          | Parameters                                                                                                                                                                                                                                               | Response                                                                                                                                                                     | Use Cases                                                                                                                                                                                                                                                                      |
|--------|--------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `GET`  | `/api/v1/pp/registry/products/{id}/versions/{version}`                   | Retrieves a Data Product Version descriptor in the v1.x.x format with variable placeholders resolved | **Path:**<br/>- `id`: Data Product identifier (supports both old parser ID generated from FQN and database UUID)<br/>- `version`: Version number<br/><br/>**Query (optional):**<br/>- `format`: Serialization format (`canonical` default, `normalized`) | JSON string containing the Data Product Version descriptor with all `${variableName}` placeholders replaced by their actual values                                           | • **Legacy Client Integration**: Retrieve descriptors without migrating to newer API versions<br/>• **Variable Resolution**: Get fully resolved descriptors with variables substituted<br/>• **Format Compatibility**: Support canonical/normalized formats for legacy clients |
| `GET`  | `/api/v1/pp/registry/products/{id}/versions/{version}/variables`         | Retrieves all descriptor variables for a specific Data Product Version                               | **Path:**<br/>- `id`: Data Product identifier (supports both old parser ID generated from FQN and database UUID)<br/>- `version`: Version number                                                                                                         | List of `RegistryV1VariableResource` objects with:<br/>- `id`: Variable sequence identifier<br/>- `variableName`: Variable key<br/>- `variableValue`: Current variable value | • **Variable Discovery**: Discover all variables referenced in a descriptor<br/>• **Variable Management**: View all customizable variables<br/>• **Auto-Initialization**: Automatically creates missing variables from descriptor placeholders with default values             |
| `PUT`  | `/api/v1/pp/registry/products/{id}/versions/{version}/variables/{varId}` | Updates the value of a specific descriptor variable                                                  | **Path:**<br/>- `id`: Data Product identifier (supports both old parser ID generated from FQN and database UUID)<br/>- `version`: Version number<br/>- `varId`: Variable sequence identifier<br/><br/>**Query:**<br/>- `value`: New value to assign      | Updated `RegistryV1VariableResource` object                                                                                                                                  | • **Variable Configuration**: Set custom values (environment URLs, credentials, config params)<br/>• **Dynamic Descriptor Resolution**: Customize descriptors at runtime<br/>• **Environment-Specific Values**: Different values per environment (dev/staging/prod)            |

**Technical Details**:

- All endpoints use the **old DPDSParser** (v1.x.x) to ensure exact compatibility with v1.x.x output format
- **Product ID Lookup**: The `id` path parameter supports dual lookup:
    - **Old Parser ID**: ID generated from the Data Product's FQN (Fully Qualified Name) using the old parser's
      `IdentifierStrategy`. This allows legacy clients using v1.x.x IDs to continue working without migration.
    - **Database UUID**: The actual UUID stored in the database for the Data Product. This enables newer clients to use
      the standard UUID format.
    - The lookup performs a case-insensitive comparison against both formats, ensuring compatibility with both legacy
      and current systems.
- Variable placeholders (`${variableName}`) are extracted from descriptors and can be managed via the variables
  endpoints
- Variable values are properly escaped when used in descriptor serialization to ensure valid JSON output
- The Get Variables endpoint automatically creates missing variables referenced in descriptors but not yet stored in the
  database

> [!NOTE]
> This layer maintains strict backward compatibility by using the **old Data Product Descriptor parser** (from
`org.opendatamesh.dpds.parser` v1.x.x dependencies). This ensures that the JSON structure and serialization format
> exactly match what the Registry V1 produced, preserving compatibility with legacy clients and tools.

### Configuration

The Registry V1 endpoints use an `IdentifierStrategy` to generate IDs from Data Product FQNs (Fully Qualified Names) for
backward compatibility. This strategy is configured via the `odm.organization.name` property.

To configure the IdentifierStrategy, add the following property to your `application.yml` (or
`application-{profile}.yml`):

```yaml
odm:
  organization:
    name: org.opendatamesh  # Default value if not specified
```

#### Properties Description

| Property                | Description                                                                                                                                                                        | Required | Default Value      |
|-------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|--------------------|
| `odm.organization.name` | Organization name used by the IdentifierStrategy to generate IDs from Data Product FQNs. This ensures that IDs generated by the old parser match those expected by legacy clients. | No       | `org.opendatamesh` |

**Important Notes**:

- The `IdentifierStrategy` uses this organization name to generate consistent IDs from FQNs, matching the behavior of
  Registry V1.
- If you're migrating from Registry V1, ensure this value matches the organization name used in your previous
  installation to maintain ID compatibility.
- Changing this value will result in different IDs being generated for the same FQNs, which may break compatibility with
  existing legacy clients.

## Policy Service Package (policyservice)

The `org.opendatamesh.platform.pp.registry.old.v1.policyservice` package implements the integration with the **Policy
Service V1**.

### Responsibility

This package acts as a bridge between the Registry's lifecycle events and the external Policy Service V1. It enforces
governance policies by validating Data Product creation and publication requests before they are finalized.

### Core Workflow

The integration follows an event-driven pattern:

1. **Intercept**: The system listens for specific "Requested" notifications:
    - `NotificationEventHandlerDpInitializationRequested`: Triggered when a new Data Product is created (but not yet
      active).
    - `NotificationEventHandlerDpvPublicationRequested`: Triggered when a new Data Product Version is published.
2. **Evaluate**: It constructs a validation request containing Data Product metadata and sends it to the configured
   Policy Service V1 via `PolicyClientV1`.
3. **React**: Based on the Policy Service response (`VALID` or `INVALID`):
    - **Approved**: Emits an approval event (e.g., `EventEmittedDataProductInitializationApproved`), allowing the
      process to complete.
    - **Rejected**: Emits a rejection event (e.g., `EventEmittedDataProductInitializationRejected`), halting the
      process.

### Components

- **Event Handlers**: Classes prefixed with `NotificationEventHandler` that contain the logic to process specific
  registry events.
- **DTOs**: Classes like `PolicyResPolicyEvaluationRequest` and `PolicyResValidationResponse` that map the
  request/response structure of the Policy Service V1 API.
- **Client**: `PolicyClientV1Impl` encapsulates the REST calls to the Policy Service.

> [!NOTE]
> This layer maintains strict backward compatibility with existing policies by using the **old Data Product Descriptor
parser** (from `org.opendatamesh.dpds.parser` v1.x.x dependencies). This ensures that the JSON structure sent to the
> Policy Service (specifically the `dataProductVersion` field) mimics exactly what the Registry V1 produced, preserving
> side effects and field mappings relied upon by legacy OPA policies.

### Configuration

To enable the retro-compatibility mode with Policy Service V1, you need to configure the following properties in your
`application.yml` (or `application-{profile}.yml`):

```yaml
odm:
  product-plane:
    policy-service:
      active: true
      version: 1 # This explicitly enables the V1 compatibility layer (PolicyServiceV1Configuration)
      address: http://my-policy-service:8001
```

### Properties Description

| Property                                                     | Description                                                                                                              | Required | Value                        |
|--------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------|----------|------------------------------|
| `odm.product-plane.policy-service.active`                    | Enables the Policy Service integration                                                                                   | Yes      | `true`                       |
| `odm.product-plane.policy-service.version`                   | Specifies the Policy Service version. Set to `1` to activate the retro-compatibility package.                            | Yes      | `1`                          |
| `odm.product-plane.policy-service.address`                   | The base URL of the Policy Service V1 instance.                                                                          | Yes      | e.g. `http://localhost:8001` |
| `odm.product-plane.policy-service.descriptor.parser.version` | Descriptor parser used during policy validation: `1` = old 1.x parser (transforms content), `2` = pass descriptor as-is. | No       | `1` (default)                |

When these properties are set, the application will load the `OldV1` configuration classes (
`PolicyServiceV1Configuration`, `PolicyClientV1Config`) and disable the V2/current implementation if it's conditional on
version not being 1 (or default).