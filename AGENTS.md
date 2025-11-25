# Repository Guidelines

## Project Scope
- `CSE731-project-details-T1-2025-26.pdf` is the single source of requirements and testing expectations.
- Add course project code here (~1000 LOC as per the brief) and keep documentation and test artifacts in-repo.

## Project Structure & Module Organization
- `src/` — application code grouped by feature or layer (e.g., `src/api/`, `src/core/`).
- `tests/` — automated tests mirroring `src/` (unit, integration, and tool-driven tests such as fuzz/mutation).
- `docs/` — supplemental notes (test plans, screenshots, coverage reports) referenced in the course submission README.
- `scripts/` or `tools/` — helper utilities (data generators, CI hooks).
- Keep the PDF untouched; add any derived notes under `docs/`.

## Build, Test, and Development Commands
- Add a `Makefile` or `package.json` scripts for repeatable workflows:
  - `make lint` (or `npm run lint`) — static checks/formatting.
  - `make test` (or `python -m pytest tests` / `npm test`) — run the suite.
  - `make coverage` — generate coverage reports for the submission packet.
- Use isolated environments (`python -m venv .venv` or `nvm use`).

## Coding Style & Naming Conventions
- Python: 4-space indent, snake_case functions/vars, PascalCase classes; use `black` + `ruff`.
- JavaScript/TypeScript: 2-space indent, camelCase functions/vars, PascalCase classes; use `eslint` + `prettier`.
- Tests mirror code paths: `tests/test_<module>.py` or `tests/<module>.spec.ts`.
- Keep functions small; comment only to capture intent.

## Testing Guidelines
- Choose frameworks per language (e.g., `pytest`, `jest`/`mocha`, `JUnit`) and note the choice in `docs/README.md`.
- Target ≥80% line/branch coverage; add mutation/fuzz reports when your selected technique requires it.
- Name tests by intent (e.g., `test_login_bypass_validation`) and keep fixtures under `tests/fixtures/`.
- Store tool configs (e.g., `stryker.conf.js`, `pytest.ini`) in repo; commit generated reports only when required.

## Commit & Pull Request Guidelines
- Use Conventional Commits (`feat:`, `fix:`, `test:`, `chore:`, `docs:`) with concise scope.
- Each PR should include a summary, linked issue/task, checklist of added tests, and before/after evidence (logs, screenshots, or report paths).
- Keep changes focused; split large test additions from refactors to simplify review.
- Confirm `make lint` and `make test` locally before requesting review.

## Security & Configuration Tips
- Do not commit secrets; use `.env` with a checked-in `.env.example`.
- Sanitise captured logs or session data before adding to `docs/`.
- Note any third-party tools or AI assistance used, per the course brief.
