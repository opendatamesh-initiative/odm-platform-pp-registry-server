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