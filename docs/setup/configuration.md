# Configuration

Properties you **must** or **should** set for the Registry Server, including database and product-plane integration. Framework defaults (Flyway script locations, Hibernate `ddl-auto`, banners, logging patterns, and similar) are left to the application YAML and are not listed here.

Related: [Development](development.md) · [Deployment](deployment.md) · [Events](../service/events.md) · [Policy service](../service/policy-service.md) · [V1 backward compatibility](../service/v1-backward-compatibility.md)

## Minimal production-like configuration

Complete minimal example for Cloud Run / containers (Notification + Policy V1). Change URLs and credentials for your environment.

```yaml
server:
  port: 8080
  baseUrl: https://registry.example.com # required: observer callback base URL

spring:
  datasource:
    url: jdbc:postgresql://db:5432/registry
    username: your_username
    password: your_password
  jpa:
    properties:
      hibernate:
        default_schema: odm_registry # only schema setting to configure (Flyway uses this)

odm:
  product-plane:
    notification-service:
      address: http://notification-service:8006
      active: true
    policy-service:
      active: true
      address: http://policy-service:8005
      version: '1' # Policy Service V1 compatibility layer
```

When Policy is **inactive**, set `odm.product-plane.policy-service.active: false` and omit `address` / `version` (Registry auto-approves `*_REQUESTED`). Keep Notification active for the event loop.

### Equivalent `SPRING_PROPS` (containers)

```bash
docker run -p 8080:8080 \
  -e SPRING_PROPS='{"server":{"port":8080,"baseUrl":"https://registry.example.com"},"spring":{"datasource":{"url":"jdbc:postgresql://db:5432/registry","username":"your_username","password":"your_password"},"jpa":{"properties":{"hibernate":{"default_schema":"odm_registry"}}}},"odm":{"product-plane":{"notification-service":{"address":"http://notification-service:8006","active":true},"policy-service":{"active":true,"address":"http://policy-service:8005","version":"1"}}}}' \
  odm-registry-server
```

---

## Server

| Property         | Purpose                                                                      | Default                                 | Manage when                                                                  |
| ---------------- | ---------------------------------------------------------------------------- | --------------------------------------- | ---------------------------------------------------------------------------- |
| `server.baseUrl` | Externally reachable base URL used as the Notification **observer callback** | `http://localhost:8080` in base YAML    | **Must** set to a URL Notification can reach whenever Notification is active |
| `server.port`    | HTTP listen port                                                             | `8080` (some local profiles use `8086`) | **Should** set if you do not use the default                                 |

```yaml
server:
  port: 8080
  baseUrl: https://registry.example.com
```

## Database

Production uses **PostgreSQL**. Local `dev` can use **H2** (in-memory, PostgreSQL compatibility mode) via the profile — see [Development](development.md).

| Property                                         | Purpose                             | Default          | Manage when                                |
| ------------------------------------------------ | ----------------------------------- | ---------------- | ------------------------------------------ |
| `spring.datasource.url`                          | JDBC URL                            | profile-specific | **Must** for any real database             |
| `spring.datasource.username`                     | DB user                             | profile-specific | **Must**                                   |
| `spring.datasource.password`                     | DB password                         | profile-specific | **Must** (may be empty for H2)             |
| `spring.jpa.properties.hibernate.default_schema` | Schema for Hibernate **and** Flyway | `odm_registry`   | **Must** set intentionally; lowercase only |

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/registry
    username: your_username
    password: your_password
  jpa:
    properties:
      hibernate:
        default_schema: odm_registry
```

| Environment                 | Engine       | Schema                                         |
| --------------------------- | ------------ | ---------------------------------------------- |
| Local `dev`                 | H2 in-memory | `odm_registry`                                 |
| Local Postgres / production | PostgreSQL   | `odm_registry` (or your agreed lowercase name) |

## Notification service

Required for the event-driven init/publish approval loop. Behavior: [Events](../service/events.md).

| Property                                         | Purpose                                                       | Manage when                 |
| ------------------------------------------------ | ------------------------------------------------------------- | --------------------------- |
| `odm.product-plane.notification-service.active`  | Enable Notification client (`true`) or no-op client (`false`) | **Must** set intentionally  |
| `odm.product-plane.notification-service.address` | Notification service base URL                                 | **Must** when `active=true` |

```yaml
odm:
  product-plane:
    notification-service:
      active: true
      address: http://notification-service:8006
```

## Policy service

Optional governance gate. When inactive, the Registry auto-approves `*_REQUESTED`. Behavior: [Policy service](../service/policy-service.md).

| Property                                                     | Purpose                                                                                  | Manage when                                                        |
| ------------------------------------------------------------ | ---------------------------------------------------------------------------------------- | ------------------------------------------------------------------ |
| `odm.product-plane.policy-service.active`                    | Use external Policy (`true`) or auto-approve (`false`)                                   | **Must** set intentionally                                         |
| `odm.product-plane.policy-service.address`                   | Policy service base URL                                                                  | **Must** when active with Policy V1                                |
| `odm.product-plane.policy-service.version`                   | `1` loads the Policy V1 compatibility layer                                              | **Must** when using Policy V1                                      |
| `odm.product-plane.policy-service.descriptor.parser.version` | `1` = old parser, `2` = pass-through when sending descriptors to Policy V1 (default `2`) | **Should** only if you need V1-shaped payloads for legacy policies |

```yaml
odm:
  product-plane:
    policy-service:
      active: true
      address: http://policy-service:8005
      version: '1'
```

## V1 compatibility (optional)

Only if you still serve `/api/v1` clients or care about legacy FQN→id mapping. Details: [V1 backward compatibility](../service/v1-backward-compatibility.md).

| Property                                                          | Purpose                                                     | Manage when                                                                             |
| ----------------------------------------------------------------- | ----------------------------------------------------------- | --------------------------------------------------------------------------------------- |
| `odm.organization.name`                                           | Organization name for V1 `IdentifierStrategy` (FQN → id)    | **Should** keep stable (default `org.opendatamesh`) if legacy clients rely on those ids |
| `odm.product-plane.registry-service.v1.descriptor.parser.version` | Parser on Registry API v1 paths (`1` = old parser, default) | **Should** only if you need to change V1 descriptor shaping                             |

## Observer identity (optional)

Used when registering with Notification. Defaults are fine for a single Registry; override if you run multiple observers.

| Property                        | Purpose                              | Default                |
| ------------------------------- | ------------------------------------ | ---------------------- |
| `registry.observer.name`        | Unique observer name in Notification | `registry2.0`          |
| `registry.observer.displayName` | Human-readable name                  | `Registry service 2.0` |

`server.baseUrl` is still the callback address (see [Server](#server)).

## How to pass configuration

| Mechanism                                                | Typical use                                                                                                        |
| -------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------ |
| Profile YAML (`application-*.yml`)                       | Local development                                                                                                  |
| Env vars (`SPRING_DATASOURCE_URL`, `SERVER_BASE_URL`, …) | Simple overrides; Spring relaxed binding applies (e.g. `ODM_PRODUCT_PLANE_NOTIFICATION_SERVICE_ACTIVE`)            |
| `SPRING_PROPS` JSON                                      | Containers / Cloud Run — nested config in one blob (see [minimal example](#minimal-production-like-configuration)) |
| `DB_JDBC_URL` / `DB_USERNAME` / `DB_PASSWORD`            | Datasource when the `docker` profile is active                                                                     |
