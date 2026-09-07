# SPDD Analysis: Multi-repository data product version snapshot tags

Companion to BDMD-4820 multi-repository instantiate/update work. Additional Git remotes are already persisted on the Data Product. This analysis covers the missing half of version identity: recording which Git tag on each additional remote belongs to a published data product version.

Registry must expose the snapshot model and Git-tag operations.

## Original Business Requirement

When supporting multiple-repository data product environment, we need to keep track of the "snapshot" a data product version represent. Currently this is done for mono-repo scenario with:

- Root data product repository pointer stored in the Data Product
- Tag referencing the root repository stored in the data product version.

we need an extension so:

- Additional data product repositories pointers stored in the Data Product
- Tags referencing the different additional repositories stored in the data product version.

---

## Domain Concept Identification

### Existing Concepts (from codebase)

- **Data Product**: Registry aggregate for a product. Owns Git remote pointers. Relationship: one product has at most one root repository and zero-to-many additional repositories.
- **Root data product repository pointer**: The descriptor-bearing Git remote stored on the Data Product. Relationship: used today to list/create Git tags, read the descriptor, and as the only remote the version `tag` refers to.
- **Additional data product repository pointer**: Already modeled in code. Keyed Git remotes on the Data Product (non-root), identified by a **repository key** matching blueprint instantiation keys. Same provider/owner/URL shape as the root pointer, without a descriptor path. Relationship: product-level configuration of the polyrepo environment; replaced as a set when the product is updated; **not** versioned themselves. Schema lives in undeployed `V2` and will be completed there (constraints + version additional tags), not via a follow-on migration.
- **Data Product Version**: A published snapshot of the product. Today the Git snapshot is a single optional `tag` string that is unique per product (when set). Relationship: the tag names the Git ref on the **root** remote; descriptor content is stored independently on the version.
- **Root version tag**: The version’s identity tag against the root repository. Used for uniqueness, search, delete-by-FQN+tag, and compact notification payloads. Relationship: one tag name per version; uniqueness is across versions of the same product.
- **Publish use case**: Persists a new version (descriptor validation, version-number extraction, PENDING state, publication-requested event). It does **not** create Git tags. Git tag creation is a separate repository operation invoked by the client before publish.
- **Data product repository Git operations**: List commits/branches/tags and create a tag, always against the **root** pointer. Generic Git-provider APIs can list branches of an arbitrary remote, but cannot list or create tags using a stored additional pointer.
- **Descriptor read**: Always from the root repository (tag/branch/commit). Additional remotes do not carry the descriptor.
- **Version listing (short)**: Lightweight version rows include the single root tag, not nested collections.
- **Publication events**: Requested event embeds the full version resource. Compact approve/reject/policy events copy the single root `tag` as the version identifier.

### New Concepts Required

- **Additional version tags (per-remote snapshot refs)**: For each additional repository that belongs to the product at publish time, the version stores the Git tag name that pins that remote. Relationship: keyed by the same **repository key** as the additional repository pointer; they are version-owned snapshot metadata, not a replacement of the root `tag`.
- **Multi-repository version snapshot**: The pair (root tag + additional tags) is what a version “represents” in Git. Relationship: root tag remains the version’s **identity** for lookup/uniqueness; additional tags complete the polyrepo checkout set.
- **Additional-remote Git targeting**: Ability to list tags/branches and create a tag on a stored additional pointer, not only on the root. Relationship: required so clients can create the Git refs that the version will then record. Does not change the publish use case’s job (persist snapshot names).

### Key Business Rules

- Mono-repo products (no additional repositories) keep today’s model: root pointer on the product, single tag on the version. Additional tags are absent only because there are no additional remotes — not as a legacy/compat allowance.
- Additional repository pointers stay on the **Data Product** (already modeled). Do not copy the full pointer onto the version; the version only stores tag names keyed by repository key.
- Completeness is an invariant from the first deploy: a published version in a polyrepo environment must record a tag for **every additional repository present on the product at publish time**. There is no pre-existing population of versions to leave empty.
- Root `tag` remains the version identity: uniqueness, search, and delete-by-tag continue to use it. Additional tag names are **not** a second identity key.
- Additional tag names are **not** unique across versions of the same product. Two versions may share the same tag on an additional remote when that remote did not change. They **are** unique per version per repository key (one tag per additional remote per version).
- The same tag **name** may be used on the root and on any number of additional remotes (typical “tag `1.0.0` everywhere”). That is allowed.
- Additional tags are immutable after publish (same as root tag: documentation-field updates do not change tags).
- Additional tags are joined by **repository key string**, not by additional-repository row identity. Product-level replace of additional remotes must not rewrite or delete historical version tags.
- Unknown repository keys in a publish payload (keys not on the product) are rejected. Missing keys for current additional remotes are rejected.
- Publish still does not create Git tags. The client creates Git tags (now on root **and** additional remotes) and then sends the names. Registry validates and stores them.
- Descriptor fetch, descriptor upload, and version-number extraction stay root/descriptor-only. Additional tags do not participate in descriptor handling.
- Compact events that currently carry a single `tag` keep that field as the root identity. Full version resources (and events that embed them) carry additional tags additively.

---

## Strategic Approach

### Solution Direction

Treat this as an **additive snapshot extension** on the existing version aggregate, plus a **targeting extension** of existing data-product Git operations. Persist both additional remotes and additional version tags in the **same undeployed `V2` migration** (edit that file; no `V3`, no expand/contract, no backfill).

1. **Persist**: When publishing a version, accept and store additional tag names keyed like additional remotes. Empty/absent only when the product has no additional remotes (mono-repo).
2. **Return**: Expose additional tags on the full version resource (publish result, get-by-id). Listing can keep showing the root tag as today.
3. **Operate on Git**: Let the existing data-product repository Git surface (list tags/branches, create tag) address an additional remote by repository key, defaulting to the root when the key is omitted — so current mono-repo clients are unchanged.
4. **Do not** move Git tag creation into the publish use case, and **do not** replace the root `tag` field with a list.

High-level flow stays: client creates Git tags on each remote → client publishes version with root tag + additional tags → registry validates against the product’s additional remotes → persist version → emit events.

Leverage existing conventions: hexagonal publish use case, product-level additional remotes (repository key, orphan-replace set), version CRUD uniqueness for the root tag, repository Git operations via stored pointers and git-utils.

### Key Design Decisions

- **Additional remotes already exist on the Data Product (code) but `V2` is undeployed**: Pointer CRUD is already implemented. Schema for remotes **and** version tags is still greenfield. → **Keep the existing additional-remote model in code; extend `V2__data_products_additional_repositories.sql` in place** (version additional-tags table, uniqueness on `(data_product_uuid, repository_key)` for remotes if missing). Do not introduce `V3` or a data migration.
- **Schema compatibility vs runtime snapshot semantics**: “Never deployed” means no Flyway/data backfill. It does **not** mean versions become invalid if remotes are added later. Completeness is evaluated **at publish time** against the remotes then on the product.
- **Keep root** `tag` **vs collapse all tags into one collection**: Collapsing would break uniqueness, search, delete-by-tag, compact events, and every mono-repo client. → **Keep root** `tag` **as identity. Add an additional-tags collection keyed by repository key.**
- **Store tag names vs store full remote pointers on the version**: Pointers can change on the product over time; a version snapshot is “which ref on which logical remote (key).” Duplicating URLs/ids on every version would drift from product updates and duplicate the additional-remote model. → **Store only (repository key, tag name) on the version.**
- **Require a tag per current additional remote vs allow partial snapshots**: A partial set cannot reconstruct the environment a version represents. There is no deployed history to preserve. → **Require completeness at publish when additional remotes exist.** Empty additional tags are valid **only** for mono-repo publishes (no additional remotes on the product).
- **Uniqueness of additional tag names across versions**: Forcing uniqueness would forbid two versions from pinning the same unchanged additional remote. Root uniqueness exists because that tag **is** the version identity. → **No cross-version uniqueness for additional tags.** One tag per (version, repository key).
- **Git operations: extend data-product repository surface vs generic Git-provider APIs**: Tag create today clones via the stored pointer. Generic Git-provider APIs list branches of an arbitrary id but have no tag list/create and would force the client to re-supply pointer fields. → **Extend the data-product repository Git operations with optional additional-remote targeting; omit the selector → root (backward compatible).**
- **Publish creates Git tags vs client creates them**: Changing publish would mix Git credentials, fail-partial remotes, and descriptor-upload into the use case, and would diverge from today’s split. → **Keep the split.** Registry Git API must support additional remotes so the UI can create tags; publish only persists names.
- **Listing (short) includes additional tags vs full resource only**: Short listing is a separate projection without collections; loading them on every list is extra cost. The versions list today shows the identity tag. → **Full version resource carries additional tags. Short listing stays root-tag-only unless a later UI need forces an additive list field.**
- **Events**: Requested publication already maps the full version. Compact approve events only copy uuid + root tag. → **Additive on the full resource; do not require compact/policy event schema changes for this ticket.**
- **Join additional tags to remotes by key, not by additional-repo UUID**: Additional remotes are replaced as a set (orphan removal) when the product is updated. A FK to those rows would delete or block snapshot tags on a later product update — a forward runtime problem, not a legacy-data one. → **Repository key is the stable logical id.**

### Alternatives Considered

- **Re-implement additional repository pointers on the Data Product**: Rejected. Already modeled (entity, persistence, validation, API, tests). Only the undeployed `V2` file may be extended.
- **Add a new Flyway version (`V3`) and keep `V2` frozen**: Rejected. `V2` has never been deployed; edit it directly so remotes and version tags ship as one schema.
- **Single list of tags replacing** `tag`: Rejected. Breaking for identity, uniqueness, search, delete, events, and mono-repo clients.
- **Same tag name automatically applied to all remotes (no per-remote storage)**: Rejected. The requirement asks for tags **referencing the different** additional repositories, and UI must select/create **multiple** tags. Remotes can (and often will) use different names, or reuse an older tag on an unchanged remote.
- **Copy full additional-remote objects onto the version**: Rejected. Duplicates product configuration; drifts when remotes are reconfigured; snapshot only needs the ref name.
- **JSON blob of key→tag on the version row**: Rejected at the strategic level in favor of a proper child collection consistent with additional remotes (queryable, constrained, mapped). Exact persistence mechanics belong in REASONS Canvas.
- **Create Git tags inside publish**: Rejected. Changes the current client/server split, couples Git credentials to publish, and collides with descriptor-upload (no Git).
- **Skip Git API work (UI uses generic Git-provider only)**: Rejected for tag create/list. No generic tag API exists; create needs the stored pointer and git-utils path already used for the root.
- **Foreign key from version tags to additional-repository rows**: Rejected. Those rows are replaced on product update; snapshot tags must outlive a later remote replace.
- **Leave empty additional tags on polyrepo versions as a compatibility mode**: Rejected. `V2` is undeployed; every polyrepo publish from day one must be a complete snapshot. Empty additional tags are only the mono-repo case.

---

## Risk & Gap Analysis

### Requirement Ambiguities

- **Pointers vs tags as the actual gap**: The requirement lists additional pointers as something “we need.” They are already modeled in code. **Resolved here:** keep that model; complete undeployed `V2` with version additional tags; new work is version tags + Git targeting.
- **Must every additional remote have a tag on every publish?**: Not stated. **Resolved:** yes, when the product currently has additional remotes. Otherwise the snapshot is incomplete.
- **May two versions share an additional tag?**: Not stated. **Resolved:** yes — additional tags are pins, not identity.
- **Must additional tag names match the root tag?**: Not stated. **Resolved:** no. Equality is allowed, not required.
- **Does publish create the Git tags?**: Not stated. Today the client does. **Resolved:** keep that; registry Git API must support additional remotes.
- **Descriptor-upload publish in a polyrepo**: Today root tag/branch can be omitted for content. **Resolved:** if additional remotes exist, additional tags are still required so the version still records a Git snapshot of those remotes. Root tag remains optional as today.
- **Schema / existing data**: `V2` additional remotes have not been deployed. **Resolved:** modify `V2__data_products_additional_repositories.sql` directly; no `V3`, no expand/contract, no backfill. Empty additional tags are **not** a compatibility row type.
- **Product additional remotes change after versions exist**: Not stated. **Resolved:** tags stored on a version stay as published (snapshot at that time). New publishes validate against the **current** product remotes. Adding remotes later does not require rewriting older versions; those older versions were complete for the remotes that existed when they were published.
- **Listing and compact events**: Not stated. **Resolved:** identity `tag` unchanged; additional tags on the full version resource.

### Edge Cases

- **Mono-repo publish**: No additional remotes → additional tags absent/empty; Git operations without a remote selector still hit the root. Behavior identical to today.
- **Polyrepo, all remotes get a new tag with the same name**: Allowed. Root uniqueness still applies to the root tag only.
- **Polyrepo, additional remote unchanged**: Client may select an **existing** Git tag on that remote (possibly used by an older version). Allowed.
- **Polyrepo, client omits one additional tag**: Publish rejected.
- **Polyrepo, client sends a tag for a key the product does not have**: Publish rejected.
- **Additional remotes added to the product after older versions**: Older versions stay as they were (complete for remotes at their publish time; additional tags empty if they were mono-repo then). New publishes must include tags for the new keys. This is snapshot-at-publish semantics, not a migration/backfill of undeployed `V2` data.
- **Additional remotes removed or keys replaced on the product**: Historical version tags for old keys remain (dangling vs current product, still a valid historical snapshot). New publishes must not require the old keys.
- **Git tag created on additional remote, then publish fails**: Same class of leftover as today’s root tag-create-then-publish. No distributed rollback. Client/UI should treat Git create as a prerequisite and surface which remote failed.
- **Git tag create when the tag already exists on that remote**: Same as root today (Git/provider error). Selecting an existing tag must **not** attempt create.
- **Descriptor upload + polyrepo**: Descriptor content from file; additional tags still stored; Git create still needed for any **new** additional tags the user asked to create.
- **Root tag omitted (allowed today) with additional tags present**: Allowed if descriptor content is supplied another way. Additional-tag completeness still applies.
- **Duplicate repository keys in additional tags on one version**: Rejected (mirrors duplicate additional remotes on the product).
- **Search/delete by additional tag**: Not supported. Identity remains root tag / uuid / version number.
- **Release notes / commit range**: Still computed from the root remote. Additional remotes are out of scope for notes.

### Technical Risks

- **Git operations are root-only**: Without targeting additional remotes, UI cannot list or create the tags the version must store. Mitigation: extend the existing data-product repository Git operations with optional remote selection; default = root.
- **Two JPA projections of the version (full vs short)**: Additional tags live on the full aggregate. Short listing must not silently drop a uniqueness rule or require a collection load. Mitigation: short stays identity-only; tests cover publish/get, not only list.
- **Product PUT orphan-replace of additional remotes**: Version tags must not cascade. Mitigation: no FK from version tags to additional-remote rows.
- **Event/policy consumers**: Compact events still have one `tag`. Mitigation: do not change that field’s meaning; additional tags are additive on the full resource. Policy v1 payloads stay root-tag-based unless a later ticket expands them.
- **Publish transaction vs Git**: Git creates remain outside the publish transaction (already true). Partial Git success across remotes is more likely with N remotes. Mitigation: fail-fast in the client; registry does not attempt compensating Git deletes.
- **Uniqueness tests**: Root tag uniqueness must stay; new tests must allow the same additional tag name on two versions and reject two tags for the same key on one version.
- **Credentials per remote**: Additional remotes may sit on another provider/org. Git operations must use **that** pointer’s provider, not the root’s. Mitigation: resolve pointer by repository key before building the Git provider.
- **UI coupling**: Registry API must be ready before UI can create/select additional tags. Sequence: registry snapshot + Git targeting, then UI publish section.
- **Local/test DBs that already applied the current `V2`**: Flyway checksum will change when `V2` is edited. Mitigation: rebuild those databases; do **not** add `V3` to paper over checksum drift. Production is treated as never having run `V2`.

### Acceptance Criteria Coverage

The requirement does not number ACs; implied criteria:

| AC# | Description                                                                                     | Addressable? | Gaps/Notes                                                                  |
| --- | ----------------------------------------------------------------------------------------------- | ------------ | --------------------------------------------------------------------------- |
| 1   | Additional data product repository pointers are stored on the Data Product                      | Yes          | Modeled in code; schema completed in undeployed `V2` (edit in place).       |
| 2   | A data product version can store tags that reference each additional repository                 | Yes          | Keyed by repository key; additive to root `tag`; table added in the same `V2`. |
| 3   | Mono-repo versions keep a single root tag; no new required snapshot fields                      | Yes          | Additional tags empty/absent only when there are no additional remotes.     |
| 4   | A polyrepo version’s Git snapshot is reconstructible (root tag + additional tags)               | Yes          | Completeness required at every polyrepo publish from day one; no backfill.  |
| 5   | Root tag remains the version identity (uniqueness, search, delete-by-tag)                       | Yes          | Additional tags are not identity.                                           |
| 6   | Clients can list/create Git tags on additional remotes (so the snapshot names can exist in Git) | Yes          | Extend data-product repository Git operations; default target remains root. |
| 7   | Publish does not break descriptor-upload or root-only descriptor fetch                          | Yes          | Additional tags do not affect descriptor content handling.                  |
| 8   | Existing compact events and short listings keep working                                         | Yes          | Additive full-resource field; short/compact stay root-tag identity.         |
| 9   | UI can publish a polyrepo version with multiple tags without changing mono-repo behavior        | Partial      | Registry enables it; UI behavior is the companion analysis in blindata-ui.  |
