# SPDD Analysis: Blueprint–data product lineage via registry extension properties

## Original Business Requirement

As a user I want to keep track of the lineage between a blueprint and a data product, so that given a data product I can know if it has been instantiated from a blueprint and from which one.
As a user I want to know also which version of the blueprint has been used to instantiate a data product, so if the blueprint is updated with a new version, I can handle that and align the data product.

Note: The notification and alignment are out of scope.
In scope: Blueprint <--> Data product lineage

Open points:
- Where to track lineage (in which model/service)
- Which entities are involved (data product, data product version, blueprint, blueprint version etc...)

Additional clarification:
- A data product repository entity is a "pointer" to a git repository.
- A data product version is a "snapshot" of that repository.
- When a blueprint version is used to instantiate a data product, it is used to provision a given "structure" to a git data product repository.
- A data product repository has always one root blueprint.
- If the repository is later aligned/upgraded to a newer blueprint version, the blueprint version metadata on the `DataProductRepo` is updated.
- Data product versions should keep the blueprint lineage metadata they had when published because they are immutable repository snapshots.
- Notification, alignment status, and warning status remain out of scope for this story; future notifications may be based on new blueprint versions or warnings attached to a blueprint version.
- Blueprint name and blueprint version number are considered natural keys and can be used as identifiers.

Team alignment (follow-up decisions)

- The Registry Service should not know the Blueprint service at all
- The Registry Service should offer extension points to store read-only properties passed during the init use case. Those properties can have a similiar structure to the Blindata Additional Properties. They must be stored inside the Data Product entity. Then copyed (snapshotted) to the Data Product Version when published. Also, if the specification is dpds, they must be copied also into the descriptor content.
- The lineage will be captured by the UI during the blueprint data product instantiation flow and stored into those properties, among with the parameters and their values used to instantiate the data product.
- The registry should also extend search options to allow filtering both data product and data product versions by those additional properties.
- The registry properties should be grouped by scope/context, so the external services, owners of those properties are free of setting whatever keys they want without having to worry of clashing into other owners keys.

## Domain Concept Identification

### Domain Concept Identification

#### Existing Concepts (from codebase)

- **Data product (`DataProduct`, table `data_products`)**: Registry aggregate for identity, FQN, domain, name, display name, description, validation state, and optional nested `DataProductRepo`. Today it has **no** generic extension or metadata bag beyond these fields (`DataProduct.java`, `V1__init_schema.sql`).

- **Data product version (`DataProductVersion`, table `data_products_versions`)**: Immutable-oriented version row with semantic `versionNumber`, descriptor spec/version, JSONB `descriptor_content`, validation state, audit fields. Publish flow runs in `DataProductVersionPublisher` (validate → enrich content for DPDS → persist → notify).

- **Descriptor content (`descriptor_content`, JSONB)**: Structured descriptor payload; for DPDS 1.x, `DataProductVersionPublisherDpdsDescriptorOutboundPort` parses, visits, and re-serializes content during publish enrichment—today without a first-class hook for merging registry-owned extension metadata.

- **Data product repository (`DataProductRepo`, table `data_products_repositories`)**: Git pointer metadata (URLs, branch, provider, owner, descriptor root path). **No** blueprint lineage columns exist today; prior analysis considered anchoring lineage here—**superseded** by the team decision to store extension properties on `DataProduct` and snapshot to versions.

- **Data product initialization (`POST` init use case, `DataProductInitCommandRes`)**: REST command currently wraps only `DataProductRes` (`DataProductInitCommandRes.java`); persistence flows through `DataProductsUseCasesService` / initializer factory. **No** extension-property payload exists on the command today.

- **Data product search (`DataProductSearchOptions`, `DataProductsServiceImpl#getSpecFromFilters`)**: Filters limited to **domain**, **name**, and **fqn** exact match via JPA `Specification` (`DataProductsRepository.Specs`).

- **Data product version search (`DataProductVersionSearchOptions`, query services)**: Filters include data product UUID, name, tag, version number, validation state, and a **name** `search` match parameter—**no** generic property-based filter today.

- **Blueprint / blueprint version**: Platform concepts **outside** this service’s persistence and Java modules; team direction explicitly forbids registry-to-blueprint coupling.

#### New Concepts Required

- **Data product extension properties (read-only bag, scope-grouped)**: Opaque, client-supplied structured metadata (conceptually analogous to Blindata “additional properties”) attached to `DataProduct`, accepted during **init**, then **immutable via normal registry APIs** for this story (no ad-hoc mutation here). Properties are **grouped by scope/context** (owner namespace): each external owner writes keys **only inside its scope**, so key names do not need global coordination across owners. **Operational corrections** to extension data are explicitly **a separate use case** and **out of scope** for now. The bag is the carrier for blueprint lineage **values** and instantiation **parameter values** without the registry interpreting blueprint semantics.

- **Extension property scope (owner context)**: A stable identifier for a logical context (owner or integration boundary) under which arbitrary keys are meaningful; uniqueness of keys is **per scope**, not across the whole document. The registry treats scope ids and inner key maps as **opaque** beyond agreed structural and size rules.

- **Version snapshot of extension properties**: A durable copy of the data product’s **scope-grouped** extension property structure (or a defined subset) stored alongside each `DataProductVersion` at publish time so historical published descriptors remain explainable even if a **future** correction use case changes the live product’s extension bag.

- **DPDS descriptor embedding of extension metadata**: When `descriptor_spec` indicates DPDS, the same **scope-grouped** snapshot must also be reflected **inside** `descriptor_content` so the published artifact is self-contained for DPDS consumers.

- **Property-aware search dimensions**: Query surfaces that can filter **both** data products and data product versions by extension metadata, typically addressing **`scope` + `key` + `value`** (or equivalent path semantics) so filters remain unambiguous when multiple owners store overlapping key names under different scopes, extending the existing `Specification`-driven patterns.

#### Key Business Rules

- **Registry isolation**: The registry must **not** call or depend on the blueprint service; it must not resolve blueprint identifiers server-side. Provenance is **declared** by the orchestrating client (UI) and stored opaquely.

- **Init-time ingestion**: Extension properties are supplied during the data product **init** use case and persisted on the data product aggregate, **grouped by scope/context** so each owning integration can choose keys freely within its scope.

- **Per-scope key autonomy**: Key names are unique **within** a scope; collisions across scopes are prevented by structure, not by global key prefix discipline alone.

- **Publish-time snapshot**: On publish, the **full scope-grouped** extension metadata is **copied** from the data product (or from an agreed source of truth at publish time) onto the `DataProductVersion`, in addition to any merge into DPDS `descriptor_content` when applicable.

- **Immutability of published version semantics**: A published `DataProductVersion` must retain the lineage and parameter snapshot it had at publish time, consistent with “versions are repository snapshots” from the original clarification.

- **Scope boundary**: Notification, alignment workflows, and alignment/warning **status** remain out of scope; stored lineage must still be sufficient for future comparison features elsewhere.

- **Optional lineage**: Not every data product is blueprint-backed; extension properties and search must tolerate absence or partial population.

- **Read-only vs corrections (scope)**: For this initiative, extension properties set at init are not updated through a general-purpose “fix my metadata” path; any **operational correction** of wrong or stale extension values belongs to a **future dedicated use case** and is **explicitly out of scope** here.

## Strategic Approach

### Strategic Approach

#### Solution Direction

- **Decouple bounded contexts**: Keep the registry authoritative for **registration and retrieval** of data products and versions, while treating blueprint lineage as **opaque client metadata** carried in extension properties—no blueprint integration in the registry process layer.

- **Leverage existing technical patterns**: Spring Boot 3.5 / JPA with `ddl-auto: validate`, Flyway migrations under `classpath:db/migration/postgresql`, JSONB for semi-structured payloads (`descriptor_content` precedent), use-case-centric publish flow (`DataProductVersionPublisher`), and `Specification`-based listing for products and versions.

- **End-to-end data flow (conceptual)**: The UI orchestrates blueprint instantiation and, **before or after** blueprint Git work as today, calls registry **init** with a `DataProduct` payload that includes the **scope-grouped** extension property bag (e.g. one scope for UI/orchestrator lineage and parameters, other scopes reserved for other platform owners as needed). On **publish**, the registry snapshots the full grouped structure onto the `DataProductVersion` and, for DPDS, ensures the descriptor JSON also carries the same information without requiring a blueprint round-trip.

#### Key Design Decisions

- **Lineage ownership (product vs repository)**: *Trade-offs*: Repository-level fields co-locate with Git provisioning metadata; **product-level** extension properties simplify the API (one bag on the aggregate the UI already posts) and match the team mandate. *Recommendation*: **Anchor the extension bag on `DataProduct`**, snapshot to `DataProductVersion`; avoid introducing blueprint-specific columns so the registry stays generic.

- **Read-only semantics**: *Trade-offs*: Strict immutability after init maximizes auditability; ad-hoc updates would complicate audit and trust. *Recommendation*: Treat extension properties as **not mutable through the lineage/init scope**—**operational corrections require a separate use case**, which is **out of scope for now** (no ambiguity on process: defer corrections to that future story).

- **DPDS embedding strategy**: *Trade-offs*: Custom top-level JSON keys may break strict schema validation; using a DPDS-endorsed container (if one exists in the spec) reduces validation risk. *Recommendation*: **Follow DPDS rules for where ancillary metadata may live** during REASONS Canvas; at strategy level, require **dual persistence**—columns/JSONB on the version row **and** merged published descriptor—for DPDS specs only.

- **Search approach**: *Trade-offs*: PostgreSQL JSONB containment with GIN indexes vs normalized EAV side tables. *Recommendation*: Start from **JSONB on product and version snapshot** with **indexed containment** for expected query paths (at minimum **scope + key + value**, or JSON path equivalent); revisit normalization only if cardinality or performance demands it.

- **Scoped grouping vs flat key-value**: *Trade-offs*: A flat map with ad-hoc prefixed keys avoids nested JSON but pushes collision avoidance to every client; **explicit grouping by scope/context** matches the stated requirement and keeps owner boundaries clear in API, persistence, search, and DPDS embedding. *Recommendation*: **Model extension properties as a map (or ordered list) of scopes**, each scope owning its own key-value map; the registry validates container shape and limits, not business meaning per key.

- **Trust model without blueprint callback**: *Trade-offs*: Verifying lineage against blueprint improves integrity but violates “registry must not know blueprint.” *Recommendation*: Accept **client-attested** values; mitigate with size limits, structural rules for scopes, and optional future policy hooks **outside** the registry if the platform requires stronger guarantees.

#### Alternatives Considered

- **Registry-owned blueprint columns on `DataProductRepo` (prior analysis direction)**: Rejected relative to current team alignment because it couples the domain model to blueprint vocabulary and does not match the “extension properties on `DataProduct`” decision.

- **Lineage only inside `descriptor_content`**: Rejected as the **sole** store because listing/search across products and versions becomes harder and non-DPDS specs would be left out; the team explicitly requires **entity storage** plus DPDS embedding.

- **Registry calls blueprint to validate lineage**: Rejected by explicit architectural decision to keep the registry ignorant of the blueprint service.

## Risk & Gap Analysis

### Risk & Gap Analysis

#### Requirement Ambiguities

- **`DataProductRepo` alignment vs product-level extension bag**: Original clarification still speaks of updating blueprint metadata on `DataProductRepo`, while the new direction stores lineage on `DataProduct` extension properties. **Clarify** whether repository alignment updates **only** Git-side concerns, whether the UI must **re-init or PATCH** extension properties, or whether alignment remains entirely outside registry scope.

- **“Read-only” meaning**: **Resolved for this scope**—extension properties supplied at init are **not** subject to operational correction flows in this story; **any correction of stored extension metadata is a separate use case** and is **out of scope** for now. (Whether future correction applies uniformly to all keys or only subsets can be decided in that later use case.)

- **DPDS placement rules**: “Copied into the descriptor content” does not yet specify **which** DPDS subtree is authoritative; wrong placement could fail `DataProductVersionPublisher` validation or downstream consumers.

- **Bidirectional lineage scope**: Original requirement used “↔”; search extensions help **from property → products**, but **blueprint → all products** navigation may still live in the UI or another service—confirm product expectations.

- **Scope identifier governance**: The requirement mandates grouping by scope/context but does not yet define how scope strings are **allocated** (e.g. reverse-DNS, UUID, central registry of scope ids) or how **spoofing** another owner’s scope id is prevented if multiple clients can call init. Clarify governance vs purely structural grouping during REASONS Canvas.

#### Edge Cases

- **Non-DPDS specs**: Extension snapshot on `DataProductVersion` should still apply; descriptor merge applies **only** when spec is DPDS as agreed.

- **Publish without extension properties**: Must not break publish; snapshot may be empty.

- **Large or highly dynamic parameter sets**: Risk of oversized JSONB payloads; needs limits and possibly key allowlists **per scope**.

- **Duplicate scope id in one payload**: If two writers merge payloads with the same scope key, define deterministic merge or reject rules so inner key maps do not silently overwrite each other in an undefined order.

- **Rename/delete of blueprints**: Without server-side validation, stored lineage may reference **stale** identifiers; consumers must tolerate missing blueprint metadata.

#### Technical Risks

- **Schema and JPA sync**: New JSONB columns (or equivalent) require Flyway migrations aligned with `hibernate.ddl-auto: validate` and entity mappings.

- **Publisher merge order**: `DataProductVersionPublisher` already replaces `content` with enriched JSON; merging extension metadata must cooperate with `enrichDescriptorContentIfNeeded` so enrichment does **not** drop provenance.

- **Search performance**: Ad-hoc JSON filters without indexes can degrade list endpoints; plan for GIN or expression indexes on declared search keys.

- **Cross-layer consistency**: OpenAPI resources (`DataProductRes`, init command, version resources, search option DTOs) must evolve together with persistence to avoid drift.

- **DPDS embedding with nested scopes**: Merging scope-grouped metadata into `descriptor_content` must preserve DPDS validity and remain consistent with how search addresses the same data (same logical path for a given scope/key).

#### Acceptance Criteria Coverage

| AC# | Description | Addressable? | Gaps/Notes |
|-----|-------------|--------------|------------|
| AC1 | As a user, given a data product, know if it was instantiated from a blueprint and which one | Yes | Depends on the **UI** writing lineage into a **dedicated scope** (or agreed scope id) within extension properties at init; registry exposes read/search surfaces but does **not** infer blueprint linkage without client data |
| AC2 | Know which blueprint version was used to instantiate, to reason about blueprint updates | Yes | Same as AC1: version identity is **declared** under the agreed scope; no registry-side blueprint version resolution |
| AC3 | Notification and alignment out of scope; blueprint ↔ data product lineage in scope | Yes | No gap; avoid adding alignment/notification state while still enabling future comparisons using stored property snapshots |
