# Git providers

High-level view of how the Registry talks to Git hosts for product repositories and descriptors.

Related: [What's new in V2](v2-whats-new.md) · [Data product lifecycle](data-product-lifecycle.md)

## Role

A data product may be linked to a Git repository (`dataProductRepo`). Through that link, the Registry can:

- Read and write the product **descriptor** in Git
- Inspect repository **commits**, **branches**, and **tags**
- Browse or create resources via **Git provider** APIs (organizations, repositories, …)

Supported providers: **GitHub**, **GitLab**, **Bitbucket**, and **Azure DevOps**.

## How authentication works

The Registry does not store long-lived Git credentials for these calls. The **client** sends credentials on each request as HTTP headers. The Registry turns those headers into provider-specific auth and calls the Git provider.

All providers use a **Personal Access Token (PAT)** style credential (Bitbucket also requires a username).

| Provider | What to send |
|----------|----------------|
| GitHub, GitLab, Azure DevOps | Auth type `PAT` + token |
| Bitbucket | Auth type `PAT` + token (app password) + username |

Header names:

- `x-odm-gpauth-type` — always `"PAT"` for the supported method
- `x-odm-gpauth-param-token` — the token / app password
- `x-odm-gpauth-param-username` — Bitbucket only

## Which operations need auth

Any API call that reaches a Git provider needs those headers, including:

- Product **descriptor** get / init / update
- Product **repository** commits, branches, tags
- **Git provider** browsing and repository operations

Exact paths live under `/api/v2/pp/registry/...` (see Swagger UI).

## Minimal permissions (PAT / app password)

Scopes below are the **minimum set for full Registry usage** as implemented today: browse orgs/repos, create repositories, list commits/branches/tags, clone, and **push** (descriptor init/update and tag creation).

They are derived from the Git REST + JGit operations the Registry triggers. Exact UI labels can change — confirm against each provider’s current docs when creating the token. Host-side access still matters (the token user must be allowed to create repos in the org/group/workspace/project).

| Registry capability | Needs |
|---------------------|-------|
| Browse orgs / workspaces / groups | Org/workspace **read** |
| Browse repos, commits, branches, tags; get descriptor | Repo **read** + Git clone |
| Init / update descriptor; create tag | Git **push** (branches and tags) |
| Create repository | Repo **create/write** on the target org/group/workspace/project |
| Bitbucket project picker | Projects **read** |

### GitHub (classic PAT)

| Scope | Why |
|-------|-----|
| `repo` | Private/public repo API, clone, push branches/tags, create repositories |
| `read:org` | List organizations (`/user/orgs`) |

Fine-grained PATs can work if granted at least: repository **Contents** (read and write), **Metadata** (read), and organization **Members** (read) for org listing, plus permission to create repositories where needed. Prefer classic scopes above unless you intentionally lock the token to specific repos.

Creating pull requests is **not** required by the Registry today.

### GitLab (personal access token)

| Scope | Why |
|-------|-----|
| `api` | List user/groups/projects, commits, branches, tags; create projects |
| `write_repository` | Git push (descriptor + tags) |

Notes:

- Listing organizations uses **owned** groups (`owned=true`); the token user must own the groups you expect to see.
- Creating a project in a group also requires sufficient **group membership role** on GitLab (typically Maintainer/Owner), not only the token scopes.

Merge-request scopes are **not** required by the Registry today.

### Bitbucket Cloud (app password)

| Permission | Access | Why |
|------------|--------|-----|
| Account | Read | Current user |
| Workspace membership | Read | List workspaces |
| Projects | Read | Workspace project picker (custom resources) |
| Repositories | Read | List/get repos, commits, branches, tags, clone |
| Repositories | Write | Create repo, push descriptor and tags |

Also send the Bitbucket **username** with the app password (`x-odm-gpauth-param-username`). Pull-request write is **not** required by the Registry today.

### Azure DevOps (PAT)

| Scope (UI) | Access | Why |
|------------|--------|-----|
| **Code** | Read & write | List/get repos, commits, refs; clone; push descriptor/tags; create Git repositories |
| **Project and Team** | Read | List projects (used before listing/getting repos) |

Organization listing is derived from the Azure DevOps URL (no extra org-list API). Creating a repository targets an Azure **project** (project ID as owner). Pull-request scopes are **not** required by the Registry today.

## Creating tokens

Use each provider’s own docs:

- [GitHub Personal Access Tokens](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token)
- [GitLab Personal Access Tokens](https://docs.gitlab.com/ee/user/profile/personal_access_tokens.html)
- [Azure DevOps Personal Access Tokens](https://learn.microsoft.com/en-us/azure/devops/organizations/accounts/use-personal-access-tokens-to-authenticate)
- [Bitbucket App Passwords](https://support.atlassian.com/bitbucket-cloud/docs/app-passwords/)
