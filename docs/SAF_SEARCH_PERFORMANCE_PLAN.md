# SAF Search Performance Improvement Plan

## Current Understanding
- The migration to the Storage Access Framework (SAF) forces the search engine to enumerate directory trees through `DocumentFile`, fetch file contents via the `ContentResolver`, and detect character encoding per file before scanning.
- Directory selections are persisted as tree URIs in shared preferences (`Prefs.mDirList`), so recreating user datasets requires seeding those URIs.
- SAF interaction currently runs on a single worker, amplifying latency introduced by IPC and per-file setup costs.

## Objectives
1. Produce reproducible benchmarks for end-to-end SAF searches.
2. Add visibility into per-stage costs across the search pipeline.
3. Microbenchmark individual hotspots to validate optimization candidates.
4. Prototype and evaluate optimizations safely before enabling them by default.
5. Guard against regressions by adding automated performance checks.

## Workstream 1: Reproducible SAF Benchmark Harness
- Add an `androidTest` harness under `app/src/androidTest/java/jp/sblo/pandora/aGrep/perf`.
- Provide a deterministic dataset by copying a zipped corpus from `androidTest/assets` into `Context.getExternalFilesDir`, exposed via a debug-only `TestDocumentProvider` (`jp.sblo.pandora.aGrep.test.provider`).
- Seed `Prefs` with the provider's tree URI (using `DocumentsContract.buildTreeDocumentUri`) to mirror the persisted `Prefs.KEY_TARGET_DIRECTORIES_TREE` structure.
- Implement an instrumented test (`ExecutorGrepEngineBenchmarkTest`) that submits a `SearchRequest`, waits for completion, and records metrics like total duration, time-to-first-match, files visited, and matches found.
- Document `adb`/Gradle commands (e.g., `./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=...`) for running the benchmark locally and in CI.

## Workstream 2: Pipeline Instrumentation & Tracing
- Introduce a `SearchMetrics` helper to collect timestamps and counters for directory traversal, file opens, charset detection, and line scanning.
- Wrap directory enumeration in `processDocumentTree` with `Trace.beginSection`/`endSection` calls and metric updates to expose SAF IPC overhead in Perfetto traces.
- Measure per-file latency in `processDocument` around `contentResolver.openInputStream`, the UniversalDetector pass, and the line-reading loop. Log aggregated stats to Logcat (debug builds) and feed them back to the benchmark harness.
- Extend `SearchSummary` to carry aggregated timing results that the benchmark harness can persist.
- Update developer docs with guidance for capturing Perfetto traces and interpreting the new trace markers.

## Workstream 3: Microbenchmarks for Hotspots
- Add a dependency on `androidx.benchmark:benchmark-junit4` in the `androidTest` module.
- Implement `UniversalDetectorBenchmark` to compare new vs. reused detector instances on captured byte buffers stored under `androidTest/resources`.
- Implement `ExtensionFilterBenchmark` to evaluate the current extension-matching loop versus a precomputed `HashSet` lookup with randomized file name samples.
- Implement `PatternScanBenchmark` to measure regex throughput across representative query patterns and line lengths.
- Track benchmark medians and integrate them into the performance dashboard alongside the end-to-end SAF benchmark results.

## Workstream 4: Flagged Optimization Prototypes
- Create a debug-only `SearchOptimizationFlags` configuration and inject it into `ExecutorGrepEngine` to toggle alternative implementations (e.g., detector reuse, HashSet extension filtering, directory caching).
- Guard new code paths in `processDocument` and `matchesExtension` with the flags so behavior is unchanged unless explicitly enabled.
- Extend the benchmark harness to iterate through flag combinations and collect comparative metrics.
- Promote proven optimizations to the default configuration and update automated tests accordingly.

## Workstream 5: Automated Regression Detection
- Add a Gradle task (e.g., `connectedSearchPerf`) that executes the benchmark suite on Firebase Test Lab or a dedicated device farm populated with the benchmark dataset.
- Parse benchmark output into JSON summaries (duration, throughput) and upload them as CI artifacts.
- Implement a comparison script that diffs current metrics against the baseline and fails the workflow when latency regresses beyond the agreed threshold.
- Document the CI workflow so developers can trigger ad-hoc performance runs prior to merging major SAF changes.
