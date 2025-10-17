# aGrep Modernization Integration Plan

This document reconciles the previously proposed modernization tasks and
establishes an execution order that keeps the codebase building and testable at
each step. It highlights cross-task dependencies to avoid conflicts while
bringing the app up to current Android requirements.

## Compatibility Overview

| Task | Key Dependencies | Notes on Compatibility |
| ---- | ---------------- | ---------------------- |
| Gradle/AndroidX migration | Baseline for all other work | Required before scoped storage, FileProvider, coroutine refactors, or UI updates. Establishes AndroidX APIs and build tooling that later changes rely on. |
| Scoped storage implementation | Requires Gradle/AndroidX migration | Uses AndroidX `ActivityResultContracts` and `DocumentFile`, available only after the migration. Must consider async refactor for clean threading but can land before it. |
| FileProvider adoption | Requires Gradle/AndroidX migration | Touches manifest and sharing flows. Independent of scoped storage but easier to validate once document URIs exist. |
| AsyncTask/ProgressDialog refactor | Requires Gradle/AndroidX migration | Benefits from storage changes exposing async flows; should happen after storage work to avoid rewriting the threading layer twice. |
| Settings and home UI modernization | Requires Gradle/AndroidX migration and scoped storage foundation | Needs new storage picker contract and coroutine-based data flow to bind UI state. |
| Charset detector update | Can occur any time after Gradle/AndroidX migration | Build-system changes (CMake/Gradle) rely on the new project structure but otherwise isolated. |

Key compatibility considerations:

* **Manifest coordination**: Merge the `android:exported` updates from the
  Gradle migration with the `FileProvider` authority declaration to avoid
  manifest merge conflicts.
* **Storage APIs**: Scoped storage introduces `Uri`-based file access. The
  AsyncTask replacement and UI refresh should be built against that API surface
  so no legacy `File` assumptions remain.
* **Testing surface**: Introducing ViewModels/coroutines should include unit
  coverage for the new storage traversal and encoding detector to ensure
  regression safety before UI rewrites.

## Recommended Merge Sequence

1. **Gradle/AndroidX migration**
   * Introduce the Gradle-based project structure, AndroidX dependencies, and
     manifest updates (`android:exported`, new themes, minimum SDK decisions).
   * Ensure the legacy storage and threading code still compiles and runs under
     the new build before proceeding.

2. **Charset detector modernization** *(optional parallel track)*
   * Convert the native module to `CMake`/`externalNativeBuild` or swap in the
     pure-Java library.
   * Because this change is self-contained and only depends on the new build
     system, it can merge immediately after the migration while the Java/Kotlin
     work continues.

3. **Scoped storage and runtime access flow**
   * Replace direct `File` access with `DocumentFile`/`ContentResolver` usage
     and persistable URI permissions.
   * Update `Prefs` and internal models to store URIs so downstream refactors
     consume the new data shapes.
   * Maintain temporary compatibility layers (e.g., converting legacy paths) to
     avoid regressing functionality during rollout.

4. **FileProvider adoption for external viewing**
   * Introduce the manifest provider entry and shared cache directory helpers.
   * Adjust intent flows to use `content://` URIs, ensuring they work with both
     legacy `File`-based paths (if any remain) and the new scoped-storage URIs.
   * Validate coexistence with scoped storage to prevent duplicate file copy
     operations.

5. **Background processing refactor**
   * Replace `AsyncTask`/`ProgressDialog` with `ViewModel` + coroutines (or
     `WorkManager`) now that storage APIs surface non-blocking streams.
   * Align the new coroutine flows with scoped storage by using suspendable I/O
     helpers introduced earlier.
   * Add tests for cancellation, progress, and error handling against the new
     storage layer.

6. **Settings and home UI modernization**
   * Rebuild the settings screen with `PreferenceFragmentCompat` and update the
     main UI to Material components.
   * Wire UI state to the coroutine-backed ViewModel introduced in step 5 and
     to the storage contracts from step 3.
   * Finalize dark mode, accessibility, and Activity Result API integrations.

## Integration Practices

* **Separate PR per task**: Create and merge one pull request for each
  modernization task, aligning the merge order with the "Recommended Merge
  Sequence" above. Avoid batching unrelated steps so reviewers can validate the
  app after every milestone and revert cleanly if a regression appears.
* **Migration checkpoints**: After each merge, tag the repository and attach a
  smoke-test checklist (build, install, basic search) to catch regressions.
* **Data migration testing**: For steps 3–6, verify upgrades from the pre-
  migration release by installing an old build, populating preferences, and then
  sideloading the new APK.
* **Continuous CI updates**: Update CI workflows (Gradle wrapper, emulator API
  level) during step 1 and expand automated tests as new coroutine-based logic
  arrives.

Following this order keeps dependent changes compatible while delivering
incremental value and minimizing long-lived branches.

## Pre-release Checklist

Before shipping to QA or the Play Store, ensure that:

1. The Gradle project syncs cleanly in the latest Android Studio preview and
   releases, confirming no reliance on deprecated build tools.
2. CI builds produce universal APKs or Android App Bundles that pass baseline
   automated smoke tests (unit, instrumentation, and lint).
3. Release notes capture the storage permission changes so existing users are
   prepared to re-select directories after the scoped-storage migration.
4. Signed artifacts are uploaded to internal testing tracks for manual
   verification on API levels 24 through the current target SDK.
5. The release branch merges cleanly back to `main` with version metadata and
   changelog updates captured in source control.
