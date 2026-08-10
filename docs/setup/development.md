# Development setup

Local developer setup for the Registry Server.

Related: [Configuration](configuration.md), [Deployment](deployment.md).

## Prerequisites

- **Java 21** (see `pom.xml` `java.version`)
- **Maven** 3.6 or higher (or the included `./mvnw` wrapper)
- **PostgreSQL** (for production-like local runs) or **H2** (in-memory, used by the `dev` profile)

## Clone and build

```bash
git clone https://github.com/opendatamesh-initiative/odm-platform-pp-registry-server.git
cd odm-platform-pp-registry-server
mvn clean install
```

## Running locally

```bash
# Default Spring Boot run (uses active profile from application.yml / overrides)
mvn spring-boot:run

# Run with the dev profile (H2 in-memory; see application-dev.yml)
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Profiles useful for local work

| Profile / file | Purpose |
|----------------|---------|
| `application-dev.yml` | H2 in-memory DB; local ports; notification + Policy V1 sample addresses |
| `application-localpostgres.yml` | Local PostgreSQL datasource; Policy inactive by default |
| `application-test.yml` (test resources) | Test profile schema and flags |

Datasource, schema, Notification, and Policy properties: [Configuration](configuration.md).

### External services for local lifecycle flows

Init/publish approval needs the Notification service when `odm.product-plane.notification-service.active=true`. Policy is optional — see [Policy service](../service/policy-service.md).

## Testing

```bash
# Run all tests
mvn test

# Run tests with coverage
mvn test jacoco:report
```

## Next steps

- Configuration (DB + product-plane): [Configuration](configuration.md)
- Container / production run: [Deployment](deployment.md)
- Service concepts: [docs index](../README.md)
