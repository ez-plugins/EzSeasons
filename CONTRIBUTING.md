# Contributing to EzSeasons

Thanks for your interest in contributing to EzSeasons.

## Local setup

### Prerequisites

- Java toolchain version defined by the parent build (`java.version` in the parent POM).
- Maven 3.9+.
- A Paper-compatible test server (Paper 26.1+, Java 25) for runtime checks.

### Build locally

```bash
mvn -q clean package
```

If this module is built inside the multi-module parent project, run Maven from the parent root so inherited properties resolve correctly.

### Run in a local server

1. Build the JAR.
2. Copy `target/ezseasons-v<version>.jar` into `<server>/plugins/`.
3. Start your Paper server.
4. Validate `/season` output and config behavior.

## Coding standards

- Keep changes focused and small; prefer incremental PRs.
- Follow existing Java style in `src/main/java`:
  - Clear class responsibilities.
  - Avoid unnecessary abstraction.
  - Keep null handling explicit.
- For user-facing text, update defaults in `src/main/resources/config.yml`.
- Preserve backwards-compatible behavior for public API interfaces in `com.skyblockexp.lifesteal.seasons.api` when possible.
- Add or update documentation (README, templates, config notes) whenever behavior or integration patterns change.

## Maintainer operations

- Code of Conduct reports are received through the maintainer-controlled alias `ezplugins@outlook.com`.
- At least two current maintainers must have mailbox access, and access must be transferred before any maintainer offboarding to preserve continuity.

## Pull request process

1. Create a branch from the current default branch.
2. Implement and test changes locally.
3. Fill in `.github/pull_request_template.md` completely.
4. Link related issues and include reproduction + validation steps.
5. Ensure docs/config examples are updated when API or configuration changes.

### PR checklist

- [ ] Build passes locally.
- [ ] No unrelated refactors included.
- [ ] Public API changes are called out explicitly.
- [ ] Config migration impact is documented.

## Versioning notes

- Artifact naming follows `ezseasons-v<version>` via Maven build config.
- Plugin runtime version comes from `${project.version}` in `plugin.yml`.
- Treat API interface changes (`SeasonsApi`, `SeasonsIntegration`) as compatibility-sensitive:
  - additive changes are preferred;
  - breaking changes should align with a project version bump and migration notes in README + PR.
