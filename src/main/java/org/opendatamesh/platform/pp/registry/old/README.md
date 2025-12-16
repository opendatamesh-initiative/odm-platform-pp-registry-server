# Backward Compatibility Package (v1.x.x)

## Overview

This package contains features and implementations to ensure backward compatibility with the API and functionalities exposed by the Registry v1.x.x. It serves as a bridge between legacy clients and the current version of the Registry service.

## Purpose

As the Registry evolves, breaking changes may be introduced in newer versions. This package provides compatibility layers, adapters, and migration utilities to support clients that still rely on v1.x.x API contracts and behaviors.

## Package Structure

The `oldv1` package is organized to maintain clear separation between legacy compatibility code and the current implementation:

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

## Policy Service Package (policyservice)

The `org.opendatamesh.platform.pp.registry.old.v1.policyservice` package implements the integration with the **Policy Service V1**. 

### Responsibility

This package acts as a bridge between the Registry's lifecycle events and the external Policy Service V1. It enforces governance policies by validating Data Product creation and publication requests before they are finalized.

### Core Workflow

The integration follows an event-driven pattern:

1.  **Intercept**: The system listens for specific "Requested" notifications:
    -   `NotificationEventHandlerDpInitializationRequested`: Triggered when a new Data Product is created (but not yet active).
    -   `NotificationEventHandlerDpvPublicationRequested`: Triggered when a new Data Product Version is published.
2.  **Evaluate**: It constructs a validation request containing Data Product metadata and sends it to the configured Policy Service V1 via `PolicyClientV1`.
3.  **React**: Based on the Policy Service response (`VALID` or `INVALID`):
    -   **Approved**: Emits an approval event (e.g., `EventEmittedDataProductInitializationApproved`), allowing the process to complete.
    -   **Rejected**: Emits a rejection event (e.g., `EventEmittedDataProductInitializationRejected`), halting the process.

### Components

- **Event Handlers**: Classes prefixed with `NotificationEventHandler` that contain the logic to process specific registry events.
- **DTOs**: Classes like `PolicyResPolicyEvaluationRequest` and `PolicyResValidationResponse` that map the request/response structure of the Policy Service V1 API.
- **Client**: `PolicyClientV1Impl` encapsulates the REST calls to the Policy Service.


> [!NOTE] 
> This layer maintains strict backward compatibility with existing policies by using the **old Data Product Descriptor parser** (from `org.opendatamesh.dpds.parser` v1.x.x dependencies). This ensures that the JSON structure sent to the Policy Service (specifically the `dataProductVersion` field) mimics exactly what the Registry V1 produced, preserving side effects and field mappings relied upon by legacy OPA policies.

## Configuration

To enable the retro-compatibility mode with Policy Service V1, you need to configure the following properties in your `application.yml` (or `application-{profile}.yml`):

```yaml
odm:
  product-plane:
    policy-service:
      active: true
      version: 1 # This explicitly enables the V1 compatibility layer (PolicyServiceV1Configuration)
      address: http://my-policy-service:8001
```

### Properties Description

| Property | Description | Required | Value |
|----------|-------------|----------|-------|
| `odm.product-plane.policy-service.active` | Enables the Policy Service integration | Yes | `true` |
| `odm.product-plane.policy-service.version` | Specifies the Policy Service version. Set to `1` to activate the retro-compatibility package. | Yes | `1` |
| `odm.product-plane.policy-service.address` | The base URL of the Policy Service V1 instance. | Yes | e.g. `http://localhost:8001` |

When these properties are set, the application will load the `OldV1` configuration classes (`PolicyServiceV1Configuration`, `PolicyClientV1Config`) and disable the V2/current implementation if it's conditional on version not being 1 (or default).