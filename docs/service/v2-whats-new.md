# What's new in Registry V2

High-level view of what V2 adds for operators and developers compared with the older Registry model.

Related:

- [Data product lifecycle](data-product-lifecycle.md)
- [Data product variables](data-product-variables.md)
- [Events](events.md)
- [Policy service](policy-service.md)
- [Git providers](git-providers.md)
- [V1 backward compatibility](v1-backward-compatibility.md)

## Big picture

V2 is built around **explicit lifecycle use cases** and an **event-driven approval loop**, plus first-class **Git / descriptor** integration. The V1 surface under `/api/v1` remains as a compatibility layer.

Product-plane APIs: `/api/v2/pp/registry/...`  
Observer callback: `/api/v2/up/observer/...`  
Interactive docs: Swagger UI / OpenAPI (see root [README](../../README.md)).

## Lifecycle as a first-class design

Instead of only CRUD, V2 exposes intents such as:

- **Init** a data product → `PENDING` + request approval
- **Publish** a version (descriptor) → `PENDING` + request approval
- **Approve / reject** — normally applied when Notification delivers a decision
- **Update documentation fields** without changing validation state
- **Resolve** descriptor variables

CRUD and search still exist for reading and managing resources; governance of “accepted into the registry” is the lifecycle flow. See [Data product lifecycle](data-product-lifecycle.md).

## Event-driven approval

Approval is asynchronous:

1. Init / publish create `PENDING` and emit `*_REQUESTED`
2. Policy (or auto-approve) emits `*_APPROVED` / `*_REJECTED`
3. Observer applies the decision and may emit `INITIALIZED` / `PUBLISHED`

See [Events](events.md) and [Policy service](policy-service.md).

## Observer model

When Notification is active, the Registry registers itself as an observer (callback base URL, name, display name) and receives decisions on its observer endpoint. That registration is what closes the approval loop.

## Git, descriptors, and repositories

V2 can work with a product’s Git repository:

- Read/write descriptors in Git
- List commits, branches, tags
- Talk to GitHub / GitLab / Bitbucket / Azure DevOps (client-supplied PAT headers)

See [Git providers](git-providers.md) and [Descriptor validation](descriptor-validation.md).

## Compared with V1 (at a glance)

| Theme                           | V2                                       | V1 compatibility                       |
| ------------------------------- | ---------------------------------------- | -------------------------------------- |
| Lifecycle init/publish/approve  | Core model                               | Not exposed as V1 use-case APIs        |
| Event-driven PENDING → APPROVED | Primary path                             | Policy V1 can plug into the same bus   |
| Git / repo / provider APIs      | Yes                                      | No                                     |
| Product model                   | Richer (UUID, validation state, repo, …) | Narrower legacy DTO                    |
| Variables                       | Broader V2 APIs + resolve                | Version-scoped list/update on V1 paths |

For what `/api/v1` still supports and how to migrate, see [V1 backward compatibility](v1-backward-compatibility.md).
