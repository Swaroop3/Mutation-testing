# E-commerce Cart & Checkout (Mutation Testing Project)

## Overview
- Java 17 implementation of a cart/checkout pipeline with pricing, promotions, tax, shipping, inventory, payments, and order persistence.
- Targeted for mutation-testing using PIT.
- Repository link: [REPO LINK](https://github.com/Swaroop3/Mutation-testing).

## Project Structure
- `src/main/java/com/example/shop/` — domain code (catalog, cart, pricing, promo, checkout, payment, order, util).
- `src/test/java/com/example/shop/` — JUnit tests covering unit and integration flows.
- `diagrams/` — DOT sources for diagrams.
- `docs/screenshots/` — test summary, PIT summary/survivors, class/sequence diagrams.
- `target/pit-reports/` — latest PIT HTML and text summaries (`mutation_summary.txt`, `mutations_*.txt`).

## Build & Run
- Build & tests: `mvn test`
- Mutation testing (PIT): `mvn org.pitest:pitest-maven:mutationCoverage`
  - Reports: open `target/pit-reports/index.html`; text summaries in `target/pit-reports/mutation_summary.txt`.
- Java version: 17; dependencies managed via Maven (`pom.xml`).

## Testing & Mutation Strategy
- Framework: JUnit; PIT with explicit operators (unit-oriented: conditionals boundary, negate conditionals, math; integration/contract: void-method-call, null/empty returns, boolean return mutators).
- Test focus:
  - Unit: pricing math, promo eligibility/stacking, tax/rounding, shipping thresholds, money arithmetic.
  - Integration: cart limits/stock checks, checkout success/failure paths, inventory reserve/commit/release, payment outcomes, order persistence.
- Coverage: ≥90% line coverage on mutated classes; mutation score: 68%, survivors documented in reports for follow-up.
- Snapshots: `docs/screenshots/test-summary.png`, `docs/screenshots/pit-summary.png`, `docs/screenshots/pit-survivors.png`, `docs/screenshots/class_diagram.png`, `docs/screenshots/checkout_sequence.png`.

## Mutation Results (PIT)
- Score: 68% (line coverage on mutated classes: 431/478 ≈90%).
- Unit-oriented operators:
  - ConditionalsBoundaryMutator: gen 25 / killed 9 / survived 16
  - NegateConditionalsMutator: gen 64 / killed 54 / survived 6 / no-cov 4
  - MathMutator: gen 7 / killed 7 / survived 0
  - PrimitiveReturnsMutator: gen 4 / killed 3 / no-cov 1
- Integration/contract operators:
  - VoidMethodCallMutator: gen 16 / killed 8 / survived 8
  - NullReturnValsMutator: gen 84 / killed 65 / survived 2 / no-cov 17
  - EmptyObjectReturnValsMutator: gen 36 / killed 18 / survived 1 / no-cov 17
  - BooleanTrueReturnValsMutator: gen 17 / killed 7 / survived 5 / no-cov 5
  - BooleanFalseReturnValsMutator: gen 11 / killed 9 / no-cov 2
- Totals: gen 264 / killed 180 / survived 33 / no-cov 46

## How to Reproduce
1) Ensure JDK 17 and Maven are installed.
2) `mvn test`
3) `mvn org.pitest:pitest-maven:mutationCoverage`
4) Review `target/pit-reports/index.html` (or the text summaries).

## Tooling & Configuration
- PIT version: 1.15.0
- JUnit5 

## LLM/AI Usage
- Codebase creation was assisted by an LLM (OpenAI ChatGPT) under our supervision. Final logic, configuration, and test intent were validated and adjusted manually.

## Contributions
- Swaroop(IMT2022587): pricing/tax/shipping modules, promo engine, Money/utilities, unit tests for pricing/promos, diagrams, README/report.
- Lokesh(IMT2022577): cart/checkout, inventory/payment/order modules, integration tests, PIT setup, README/report.
