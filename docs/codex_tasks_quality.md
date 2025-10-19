Codex formulated a bunch of tasks that it cannot for some reason save
into a file. Here they are:

# Harden search cancellation handling

Cancel requests currently still result in onComplete callbacks, so the UI flashes a “Finished” toast even when the user stops a search mid-run.

1. Update `ExecutorGrepEngine.search` and its helper methods (`processDocumentTree` / `processDocument`) to short-circuit without calling `ProgressCallback.onComplete` when `cancelled.get()` becomes `true` (`app/src/main/java/jp/sblo/pandora/aGrep/ExecutorGrepEngine.java`).
2. In `SearchViewModel`, guard `onComplete`/`onError` callbacks so that once `cancelSearch()` runs, late callbacks are ignored or mapped to a new “Canceled” UI state (`app/src/main/java/jp/sblo/pandora/aGrep/SearchViewModel.java`).
3. Add a unit test that triggers `cancelSearch()` mid-run (use a fake `GrepEngine`) and assert that the UI state ends in “idle/canceled” without the completion toast (`app/src/test/...`).

# Null-safe query extraction in Search activity

Search.onCreate dereferences extras without a null check; ACTION_SEARCH intents without extras (voice queries, deep links) would crash the activity.

1. In `Search.onCreate`, replace the direct `extras.getString(...)` call with `it.getStringExtra(...)` and bail out gracefully if the query is empty (`app/src/main/java/jp/sblo/pandora/aGrep/Search.java`).
2. Show a user-friendly error toast and `finish()` when no query is supplied.
3. Add an instrumentation test that launches `Search` with an ACTION\_SEARCH intent lacking extras and verifies the activity exits without crashing (`app/src/androidTest/...`).

# Modernize TextViewer file loading

The TextViewer still uses AsyncTask and builds an in-memory list of all lines, which is both deprecated and memory-heavy for large files.

1. Replace `TextLoadTask` with a lifecycle-aware coroutine or `ViewModel` that streams lines via `Flow`/`LiveData`, emitting batched updates to the adapter (`app/src/main/java/jp/sblo/pandora/aGrep/TextViewer.java`).
2. Rework `TextPreview.Adapter` usage to accept incremental updates (e.g., `ListAdapter` + `DiffUtil`) so the UI stays responsive for large files (`app/src/main/java/jp/sblo/pandora/aGrep/TextPreview.java`).
3. Add tests that exercise large content sources and verify cancellation/rotation handling.

# Optimize per-file work in ExecutorGrepEngine

The engine creates a new UniversalDetector and scans the entire extension list for every file, matching the bottlenecks called out in the existing performance plan.

1. Precompute enabled extensions into a `HashSet` at the start of `search` and pass the cache down so `matchesExtension` becomes O(1) (`app/src/main/java/jp/sblo/pandora/aGrep/ExecutorGrepEngine.java`).
2. Promote `UniversalDetector` to a reusable field or pool; reset it per file instead of re-instantiating.
3. Optionally short-circuit detection for known UTF-8 text extensions per the performance plan, guarding with a fallback for other types.
4. Capture before/after benchmarks (e.g., log duration statistics exposed via `SearchSummary`) and document the win in `docs/`.

# Avoid main-thread resorting of large result sets

After completion, Search resorts all matches on the UI thread, which can freeze the activity for big searches.

1. Shift the final sort into the engine or ViewModel so `Search` receives already-ordered batches (`app/src/main/java/jp/sblo/pandora/aGrep/ExecutorGrepEngine.java`, `SearchViewModel.java`).
2. Alternatively, switch the adapter to `ListAdapter` with `DiffUtil` and post sorted lists via `LiveData`, ensuring updates happen on a background dispatcher.
3. Validate UI responsiveness with an Espresso test that inserts hundreds of matches and measures frame timing (`app/src/androidTest/...`).

# Expand automated coverage for preferences and search state

Critical flows—directory migration flags, recent queries, and ViewModel progress—currently have no regression tests.

1. Introduce JVM unit tests covering `Prefs.loadPrefes`/`savePrefs`, ensuring legacy directory migration flags behave correctly (`app/src/test/java/...`).
2. Add tests for `SearchViewModel` that simulate progress, completion, error, and cancellation callbacks using a fake `GrepEngine`.
3. Integrate these tests into CI and document the scenarios in the testing checklist.
