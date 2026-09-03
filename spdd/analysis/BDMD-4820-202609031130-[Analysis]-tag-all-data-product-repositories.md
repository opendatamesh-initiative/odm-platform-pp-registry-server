# SPDD Analysis: Tag all data product repositories

## Original Business Requirement

When publishing a new data product version, tags MUST be created also on all additional product repositories.

- DO NOT modify the UI
- Extend the POST /tags endpoint so the tags are created also on all the additional repositories
- Document the endpoint expliciting the behaviour on the description
- Change the dataProductRepositoryUtilsService method name in `tagAllDataProductRepositories`
- Add IT test with the gherkin scenario description on top

The user select an existing tag when publishing the dpv.
  - Ensure also additional repositories have that tag name. If not, fail.
The user creates a new tag when publishing the dpv.
  - Create also on additional repositories. If they already have it, fail.
The user creates a new tag on a branch != default branch
- Create also on additional repositories. If they do not have that branch, fail.

In both case the error message should be clear to the user, with also a recovery suggestion (what he should do before retry publishing).

## Domain Concept Identification

### Existing Concepts (from codebase)

- **Data Product**: Registry aggregate identified by UUID, with a single root Git pointer (`dataProductRepo`) and an optional collection of keyed additional remotes (`additionalDataProductRepos`).
- **Root Data Product Repository**: Descriptor-bearing Git remote used today by `POST /api/v2/pp/registry/products/{uuid}/repository/tags` via `DataProductRepositoryUtilsService.addTag`.
- **Additional Data Product Repository**: Non-root Git remote (`manifestKey`, `externalIdentifier`, owner, provider, default branch). Persisted and validated, but **not** tagged by the current create-tag path.
- **Tag resource (`TagRes`)**: Request/response for tag name, optional commit hash, branch name, message, and author. Unchanged as the HTTP contract.
- **Data Product Repository Controller**: REST surface under `/products/{uuid}/repository`. `POST /tags` currently has no OpenAPI `@Operation` description of multi-remote behaviour.
- **Git provider operations**: Clone/read, resolve HEAD, `addTag`, push — already used for the root remote.

### New Concepts Required

- **All-repositories tag application**: One POST applies the same tag **name** (and message/author) to the root remote and to every additional remote of that data product.
- **Per-remote tag pointer**: Commit hashes are not portable across remotes. Root keeps today’s pointer rules (`commitHash`, else `branchName`, else root `defaultBranch`). Each additional remote uses request `branchName` when present, otherwise that remote’s `defaultBranch`.
- **Existing-tag consistency check**: When the named tag already exists on the root, the POST does **not** create it again. It **verifies** every additional remote already has that tag name. If any additional remote lacks it, the request fails (do not create the missing tag).
- **New-tag uniqueness on additional remotes**: When the named tag does **not** exist on the root, create it on root and additional remotes. If an additional remote **already** has that tag name, fail (do not overwrite or skip).
- **Non-default branch presence**: When creating a new tag and the request names a branch that is not that additional remote’s default branch, that branch must exist on the additional remote. If it does not, fail.

### Key Business Rules

- The UI is **out of scope**. Clients (including Builder `createGitTag`) keep a single POST; the server fans out.
- `GET /tags` remains a listing of the **root** repository only (this slice does not change list/commits/branches).
- Empty or absent `additionalDataProductRepos` keeps today’s root-only create-tag behaviour.
- Do not skip an additional remote silently. Missing Git identity on an additional remote is a client error.
- Git tagging is not transactional: if a later remote fails, do not roll back tags already pushed; return an error.
- Infer existing vs new from **whether the tag already exists on the root remote** (no extra request flag). Existing → verify additional remotes; new → check additional remotes, then create on all remotes with conflict/branch checks.
- Every 400 for the three publish scenarios names the additional repository (`manifestKey`), states what is wrong, and tells the user what to do **before retrying publish**.
- New-tag uniqueness and missing-branch checks run **before** creating on the root, so a failed publish does not leave the tag only on the root.
- Builder version publish must still **POST /tags** when the user selects an existing root tag (otherwise additional remotes are never checked). That is a one-call client change, not a second tag picker.

## Strategic Approach

### Solution Direction

- Extend `DataProductRepositoryUtilsServiceImpl` so create-tag walks root then additional remotes, reusing the existing Git clone/tag/push sequence per remote.
- Build the Git provider from **that remote’s** `providerType` and `providerBaseUrl`.
- Document fan-out on `POST /tags` with OpenAPI `@Operation` summary and description.
- Add one IT next to existing POST `/tags` tests, with a Feature/Scenario Gherkin block, asserting the tag is applied on root and additional remotes.

### Key Design Decisions

- **UI vs registry fan-out**: Fan-out in the existing product tags API. Builder keeps a single POST and must also POST when selecting an existing tag. → **Decided: registry fan-out; Builder always POSTs /tags on Git publish.**
- **New Git-provider create-tag vs extend product POST /tags**: A second API would require UI and agent changes. → **Rejected.** Extend `POST .../repository/tags`.
- **Same commit SHA on additional remotes**: SHAs do not exist on other remotes. → Root may use `commitHash`; additional remotes resolve a branch HEAD per remote.
- **Transactional all-or-nothing**: Git remotes cannot be rolled back atomically. → Fail the request on first error after the failure; do not delete tags already created. Document this.
- **Existing vs new tag — client flag vs Git state on root**: A UI flag would force a contract change. → **Decided:** if the tag exists on the root, treat as “select existing” (verify additional remotes have it; do not create). If it does not, treat as “create new” (create on all; fail if an additional remote already has the tag).
- **Missing tag on additional when selecting existing — auto-create vs fail**: Auto-create would silently tag a different history. → **Decided: fail** and tell the user to create that tag on the additional remote, then retry publish.
- **Tag already on additional when creating new — skip vs fail**: Skip would hide divergent tags. → **Decided: fail** and tell the user to pick another name or remove the extra tag, then retry.
- **Create from a non-default branch missing on additional — skip that remote vs fail**: Skip would leave remotes untagged. → **Decided: fail** and tell the user to create that branch on the additional remote (or tag from a branch present everywhere), then retry.

### Alternatives Considered

- Change Builder to call Git-provider create-tag per additional remote: Rejected — UI must not change.
- Add `manifestKey` on the tag request to target one remote: Rejected — the requirement is to tag **all** remotes in one POST.
- Change GET `/tags` to merge tags from every remote: Out of scope.
- Auto-create a missing tag on additional remotes when the user selected an existing root tag: Rejected — fail with recovery instead.
- Ignore an additional remote that already has the new tag name: Rejected — fail with recovery instead.

## Risk & Gap Analysis

### Requirement Ambiguities

- **Request `branchName` missing on an additional remote**: **Resolved.** Use that additional remote’s `defaultBranch`. If that is also blank, fail that remote (400), do not skip it.
- **Request `commitHash` with additional remotes**: **Resolved.** Apply `commitHash` only when tagging the **root**. Additional remotes always resolve a branch HEAD as above.
- **Additional remote on a different Git provider**: **Resolved.** Build a provider from that remote’s type and base URL (factory mock in ITs still returns the same mock for all identifiers).

### Edge Cases

- No additional remotes: tag root only; 201 as today.
- Two additional remotes: tag root plus both; verify Git `addTag` once per remote.
- Additional remote missing `externalIdentifier` or owner: 400, do not skip.
- Later remote’s Git operation fails: 400 with existing “Failed to create tag” style; earlier remotes may already have the tag.
- Data product not found: 404 unchanged.
- Missing tag name: 400 unchanged.
- Select existing tag (present on root): additional remotes that already have the same tag name → 201, no new Git tag writes.
- Select existing tag: additional remote missing that tag name → 400 with recovery (create the tag on that remote, then retry publish). Do not create it as a side effect.
- Create new tag: additional remote already has that tag name → 400 with recovery (choose another name or delete the extra tag, then retry).
- Create new tag with `branchName` different from that additional remote’s default branch, and the branch is absent → 400 with recovery (create the branch on that remote or tag from a shared branch, then retry).
- Create new tag with no `branchName` or `branchName` equal to the additional default: create on that remote’s default branch (existing fan-out).
- Builder currently POSTs /tags only for **new** tags. Existing-tag publish must also POST /tags so verification runs.

### Technical Risks

- **Non-atomic multi-remote Git**: Documented; no distributed transaction.
- **IT mock `getRepository`**: Existing tests stub one repo id. The new IT must stub root and additional ids and verify `addTag` invocation count.
- **OpenAPI currently missing on POST /tags**: Add summary and description so the contract is explicit.

### Acceptance Criteria Coverage

| AC# | Description                                                        | Addressable? | Gaps/Notes                                                            |
| --- | ------------------------------------------------------------------ | ------------ | --------------------------------------------------------------------- |
| 1   | Fan-out in registry; Builder POSTs /tags for new and existing tags | Yes          | No per-additional tag picker                                      |
| 2   | POST /tags creates the tag on root and all additional repositories | Yes          | Same tag name; per-remote pointer rules                               |
| 3   | Document the endpoint behaviour in the API description             | Yes          | OpenAPI `@Operation` on `createTag`                                   |
| 4   | Rename service method to `tagAllDataProductRepositories`           | Yes          | Interface, impl, controller                                           |
| 5   | Add IT with Gherkin scenario on top                                | Yes          | `DataProductDescriptorControllerIT` next to existing POST /tags tests |
| 6   | Existing tag: additional remotes must already have that tag name   | Yes          | Fail with recovery if missing; do not auto-create                     |
| 7   | New tag: fail if an additional remote already has that tag name    | Yes          | Fail with recovery                                                    |
| 8   | New tag on non-default branch: fail if additional lacks the branch | Yes          | Fail with recovery                                                    |
| 9   | Error text names the remote and how to retry publish               | Yes          | 400 `BadRequestException` messages                                    |
