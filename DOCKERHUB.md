# ODM Platform Registry Server

The Registry Server is the product-plane registry of [Open Data Mesh Platform](https://github.com/opendatamesh-initiative) V2. It stores data products and their [DPDS](https://dpds.opendatamesh.org/) descriptors, validates descriptors when a version is published, and moves products and versions through an event-driven approval lifecycle (`PENDING` → `APPROVED` / `REJECTED`) together with the Notification service and, optionally, the Policy service.

- **Source code:** [opendatamesh-initiative/odm-platform-pp-registry-server](https://github.com/opendatamesh-initiative/odm-platform-pp-registry-server)
- **Documentation:** [docs](https://github.com/opendatamesh-initiative/odm-platform-pp-registry-server/blob/main/docs/README.md)
- **Issues:** [GitHub issues](https://github.com/opendatamesh-initiative/odm-platform-pp-registry-server/issues)

## Tags

Each [GitHub release](https://github.com/opendatamesh-initiative/odm-platform-pp-registry-server/releases) publishes one image tagged with its version, without the `v` prefix:

```bash
docker pull opendatamesh/odm-platform-registry:<version>
```

There is no `latest` tag: always pin a version. Tags before `2.0.0` are ODM Platform V1 images, built from [odm-platform](https://github.com/opendatamesh-initiative/odm-platform), and are configured differently.

## Requirements

| Dependency | Required | Notes |
| --- | --- | --- |
| PostgreSQL | Yes | Flyway creates and migrates the schema on startup. |
| [Notification service](https://hub.docker.com/r/opendatamesh/odm-platform-notification) | Yes, for the approval lifecycle | The Registry registers itself as an observer and receives events on `server.baseUrl`. |
| Policy service | No | When it is disabled, the Registry approves requests automatically. |

## Quick start

Starts the Registry against an existing PostgreSQL database, with Notification and Policy disabled. Use it to check that the container starts and connects to the database: without Notification, products and versions stay `PENDING`.

```bash
docker run --name odm-registry -p 8080:8080 \
  -e DB_JDBC_URL=jdbc:postgresql://db.example.com:5432/registry \
  -e DB_USERNAME=registry \
  -e DB_PASSWORD=change-me \
  -e ODM_PRODUCT_PLANE_NOTIFICATION_SERVICE_ACTIVE=false \
  -e ODM_PRODUCT_PLANE_NOTIFICATION_SERVICE_ADDRESS=http://localhost:8006 \
  -e ODM_PRODUCT_PLANE_POLICY_SERVICE_ACTIVE=false \
  opendatamesh/odm-platform-registry:<version>
```

The Notification address is required even when Notification is disabled; any value works then.

When it's up, the API is documented at `http://localhost:8080/swagger-ui.html` (OpenAPI at `/api-docs`).

## Production-like run

Spring Boot reads nested configuration from the `SPRING_APPLICATION_JSON` environment variable. This example enables Notification and Policy V1:

```bash
docker run --name odm-registry -p 8080:8080 \
  -e DB_JDBC_URL=jdbc:postgresql://db.example.com:5432/registry \
  -e DB_USERNAME=registry \
  -e DB_PASSWORD=change-me \
  -e SPRING_APPLICATION_JSON='{
    "server": { "baseUrl": "https://registry.example.com" },
    "odm": { "product-plane": {
      "notification-service": { "active": true, "address": "http://notification.example.com:8006" },
      "policy-service": { "active": true, "address": "http://policy.example.com:8005", "version": "1" }
    } }
  }' \
  opendatamesh/odm-platform-registry:<version>
```

`server.baseUrl` must be a URL the Notification service can reach: it's where the Registry receives events.

## Environment variables

### Image

| Variable | Description | Default |
| --- | --- | --- |
| `DB_JDBC_URL` | JDBC URL of the PostgreSQL database (`spring.datasource.url`). | none, required |
| `DB_USERNAME` | Database user (`spring.datasource.username`). | none, required |
| `DB_PASSWORD` | Database password (`spring.datasource.password`). | none, required |
| `PROFILES_ACTIVE` | Spring profiles to activate. The `docker` profile maps the three `DB_*` variables above. | `docker` |
| `JAVA_OPTS` | Extra JVM options, for example `-Xmx512m`. | empty |
| `SPRING_PROPS` | Extra arguments added to the `java` command line before `-jar`, such as `-Dserver.port=9000`. It does not accept JSON: use `SPRING_APPLICATION_JSON` for that. | empty |

### Application

Every property can be set with `SPRING_APPLICATION_JSON` or as an environment variable, using Spring Boot relaxed binding (for example, `odm.product-plane.notification-service.active` becomes `ODM_PRODUCT_PLANE_NOTIFICATION_SERVICE_ACTIVE`).

| Property | Description | Default |
| --- | --- | --- |
| `server.baseUrl` | Externally reachable URL of the Registry, used as the Notification observer callback. Required when Notification is active. | `http://localhost:8080` |
| `server.port` | HTTP port. | `8080` |
| `spring.jpa.properties.hibernate.default_schema` | Database schema used by Hibernate and Flyway. Lowercase only. | `odm_registry` |
| `odm.product-plane.notification-service.active` | Send and receive lifecycle events through the Notification service. When `true`, the Registry checks the connection on startup. | none, required |
| `odm.product-plane.notification-service.address` | Base URL of the Notification service. | none, required |
| `odm.product-plane.policy-service.active` | Use the Policy service for approvals (`true`) or approve automatically (`false`). | `false` |
| `odm.product-plane.policy-service.address` | Base URL of the Policy service. | none |
| `odm.product-plane.policy-service.version` | `1` to use the Policy Service V1 compatibility layer. | none |
| `odm.organization.name` | Organization name used to derive V1 identifiers from FQNs. Keep it stable if V1 clients rely on those ids. | `org.opendatamesh` |
| `registry.observer.name` | Observer name registered in Notification. Change it only when running several Registries. | `registry2.0` |

The full list is in [Configuration](https://github.com/opendatamesh-initiative/odm-platform-pp-registry-server/blob/main/docs/setup/configuration.md).

## Ports and volumes

- **Port:** `8080` (HTTP), or the value of `server.port`.
- **Volumes:** none. All state is in PostgreSQL.

## Learn more

- [Deployment guide](https://github.com/opendatamesh-initiative/odm-platform-pp-registry-server/blob/main/docs/setup/deployment.md)
- [Data product lifecycle](https://github.com/opendatamesh-initiative/odm-platform-pp-registry-server/blob/main/docs/service/data-product-lifecycle.md)
- [Events](https://github.com/opendatamesh-initiative/odm-platform-pp-registry-server/blob/main/docs/service/events.md) and [Policy service](https://github.com/opendatamesh-initiative/odm-platform-pp-registry-server/blob/main/docs/service/policy-service.md)
- [What's new in V2](https://github.com/opendatamesh-initiative/odm-platform-pp-registry-server/blob/main/docs/service/v2-whats-new.md)

Licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).
