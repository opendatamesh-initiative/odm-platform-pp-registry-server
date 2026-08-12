# Registry Server documentation

Index of guides for the **ODM Platform Registry Server**.

<p>
  <a href="http://localhost:8080/swagger-ui.html">Swagger UI</a> ·
  <a href="http://localhost:8080/api-docs">OpenAPI</a>
  <em>(when the service is running locally)</em>
</p>

---

## Service

| Guide | Description |
|:------|:------------|
| [Data product lifecycle](service/data-product-lifecycle.md) | What products/versions are, and how init → approve works |
| [Data product variables](service/data-product-variables.md) | How `${placeholder}` variables are stored and resolved |
| [Descriptor validation](service/descriptor-validation.md) | What publish-time DPDS validation checks and fills in |
| [Events](service/events.md) | How Notification drives approval and lifecycle notifications |
| [Git providers](service/git-providers.md) | How the Registry uses Git hosts and client-supplied auth |
| [Policy service](service/policy-service.md) | Optional governance gate vs auto-approve |
| [What's new in V2](service/v2-whats-new.md) | What changed conceptually in Registry V2 |
| [V1 backward compatibility](service/v1-backward-compatibility.md) | High-level bridge overview; technical detail in `old/README.md` |

## Setup

| Guide | Description |
|:------|:------------|
| [Development](setup/development.md) | Local build, run, profiles, and testing |
| [Deployment](setup/deployment.md) | Docker / container deployment and external dependencies |
| [Configuration](setup/configuration.md) | Properties to manage (DB, Notification, Policy) + minimal example |

---

↑ Back to the [project README](../README.md)
