#!/usr/bin/env bash
# Creates the next scip-java GitHub release, and with it the tag, on the newest
# main commit whose required checks passed. Prints tag=vM.N.P to $GITHUB_OUTPUT
# so .github/workflows/scheduled-release.yaml can publish it.
#
# Usage: [NEW_VERSION=M.N.P] [DRY_RUN=1] bin/create-release.sh
# NEW_VERSION defaults to the next patch version after the latest release.
set -euo pipefail

REPO="${GITHUB_REPOSITORY:-scip-code/scip-java}"
# The required status checks of main's branch protection.
REQUIRED_CHECKS=("CI pass" "Nix pass" "Repository indexing pass")
# Links the release to the workflow run that creates it.
RUN_URL="${GITHUB_SERVER_URL:-https://github.com}/$REPO/actions/runs/${GITHUB_RUN_ID:-local}"

# A rerun of a workflow run publishes the release that its first attempt
# created, instead of releasing again.
if [ "${GITHUB_RUN_ATTEMPT:-1}" -gt 1 ]; then
  TAG="$(gh api "repos/$REPO/releases?per_page=20" \
    --jq "[.[] | select((.body // \"\") | contains(\"($RUN_URL)\"))][0].tag_name // empty")"
  if [ -n "$TAG" ]; then
    echo "An earlier attempt of this run created $TAG; publishing it again"
    echo "tag=$TAG" >>"${GITHUB_OUTPUT:-/dev/null}"
    exit 0
  fi
fi

git fetch --quiet --tags origin main

LAST_TAG="$(git tag --list 'v*' | grep -E '^v[0-9]+\.[0-9]+\.[0-9]+$' | sort -V | tail -n 1)"
IFS=. read -r MAJOR MINOR PATCH <<<"${LAST_TAG#v}"
NEW_VERSION="${NEW_VERSION:-$MAJOR.$MINOR.$((PATCH + 1))}"

if ! [[ "$NEW_VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "error: NEW_VERSION=$NEW_VERSION is not of the form M.N.P" >&2
  exit 1
fi
if [ "$NEW_VERSION" = "${LAST_TAG#v}" ] ||
  [ "$(printf '%s\n%s\n' "${LAST_TAG#v}" "$NEW_VERSION" | sort -V | tail -n 1)" != "$NEW_VERSION" ]; then
  echo "error: NEW_VERSION=$NEW_VERSION must be newer than the latest release $LAST_TAG" >&2
  exit 1
fi

# Skip commits whose checks failed or are still running, such as a Renovate
# merge from this morning, instead of releasing them or failing the release.
# Never go back past the latest release, which would publish older code.
SHA=""
for commit in $(git rev-list --first-parent --max-count=20 "$LAST_TAG^..origin/main"); do
  passed=true
  for check in "${REQUIRED_CHECKS[@]}"; do
    conclusion="$(gh api -X GET "repos/$REPO/commits/$commit/check-runs" -f check_name="$check" \
      --jq '.check_runs[0].conclusion // ""')"
    if [ "$conclusion" != "success" ]; then
      echo "Skipping $commit: $check is ${conclusion:-pending or missing}"
      passed=false
      break
    fi
  done
  if $passed; then
    SHA="$commit"
    break
  fi
done
if [ -z "$SHA" ]; then
  echo "error: no recent main commit since $LAST_TAG passed ${REQUIRED_CHECKS[*]}" >&2
  exit 1
fi

TAG="v$NEW_VERSION"
if [ -n "${DRY_RUN:-}" ]; then
  echo "Would release $TAG on $SHA (latest release: $LAST_TAG)"
  exit 0
fi
gh release create "$TAG" --repo "$REPO" --target "$SHA" --title "$TAG" --generate-notes \
  --notes "Published by the [scheduled release workflow]($RUN_URL)."
echo "tag=$TAG" >>"${GITHUB_OUTPUT:-/dev/null}"
echo "Released $TAG on $SHA"
