# ODM Platform Registry Server

The Open Data Mesh Platform's Registry service server is a Spring Boot application that provides registry
functionality for the Open Data Mesh Platform.

<!-- TOC -->

* [ODM Platform Registry Server](#odm-platform-registry-server)
    * [Overview](#overview)
    * [Core Functionalities](#core-functionalities)
        * [Data Product Registry](#data-product-registry)
    * [Prerequisites](#prerequisites)
    * [Setup Instructions](#setup-instructions)
        * [1. Database Configuration](#1-database-configuration)
            * [PostgreSQL (Production)](#postgresql-production)
            * [H2 (Development)](#h2-development)
        * [2. Building the Project](#2-building-the-project)
        * [3. Running the Application](#3-running-the-application)
            * [Local Development](#local-development)
            * [Docker Deployment](#docker-deployment)
    * [Configuration Options](#configuration-options)
        * [Application Properties](#application-properties)
            * [Server Configuration](#server-configuration)
            * [Spring Configuration](#spring-configuration)
            * [Database Configuration](#database-configuration)
            * [Flyway Database Migration](#flyway-database-migration)
            * [ODM Platform Configuration](#odm-platform-configuration)
            * [Logging Configuration](#logging-configuration)
        * [Docker Spring JSON Configuration](#docker-spring-json-configuration)
        * [Environment Variables](#environment-variables)
    * [API Documentation](#api-documentation)
    * [Git Provider Authentication](#git-provider-authentication)
        * [Authentication Overview](#authentication-overview)
        * [Git Provider Authentication Table](#git-provider-authentication-table)
        * [How Authentication Works](#how-authentication-works)
        * [Endpoints Requiring Authentication](#endpoints-requiring-authentication)
    * [Testing](#testing)
    * [Contributing](#contributing)
    * [License](#license)
    * [Support](#support)
    * [Acknowledgments](#acknowledgments)

<!-- TOC -->

## Overview

This service is part of the Open Data Mesh Platform initiative, providing registry capabilities for data products and
their metadata management.

## Core Functionalities

### Data Product Registry

- Register and manage data product metadata
- Provide data product discovery and search capabilities
- Handle data product versioning and lifecycle management

## Prerequisites

- Java 11 or higher
- Maven 3.6 or higher
- PostgreSQL (for production) or H2 (for development)

## Setup Instructions

### 1. Database Configuration

#### PostgreSQL (Production)

```properties
# application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/registry
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

#### H2 (Development)

```properties
# application.properties
spring.datasource.url=jdbc:h2:mem:registry
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

### 2. Building the Project

```bash
# Clone the repository
git clone https://github.com/opendatamesh-initiative/odm-platform-pp-registry-server.git

# Navigate to project directory
cd odm-platform-pp-registry-server

# Build the project
mvn clean install
```

### 3. Running the Application

#### Local Development

```bash
# Run with default configuration
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### Docker Deployment

```bash
# Build the Docker image
docker build -t odm-registry-server .

# Run the container
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/registry \
  -e SPRING_DATASOURCE_USERNAME=your_username \
  -e SPRING_DATASOURCE_PASSWORD=your_password \
  odm-registry-server
```

## Configuration Options

### Application Properties

The application can be configured using the following properties in `application.yml` or `application.properties`:

#### Server Configuration

```yaml
server:
  port: 8080  # The port on which the application will run
```

#### Spring Configuration

```yaml
spring:
  application:
    name: odm-platform-pp-registry-server
  banner:
    charset: UTF-8
    mode: console
```

#### Database Configuration

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/registry  # Database connection URL
    username: your_username                         # Database username
    password: your_password                         # Database password
  jpa:
    properties:
      hibernate:
        default_schema: odm_registry                # Default schema for database tables
```

#### Flyway Database Migration

```yaml
spring:
  flyway:
    baselineOnMigrate: true                        # Allow baseline migrations
    locations: classpath:db/migration/postgresql   # Location of migration scripts
    schemas: public                                # Target schema for migrations
    validateOnMigrate: false                       # Skip validation during migration
    outOfOrder: true                               # Allow out-of-order migrations
```

#### ODM Platform Configuration

```yaml
odm:
  product-plane:
    notification-service:
      address: http://localhost:8006               # Notification service URL
      active: true                                 # Enable/disable notification service
```

#### Observer Configuration

The Registry service acts as an observer in the notification system, receiving events from the Notification service. The observer configuration is managed by `NotificationClientConfig`, which sets up the `NotificationClient` bean at application startup.

**How NotificationClientConfig Works:**

The `NotificationClientConfig` class is a Spring `@Configuration` class that:

1. **Reads configuration properties** from `application.yml`:
   - `server.baseUrl`: The base URL where the Registry service is accessible (used as the observer's callback URL)
   - `server.observer.name`: A unique identifier for this observer instance
   - `server.observer.displayName`: A human-readable name for the observer
   - `server.observer.event-types`: List of event types to subscribe to
   - `server.observer.policy-event-types`: List of policy-related event types (used when Policy service is unavailable)
   - `odm.product-plane.notification-service.address`: The Notification service URL
   - `odm.product-plane.notification-service.active`: Whether the notification service is enabled

2. **Creates the NotificationClient bean**:
   - If `odm.product-plane.notification-service.active` is `true`, it creates a `NotificationClientImpl` instance
   - The client connects to the Notification service and subscribes to the configured events
   - If the notification service is inactive, it returns a no-op implementation that logs warnings

3. **Subscribes to events**:
   - At startup, the client calls the Notification service's subscription endpoint
   - The subscription request includes the observer's base URL, name, display name, API version (always V2), and the list of event types to subscribe to
   - The Notification service will forward matching events to the Registry's `/api/v2/up/observer/notifications` endpoint

**Configuration Properties:**

```yaml
server:
  baseUrl: http://localhost:8080                    # Base URL of the Registry service (observer callback URL)
  observer:
    name: registry2.0                                # Unique identifier for this observer
    displayName: Registry service 2.0                # Human-readable observer name
    event-types:                                     # List of event types to subscribe to
      - DATA_PRODUCT_INITIALIZATION_APPROVED
      - DATA_PRODUCT_INITIALIZATION_REJECTED
      - DATA_PRODUCT_VERSION_INITIALIZATION_APPROVED
      - DATA_PRODUCT_VERSION_INITIALIZATION_REJECTED
    policy-event-types:                              # Policy-related events (used when Policy service is unavailable)
      - DATA_PRODUCT_INITIALIZATION_REQUESTED
      - DATA_PRODUCT_VERSION_PUBLICATION_REQUESTED
```

**Property Descriptions:**

| Property | Description | Required |
|----------|-------------|----------|
| `server.baseUrl` | The base URL where the Registry service is accessible. This is used as the observer's callback URL, so the Notification service knows where to send events. | Yes |
| `server.observer.name` | A unique identifier for this observer instance. Used to identify the Registry service in the Notification service's subscription registry. | Yes |
| `server.observer.displayName` | A human-readable name for the observer. Used for display purposes in the Notification service. | Yes |
| `server.observer.event-types` | A list of event type names that the Registry service wants to receive. These are the standard events that the Registry subscribes to (approval/rejection events from external services like the Policy service). | Yes |
| `server.observer.policy-event-types` | A list of policy-related event types. These events are emitted by the Registry itself and are subscribed to when the Policy service is unavailable, allowing the Registry to auto-approve requests without external validation. | Optional |

**How It Works:**

1. At application startup, `NotificationClientConfig` reads the observer configuration from `application.yml`
2. It extracts the event type lists using Spring's `Environment` API (to handle YAML list syntax)
3. If the notification service is active, it creates a `NotificationClientImpl` instance with:
   - The observer's base URL, name, and display name
   - The Notification service URL
4. The client performs a health check on the Notification service
5. If successful, it sends a subscription request containing:
   - Observer metadata (base URL, name, display name, API version)
   - The list of event types to subscribe to
6. The Notification service registers this subscription and will forward matching events to the Registry's observer endpoint (`/api/v2/up/observer/notifications`)
7. When events arrive, the `ObserverController` receives them and the `ObserverService` dispatches them to the appropriate use case handlers

**Note:** The `policy-event-types` are currently configured but not yet fully implemented (see TODO in `NotificationClientImpl.createSubscribeRequest`). They are intended to allow the Registry to subscribe to its own emitted events when the Policy service is unavailable, enabling auto-approval workflows.

#### Logging Configuration

```yaml
logging:
  pattern:
    console: "%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} %clr(%5p) %clr(${PID:- }){magenta} %clr(---){faint} %clr([%15.15t]){faint} %clr(%-40.40logger{39}){cyan} %clr(:){faint} %m%n%wEx"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
  level:
    org.springframework.web.filter.CommonsRequestLoggingFilter: DEBUG
```

These properties can be overridden using environment variables or command-line arguments when running the application.

### Docker Spring JSON Configuration

When running the application in Docker, you can pass the Spring configuration as a JSON string using the `SPRING_PROPS`
environment variable. Here's an example:

```bash
docker run -p 8080:8080 \
  -e SPRING_PROPS='{"spring":{"datasource":{"url":"jdbc:postgresql://db:5432/registry","username":"your_username","password":"your_password"},"jpa":{"properties":{"hibernate":{"default_schema":"odm_registry"}}}},"odm":{"product-plane":{"notification-service":{"address":"http://notification-service:8006","active":true}}}}' \
  odm-registry-server
```

The JSON structure follows the same hierarchy as the YAML configuration:

```json
{
  "spring": {
    "datasource": {
      "url": "jdbc:postgresql://db:5432/registry",
      "username": "your_username",
      "password": "your_password"
    },
    "jpa": {
      "properties": {
        "hibernate": {
          "default_schema": "odm_registry"
        }
      }
    }
  },
  "odm": {
    "product-plane": {
      "notification-service": {
        "address": "http://notification-service:8006",
        "active": true
      }
    }
  }
}
```

This approach is particularly useful when you need to configure multiple properties at once in a Docker environment, as
it allows you to pass all configuration in a single environment variable.

### Environment Variables

The application can be configured using environment variables:

- `SPRING_PROFILES_ACTIVE`: Set active profile (dev, prod)
- `SPRING_DATASOURCE_URL`: Database connection URL
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password
- `SERVER_PORT`: Application port
- `SERVER_SERVLET_CONTEXT_PATH`: API context path

## API Documentation

Once the application is running, you can access:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI Specification: `http://localhost:8080/api-docs`

## Events

The Registry service integrates with the Notification service to both receive and emit events. This enables event-driven workflows for data product lifecycle management.

### Overview

At startup, the Registry service attempts to connect to the Notification service using the configuration property `odm.product-plane.notification-service.address`. If the Notification service is not available and `odm.product-plane.notification-service.active` is set to `true`, the application will fail to start. If the Notification service is available, the Registry service automatically subscribes to receive specific events and can emit events to notify other services about registry operations.

### Subscribed Events

The Registry service subscribes to the following events from the Notification service. These events are received via the `/api/v2/up/observer/notifications` endpoint and trigger corresponding use cases:

| Event Type | When Received | Action Triggered | Additional Notes |
|------------|---------------|------------------|------------------|
| `DATA_PRODUCT_INITIALIZATION_APPROVED` | When a data product approval decision is made by an external service (e.g., Policy service) | Triggers `approveDataProduct` use case | After approval, emits `DATA_PRODUCT_INITIALIZED` event |
| `DATA_PRODUCT_INITIALIZATION_REJECTED` | When a data product rejection decision is made by an external service | Triggers `rejectDataProduct` use case | Updates data product status to `REJECTED` |
| `DATA_PRODUCT_VERSION_INITIALIZATION_APPROVED` | When a data product version approval decision is made by an external service | Triggers `approveDataProductVersion` use case | After approval, emits `DATA_PRODUCT_VERSION_PUBLISHED` event |
| `DATA_PRODUCT_VERSION_INITIALIZATION_REJECTED` | When a data product version rejection decision is made by an external service | Triggers `rejectDataProductVersion` use case | Updates data product version status to `REJECTED` |

**Note:** If the Policy service is not available during startup, the Registry service may also subscribe to `DATA_PRODUCT_INITIALIZATION_REQUESTED` and `DATA_PRODUCT_VERSION_PUBLICATION_REQUESTED` events (emitted by the Registry itself) to bypass validation that would normally be performed by the Policy service. This allows the Registry service to work correctly even when the Policy service is unavailable. However, the Registry service requires the Notification service to be available to function properly.

### Emitted Events

The Registry service emits the following events to notify other services about operations performed in the registry:

| Event Type | When Emitted | Use Case | Event Content Structure |
|------------|---------------|----------|------------------------|
| `DATA_PRODUCT_INITIALIZATION_REQUESTED` | When a new data product is created | `DataProductInitializer` | `{ "dataProduct": DataProductRes }` |
| `DATA_PRODUCT_INITIALIZED` | When a data product status changes from `PENDING` to `APPROVED` | `DataProductApprover` | `{ "dataProduct": DataProductRes }` |
| `DATA_PRODUCT_DELETED` | When a data product is deleted | `DataProductDeleter` | `{ "dataProductUuid": string, "dataProductFqn": string }` |
| `DATA_PRODUCT_VERSION_PUBLICATION_REQUESTED` | When a new data product version is published | `DataProductVersionPublisher` | `{ "dataProductVersion": DataProductVersionRes }` |
| `DATA_PRODUCT_VERSION_PUBLISHED` | When a data product version status changes from `PENDING` to `APPROVED` | `DataProductVersionApprover` | `{ "dataProductVersion": DataProductVersionRes }` |
| `DATA_PRODUCT_VERSION_DELETED` | When a data product version is deleted | `DataProductVersionDeleter` | `{ "dataProductVersionUuid": string, "dataProductFqn": string, "dataProductVersionTag": string }` |

### Event Structure

All events follow a standardized structure. The generic format of an event is:

```json
{
  "event": {
    "resourceType": "DATA_PRODUCT" | "DATA_PRODUCT_VERSION",
    "resourceIdentifier": "<uuid>",
    "type": "<EVENT_TYPE>",
    "eventTypeVersion": "1.0.0",
    "eventContent": {
      // Event-specific content (see details below)
    }
  }
}
```

Where:
- `resourceType`: The type of resource the event relates to (`DATA_PRODUCT` or `DATA_PRODUCT_VERSION`)
- `resourceIdentifier`: The UUID of the resource
- `type`: The event type (e.g., `DATA_PRODUCT_INITIALIZATION_REQUESTED`)
- `eventTypeVersion`: The version of the event schema (currently `1.0.0`)
- `eventContent`: Event-specific payload containing additional information

### Detailed Event Specifications

#### Data Product Events

##### DATA_PRODUCT_INITIALIZATION_REQUESTED

Emitted when a new data product is created. This event is sent to the Notification service, which may forward it to the Policy service for validation.

**Event Content:**
```json
{
  "event": {
    "resourceType": "DATA_PRODUCT",
    "resourceIdentifier": "<data-product-uuid>",
    "type": "DATA_PRODUCT_INITIALIZATION_REQUESTED",
    "eventTypeVersion": "1.0.0",
    "eventContent": {
      "dataProduct": {
        // Complete DataProductRes object
      }
    }
  }
}
```

##### DATA_PRODUCT_INITIALIZED

Emitted when a data product is approved (status transitions from `PENDING` to `APPROVED`). This occurs after the Registry receives a `DATA_PRODUCT_INITIALIZATION_APPROVED` event and successfully processes the approval.

**Event Content:**
```json
{
  "event": {
    "resourceType": "DATA_PRODUCT",
    "resourceIdentifier": "<data-product-uuid>",
    "type": "DATA_PRODUCT_INITIALIZED",
    "eventTypeVersion": "1.0.0",
    "eventContent": {
      "dataProduct": {
        // Complete DataProductRes object with updated status
      }
    }
  }
}
```

##### DATA_PRODUCT_DELETED

Emitted when a data product is deleted from the registry.

**Event Content:**
```json
{
  "event": {
    "resourceType": "DATA_PRODUCT",
    "resourceIdentifier": "<data-product-uuid>",
    "type": "DATA_PRODUCT_DELETED",
    "eventTypeVersion": "1.0.0",
    "eventContent": {
      "dataProductUuid": "<data-product-uuid>",
      "dataProductFqn": "<fully-qualified-name>"
    }
  }
}
```

#### Data Product Version Events

##### DATA_PRODUCT_VERSION_PUBLICATION_REQUESTED

Emitted when a new data product version is published. This event is sent to the Notification service, which may forward it to the Policy service for validation.

**Event Content:**
```json
{
  "event": {
    "resourceType": "DATA_PRODUCT_VERSION",
    "resourceIdentifier": "<data-product-version-uuid>",
    "type": "DATA_PRODUCT_VERSION_PUBLICATION_REQUESTED",
    "eventTypeVersion": "1.0.0",
    "eventContent": {
      "dataProductVersion": {
        // Complete DataProductVersionRes object
      }
    }
  }
}
```

##### DATA_PRODUCT_VERSION_PUBLISHED

Emitted when a data product version is approved (status transitions from `PENDING` to `APPROVED`). This occurs after the Registry receives a `DATA_PRODUCT_VERSION_INITIALIZATION_APPROVED` event and successfully processes the approval.

**Event Content:**
```json
{
  "event": {
    "resourceType": "DATA_PRODUCT_VERSION",
    "resourceIdentifier": "<data-product-version-uuid>",
    "type": "DATA_PRODUCT_VERSION_PUBLISHED",
    "eventTypeVersion": "1.0.0",
    "eventContent": {
      "dataProductVersion": {
        // Complete DataProductVersionRes object with updated status
      }
    }
  }
}
```

##### DATA_PRODUCT_VERSION_DELETED

Emitted when a data product version is deleted from the registry.

**Event Content:**
```json
{
  "event": {
    "resourceType": "DATA_PRODUCT_VERSION",
    "resourceIdentifier": "<data-product-version-uuid>",
    "type": "DATA_PRODUCT_VERSION_DELETED",
    "eventTypeVersion": "1.0.0",
    "eventContent": {
      "dataProductVersionUuid": "<data-product-version-uuid>",
      "dataProductFqn": "<fully-qualified-name>",
      "dataProductVersionTag": "<version-tag>"
    }
  }
}
```

## Git Provider Authentication

The registry server integrates with multiple Git providers (GitHub, GitLab, Bitbucket, Azure DevOps) to manage data
product repositories and descriptors. All Git provider operations require authentication via HTTP headers.

### Authentication Overview

The registry server uses a standardized authentication mechanism across all supported Git providers. Authentication
credentials are passed via HTTP headers in API requests that interact with Git providers.

### Git Provider Authentication Table

| Git Provider | Auth Method | Required Headers                                                                                                                        |
|--------------|-------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| GitHub       | PAT         | `x-odm-gpauth-type`: `"PAT"`<br>`x-odm-gpauth-param-token`: GitHub Personal Access Token                                                |
| GitLab       | PAT         | `x-odm-gpauth-type`: `"PAT"`<br>`x-odm-gpauth-param-token`: GitLab Personal Access Token                                                |
| Azure DevOps | PAT         | `x-odm-gpauth-type`: `"PAT"`<br>`x-odm-gpauth-param-token`: Azure DevOps Personal Access Token                                          |
| Bitbucket    | PAT         | `x-odm-gpauth-type`: `"PAT"`<br>`x-odm-gpauth-param-token`: Bitbucket App Password<br>`x-odm-gpauth-param-username`: Bitbucket Username |

### How Authentication Works

1. When making API requests to endpoints that interact with Git providers, include the appropriate authentication
   headers in your HTTP request.
2. The registry server extracts these headers and creates provider-specific credentials.
3. These credentials are used to authenticate with the Git provider's API and perform Git operations (clone, push,
   fetch, etc.).

### Endpoints Requiring Authentication

The following endpoints require Git provider authentication headers:

- Data Product Descriptor operations (`GET`, `POST`, `PUT /api/v2/pp/registry/products/{uuid}/descriptor`)
- Repository operations (`GET /api/v2/pp/registry/products/{uuid}/repository/commits`, `/branches`, `/tags`)
- Git Provider operations (`GET /api/v2/pp/registry/git-providers/organizations`, `/repositories`, etc.)

For detailed information about creating Personal Access Tokens for each provider, refer to their respective
documentation:

- [GitHub Personal Access Tokens](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token)
- [GitLab Personal Access Tokens](https://docs.gitlab.com/ee/user/profile/personal_access_tokens.html)
- [Azure DevOps Personal Access Tokens](https://learn.microsoft.com/en-us/azure/devops/organizations/accounts/use-personal-access-tokens-to-authenticate)
- [Bitbucket App Passwords](https://support.atlassian.com/bitbucket-cloud/docs/app-passwords/)

## Testing

Run the test suite to verify everything is working correctly:

```bash
# Run all tests
mvn test

# Run tests with coverage
mvn test jacoco:report
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Support

For support, please open an issue in the GitHub repository.

## Acknowledgments

- Open Data Mesh Initiative
- All contributors to this project