# ADR-0006: Protecting the build against tampered dependencies

- **Status:** Accepted
- **Date:** 2026-11-28

## Context

Everything the build downloads comes from third parties: Maven and npm packages, the Gradle distribution,
GitHub Actions and Docker images. Most PRs are Dependabot updates of exactly these, and they are merged soon after
they appear. A dependency can be replaced by a malicious one in three ways:

1. **The same version, a different file:** a mirror, CDN or proxy serves a tampered file for a version we already
   use.
2. **A label moved:** GitHub Action tags (`actions/checkout@v7`) and Docker tags (`node:26-alpine`) can be pointed
   at new content at any time. The tj-actions/changed-files compromise (March 2025) spread this way.
3. **A real release that is malicious:** an attacker takes over a maintainer account and publishes a new version
   (the chalk/debug npm compromise, September 2025). Checksums do not help here, because the bad release is
   "genuine". Such releases are usually found and pulled within hours or days.

Already in place before this ADR: `gradle-wrapper.jar` is checked by `wrapper-validation` in CI, `npm ci` checks
every package against the `integrity` hash in `package-lock.json`, and the CI token is read-only by default.

## Decision

- **Dependabot cooldown of 5 days** for every ecosystem: a new release is proposed only once it is 5 days old.
  This is the defence against case 3. Security updates for known vulnerabilities are not delayed.
- **GitHub Actions pinned to full commit SHAs**, with the version in a comment (`@<sha> # v7.0.1`). A commit SHA
  cannot be moved, which covers case 2. Dependabot updates the SHA and the comment together.
- **Docker images pinned by digest** (`node:26-alpine@sha256:…`), including the PostGIS image in `compose.yaml`,
  which Dependabot now watches through the `docker-compose` ecosystem. Covers cases 1 and 2. A rebuilt image
  under the same tag now arrives as a Dependabot PR instead of silently on the next build.
- **The Gradle distribution is checked against its SHA-256** (`distributionSha256Sum` in
  `gradle-wrapper.properties`, the value Gradle publishes for each release). CI validates `gradle-wrapper.jar`, and
  the jar refuses a downloaded zip with a different hash. Covers case 1 for Gradle itself.
- **npm install scripts never run** (`ignore-scripts=true` in `frontend/.npmrc`, also copied into the frontend
  Docker build). A `postinstall` script runs on every install with our permissions, which is how malicious npm
  releases usually strike (the Shai-Hulud worm, September 2025). No installed package needs one today; a future
  one that does has to be allowed on purpose.

Still under consideration, and not decided here:

- Gradle dependency verification (`gradle/verification-metadata.xml`), case 1 for Maven dependencies. It needs
  the file regenerated on every dependency change. Tracked with PGP signatures in issue #58.
- Defences against malicious package code that runs at build time, not install time (install-script allowlist
  check, `npm audit signatures`, isolating local npm runs, and more). Tracked in issue #62.

## Alternatives considered

- **No cooldown, rely on review:** a malicious release looks like any other version bump in a Dependabot PR, and
  its code is not in the diff, so review alone does not catch it.
- **A longer cooldown (7–14 days):** a little safer, but every update arrives later. 5 days covers how long such
  releases usually stay up.
- **Pinning by tag only:** what we had. Simple, but it trusts whoever controls the tag at build time.

## Consequences

- Updates arrive at least 5 days after release.
- `ci.yml`, the Dockerfiles and `compose.yaml` are less readable: each reference carries a long hash. The comments
  and tags keep the version visible.
- More Dependabot PRs: base images are rebuilt often (OS security patches), and every rebuild is now a digest
  update.
