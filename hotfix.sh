#!/bin/bash

set -e  # Exit on error

DEFAULT_BRANCH="main"

# Check if Git is clean
if [[ -n $(git status --porcelain) ]]; then
  echo "Error: Git working directory is not clean. Commit or stash changes first."
  exit 1
fi

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 <tag>"
  echo "Example: $0 v2.0.11"
  exit 1
fi

TAG="$1"
BRANCH_PREFIX="hotfix"

git fetch origin --tags
if ! git rev-parse -q --verify "refs/tags/${TAG}" >/dev/null; then
  echo "Error: Tag ${TAG} not found."
  exit 1
fi

VERSION="${TAG#v}"
IFS='.' read -r MAJOR MINOR PATCH <<< "$VERSION"
PATCH=$((10#${PATCH:-0}))
NEXT_TAG="v${MAJOR}.${MINOR}.$((PATCH + 1))"
BRANCH="${BRANCH_PREFIX}/${NEXT_TAG}"

if git show-ref --verify --quiet "refs/heads/${BRANCH}"; then
  echo "Error: Branch ${BRANCH} already exists."
  exit 1
fi

git checkout -b "${BRANCH}" "${TAG}"

echo ""
echo "Hotfix branch created: ${BRANCH} (from ${TAG})"
echo ""
echo "Next steps (manual):"
echo "  1. Apply your fix and commit"
echo "  2. git push -u origin ${BRANCH}"
echo "  3. Open a PR into ${DEFAULT_BRANCH}"
echo "  4. Merge and delete the branch"
