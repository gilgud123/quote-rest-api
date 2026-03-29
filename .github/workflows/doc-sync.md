---

description: |
Runs daily to keep repository documentation up to date. Identifies documentation
files that are out of sync with recent code changes and opens a pull request
with the necessary updates.

on:
schedule: daily on weekdays
workflow_dispatch:

permissions:
contents: read
pull-requests: read
issues: read

network:
allowed:
- defaults
- java

checkout:
fetch-depth: 0

tools:
github:
mode: remote
toolsets: [default]
edit:
bash: true

safe-outputs:
create-pull-request:
title-prefix: "[docs] "
labels: [documentation, automated]
draft: false

engine: copilot
---------------

# Documentation Sync

You are a documentation maintenance agent. Your job is to identify documentation files
that are out of sync with recent code changes and produce a pull request with the
necessary updates.

## Context

This is a **Quote REST API** — a Spring Boot 3 application (Java 17, Maven) with:

- **Backend**: `backend/src/main/java/com/katya/quoterestapi/` — controllers, services,
  repositories, entities, DTOs, mappers, config, exceptions.
- **Frontend**: `frontend/` — Angular application.
- **Documentation files** to keep in sync:
  - `README.md` — primary project documentation.
  - `references/ALL_ENDPOINTS_REFERENCE.md` — complete API endpoint reference.
  - `references/ANGULAR_FRONTEND_DETAILED_PLAN.md` — Angular frontend architecture.
  - `references/ANGULAR_SECURITY_GUIDE.md` — security implementation guide.
  - `references/JENKINS_SETUP.md` and `references/JENKINS_PIPELINE_GUIDE.md` — CI/CD docs.
  - `tests/README.md` — test documentation.
  - `scripts/README.md` — scripts documentation.
  - `frontend/README.md` — frontend-specific documentation.

## Process

### Step 1 — Find recent code changes

Run the following to discover what source files changed since the last weekday run.
Because this workflow runs on weekdays only, use a 96-hour window to safely cover
the Friday→Monday weekend gap:

```bash
git log --since="96 hours ago" --name-only --format="" | sort -u | grep -v '^$'
```

If that returns nothing (e.g. the repository has had no commits recently), expand
to 7 days as a fallback:

```bash
git log --since="7 days ago" --name-only --format="" | sort -u | grep -v '^$'
```

Note the changed files. Focus on changes to:

- Java source files under `backend/src/main/java/`
- Configuration files (`backend/src/main/resources/`, `pom.xml`, `docker-compose*.yml`)
- Frontend source files under `frontend/src/`
- Test files under `backend/src/test/` and `tests/`
- Script files under `scripts/`

### Step 2 — Assess documentation impact

For each changed source file, determine which documentation files may be out of sync:

|                         Changed area                          |                                Likely affected docs                                |
|---------------------------------------------------------------|------------------------------------------------------------------------------------|
| Controller endpoints (`controller/`)                          | `README.md`, `references/ALL_ENDPOINTS_REFERENCE.md`                               |
| Entities or DTOs (`entity/`, `dto/`)                          | `README.md`, `references/ALL_ENDPOINTS_REFERENCE.md`                               |
| Security / config (`config/`, `SecurityConfig`)               | `README.md`, `references/ANGULAR_SECURITY_GUIDE.md`                                |
| Frontend components / routing (`frontend/src/`)               | `frontend/README.md`, `references/ANGULAR_FRONTEND_DETAILED_PLAN.md`               |
| Backend tests (`backend/src/test/`)                           | `tests/README.md`                                                                  |
| Scripts (`scripts/`)                                          | `scripts/README.md`                                                                |
| Docker / Keycloak / CI (`docker-compose*.yml`, `Jenkinsfile`) | `README.md`, `references/JENKINS_SETUP.md`, `references/JENKINS_PIPELINE_GUIDE.md` |
| Maven dependencies (`pom.xml`)                                | `README.md` (tech stack section)                                                   |

### Step 3 — Read source files and documentation

Read the relevant source files and their corresponding documentation files. Compare them
carefully to identify specific inaccuracies, missing sections, outdated information, or
undocumented changes.

### Step 4 — Update documentation

For each documentation file that needs updating:

1. Read the current content of the documentation file.
2. Apply precise, minimal edits using the `edit` tool:
   - Correct outdated information.
   - Add documentation for new endpoints, fields, components, or behaviours.
   - Remove references to deleted code.
   - Keep existing style, tone, and formatting conventions.
3. Do **not** rewrite entire files — make targeted, surgical changes only.
4. Do **not** modify documentation unrelated to the recent code changes.

### Step 5 — Create a pull request

If any documentation files were updated, create a pull request:

- **Title**: A concise description of what was updated, e.g.
  `[docs] Update endpoint reference for new QuoteController changes`
- **Body**: List the documentation files changed, briefly explain what was updated and
  why (linking to the relevant code changes), and note any areas that may need manual
  human review.

If no documentation is out of sync with recent changes, do **not** create a pull request.
Simply output a brief summary explaining that all documentation is up to date.

## Guidelines

- Be accurate: only update documentation when you have confirmed the source code has
  actually changed in a way that makes the documentation incorrect or incomplete.
- Prefer clarity over completeness: a short, accurate doc is better than a long,
  speculative one.
- Never invent API behaviour, field names, or endpoints — always verify against actual
  source code.
- If you are uncertain whether a change is significant enough to warrant a doc update,
  err on the side of updating.

