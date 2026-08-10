# Deployment

How to deploy the Registry Server as a container (recommended for production and shared environments).

Related: [Configuration](configuration.md) · [Development](development.md) · [Events](../service/events.md) · [Policy service](../service/policy-service.md)

## What you need

| Dependency | Required? | Notes |
|------------|-----------|-------|
| **PostgreSQL** | Yes | Production database. Set URL, credentials, and schema — see [Configuration](configuration.md#database) |
| **Notification service** | Yes for event-driven lifecycle | Init/publish approval loop. Set `active` + `address` |
| **Policy service** | Optional | When inactive, Registry auto-approves `*_REQUESTED` |

Also set **`server.baseUrl`** to the URL that other services (especially Notification) can call back on for observer delivery.

The container image ships the application only. You provide the database and product-plane service URLs via configuration.

## Get the image

Prefer a **versioned release image**. On each GitHub release, the CI/CD pipeline builds and publishes the image.

| Source | Image | Notes |
|--------|-------|--------|
| **Docker Hub** | [`opendatamesh/odm-platform-registry`](https://hub.docker.com/r/opendatamesh/odm-platform-registry) | Primary public registry. Tags match release versions (e.g. `2.0.11`). |
| **GitHub Container Registry** | `ghcr.io/opendatamesh-initiative/odm-platform-registry` | Alternative when packages are published for the [repository](https://github.com/opendatamesh-initiative/odm-platform-pp-registry-server) / org. May require `docker login ghcr.io`. |
| **Build locally** | Your own tag | Use when you need unreleased changes or a custom build. |

### Pull from Docker Hub

```bash
docker pull opendatamesh/odm-platform-registry:<tag>
# example:
docker pull opendatamesh/odm-platform-registry:2.0.11
```

### Pull from GitHub Container Registry

```bash
# Public pulls may work without login; private packages need a token with read:packages
docker login ghcr.io -u USERNAME -p TOKEN
docker pull ghcr.io/opendatamesh-initiative/odm-platform-registry:<tag>
```

### Build from source

The image expects a Maven-built JAR under `target/` (the Dockerfile copies `odm-platform-pp-registry-server-*.jar`).

```bash
mvn -B package -DskipTests
docker build -t odm-registry-server .
```

## Configure and run

The image defaults to the **`docker`** Spring profile (`PROFILES_ACTIVE=docker`), which binds the datasource from:

| Environment variable | Maps to |
|----------------------|---------|
| `DB_JDBC_URL` | `spring.datasource.url` |
| `DB_USERNAME` | `spring.datasource.username` |
| `DB_PASSWORD` | `spring.datasource.password` |

For a full production-like setup (schema, `server.baseUrl`, Notification, Policy), pass nested Spring config as **`SPRING_PROPS`** JSON. Property meanings: [Configuration](configuration.md#minimal-production-like-configuration).

### Minimal run (datasource only)

Useful to smoke-test connectivity; add Notification / `baseUrl` before relying on lifecycle flows.

```bash
docker run --name odm-registry -p 8080:8080 \
  -e DB_JDBC_URL=jdbc:postgresql://db:5432/registry \
  -e DB_USERNAME=your_username \
  -e DB_PASSWORD=your_password \
  opendatamesh/odm-platform-registry:<tag>
```

### Production-like run (`SPRING_PROPS`)

```bash
docker run --name odm-registry -p 8080:8080 \
  -e SPRING_PROPS='{"server":{"baseUrl":"https://registry.example.com"},"spring":{"datasource":{"url":"jdbc:postgresql://db:5432/registry","username":"your_username","password":"your_password"},"jpa":{"properties":{"hibernate":{"default_schema":"odm_registry"}}}},"odm":{"product-plane":{"notification-service":{"address":"http://notification-service:8006","active":true},"policy-service":{"active":true,"address":"http://policy-service:8005","version":"1"}}}}' \
  opendatamesh/odm-platform-registry:<tag>
```

Replace the image name with `ghcr.io/opendatamesh-initiative/odm-platform-registry:<tag>` or your locally built tag if you are not using Docker Hub.

Optional: `JAVA_OPTS` for JVM flags; `PROFILES_ACTIVE` to override the default `docker` profile.

## Deploy checklist

1. Provision **PostgreSQL** and choose a lowercase schema (default `odm_registry`).
2. Decide **Policy** on or off; if on, deploy Policy Service V1 and set `active` / `address` / `version`.
3. Deploy **Notification** and point the Registry at it; set **`server.baseUrl`** so Notification can reach the Registry observer endpoint.
4. Pull or build the **image**, inject config (`DB_*` and/or `SPRING_PROPS`), expose port **8080** (or your `server.port`).
5. Confirm the app is up: Swagger UI at `/swagger-ui.html`, OpenAPI at `/api-docs`.

## Further reading

- Properties to manage: [Configuration](configuration.md)
- Local build without containers: [Development](development.md)
- Lifecycle / observer behavior: [Events](../service/events.md), [Policy service](../service/policy-service.md)
