# aGrep Quality Improvement Plan

This plan consolidates the maintenance priorities that keep the Storage Access Framework (SAF) search experience reliable and fast while expanding automated coverage. It surfaces the bug fixes, performance optimizations, and test investments required to stabilize recent modernization work without requiring contributors to jump between multiple roadmap documents.

## Bug Fix and Reliability Backlog
1. **Finalize the AndroidX/Gradle migration** so manifest updates (e.g., `android:exported`) and theme changes land cleanly without blocking downstream work. This keeps the project buildable for follow-on fixes and testing infrastructure updates.
2. **Harden storage transitions around scoped storage and FileProvider** by aligning the new `DocumentFile`-based storage flow with the FileProvider authority to avoid regressions when sharing search results or upgrading users from legacy path-based preferences.
3. **Refactor background work off `AsyncTask`/`ProgressDialog`** onto ViewModel/coroutines so searches remain responsive and cancellation/error handling is testable once the storage changes are in place.
4. **Track upgrade verification after each migration step** by tagging releases, running smoke tests, and validating preference/data migrations from pre-modernization builds.

## Performance Optimization Priorities
Focus on SAF-specific slowdowns that surfaced after the migration:
1. **Reuse costly components and skip redundant work** by caching `UniversalDetector` instances and short-circuiting encoding detection for known text formats.
2. **Tighten extension filtering** by preparing hash-based lookups and pre-filtering queries so traversal avoids unnecessary file handling.
3. **Stage higher-effort wins** like metadata caching, batched SAF queries, and parallel file processing once correctness fixes land, balancing complexity with measurable speedups.
4. **Instrument key metrics** (time to first result, traversal vs. processing time, encoding overhead) to verify progress across optimization phases.

## Testing and Verification Roadmap
Extend coverage alongside the fixes above using a phased test plan:
1. **Phase 1 (Pure logic)** – Add ~40-50 unit tests for core helpers like `CheckedString`, search pattern utilities, and comparators that have no Android dependencies.
2. **Phase 2 (Search plumbing)** – Test request/response models and the `ExecutorGrepEngine` extension handling to protect refactors to filtering and encoding paths.
3. **Phase 3 (ViewModels and settings)** – Introduce tests for ViewModel logic and preference helpers once coroutine-based execution is in place.
4. **Phase 4 (Performance validation)** – Layer on targeted performance tests (e.g., detector benchmarks) and ensure total suite execution stays under the documented thresholds.
5. **Success metrics** – Track coverage goals (40% → 75%), keep tests deterministic, and adhere to the Arrange-Act-Assert structure to support long-term maintainability.

## Coordination Notes
- Update the plan as each migration milestone lands to keep the bug fix backlog and test phases aligned with current code paths.
