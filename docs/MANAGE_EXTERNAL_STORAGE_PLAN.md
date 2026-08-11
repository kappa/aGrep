# MANAGE_EXTERNAL_STORAGE Implementation Plan

## Problem

aGrep Again uses the Storage Access Framework (SAF) for all file access.
SAF incurs double IPC roundtrips per file operation (directory listing +
content read), making searches 25-50× slower than direct `java.io.File`
access. GitHub Issue #11 reported this as "slow search on Android 14."

## Background: git history

The original aGrep (jiro-aqua, 2010–2014) used direct `java.io.File` for
everything — `File.listFiles()`, `FileInputStream`, recursive
`grepDirectory(File dir)`. This is the fastest path on Android.

Commit `0cd11a8607` ("Migrate storage access to SAF and modern
permissions", Oct 2025) replaced all `File` access with
`DocumentFile`/`ContentResolver`. Interestingly, that commit *already*
added `MANAGE_EXTERNAL_STORAGE` to the manifest and had a full permission
request flow (`isAllFilesMode()`, `showManageStorageDialog()`), but the
app still used SAF for the actual I/O even when the permission was
granted.

The later ViewModel refactor (`ca8b18a598`) extracted the grep into
`ExecutorGrepEngine` but dropped `MANAGE_EXTERNAL_STORAGE` from the
manifest, leaving only SAF.

## Solution: dual-engine architecture

Add a `DirectFileGrepEngine` that uses `java.io.File` / `FileInputStream`
(essentially restoring the original grep logic), and keep
`ExecutorGrepEngine` (SAF) as a fallback. Route to the right engine at
search time based on whether direct file access is available.

### Why keep SAF at all?

SAF is the only way to access cloud-backed storage providers (Google
Drive, Dropbox, OneDrive) and USB OTG devices via `DocumentsProvider`.
The IPC overhead is noise compared to network latency. The
`content://` → `file://` resolution only works for
`com.android.externalstorage.documents` (local volumes).

### Permission matrix

| API level | Permission | Access method |
|-----------|-----------|---------------|
| 30+ (R)   | `MANAGE_EXTERNAL_STORAGE` | Direct `java.io.File` I/O |
| 29 (Q)    | `READ_EXTERNAL_STORAGE` + `requestLegacyExternalStorage=true` | Direct `java.io.File` I/O |
| 24–28     | `READ_EXTERNAL_STORAGE` | Direct `java.io.File` I/O |

All API levels get the fast path. SAF is only used for non-local URIs.

### URI resolution

Existing user directories are stored as SAF tree URIs like:
```
content://com.android.externalstorage.documents/tree/primary:Documents
```

These can be decoded to filesystem paths:
- `primary:Documents` → `/storage/emulated/0/Documents`
- `XXXX-XXXX:path` → `/storage/XXXX-XXXX/path` (SD card)

Non-local providers (authority ≠ `com.android.externalstorage.documents`)
return `null` and stay on SAF.

### Play Store review

Google explicitly lists **"on-device file search"** as an approved use
case for `MANAGE_EXTERNAL_STORAGE`. The app already declares file access
in its data safety form. A brief explanation in the Play Console
declaration should be sufficient.

## Edge cases from original code

Comparing the original `grepFile(File)` (commit `8eb6f8a5dd`) with the
current `ExecutorGrepEngine.processDocument(DocumentFile)`:

1. **UniversalDetector API**: Original used jiro-aqua's native
   UniversalDetector with `getCharset()`, `destroy()`, and a
   `DetectorException` catch. Current code uses juniversalchardet
   (`com.googlecode.juniversalchardet:1.0.3`) with
   `getDetectedCharset()`, no `destroy()`, no `DetectorException`.
   **→ New engine should follow juniversalchardet API (as
   ExecutorGrepEngine already does).**

2. **Extension matching**: Original had a bug — if no extensions were
   enabled (`!extok` but no `hasEnabled` check), it would skip every
   file. `ExecutorGrepEngine.matchesExtension()` correctly returns
   `!hasEnabled || allow`.
   **→ Keep the fixed version.**

3. **GrepView.Data constructor**: Original used `Data(File, int, String)`.
   Current uses `Data(Uri, String, int, String)` with a displayPath.
   **→ New engine uses `Uri.fromFile(file)` + display path, matching the
   current Data model.**

4. **Progress reporting**: Original batched matches per file and flushed
   on first 10 matches eagerly, then batch-at-end. Current
   `ExecutorGrepEngine` does the same.
   **→ Preserve this behavior.**

5. **Null safety**: Original didn't check `listFiles()` returning null
   on permission-denied directories. Current code does.
   **→ Keep null checks.**

6. **Resource cleanup**: Original had inconsistent `close()` in nested
   try blocks. Current uses try-finally.
   **→ Keep try-finally pattern.**

7. **Symlink handling**: Neither original nor SAF code handles symlinks
   explicitly. `File.listFiles()` follows symlinks by default, which
   could cause infinite loops on circular symlinks.
   **→ Low risk on Android (no circular symlinks in /storage), but
   document as a known limitation. Consider adding a visited-set for
   canonical paths in a future PR if reports come in.**

8. **Hidden files/directories**: Both original and SAF enumerate
   everything including dotfiles. This matches grep behavior.
   **→ Keep as-is.**

## Implementation plan

### Phase 1: Plan + infrastructure (this PR)
- This document
- Checkvist task breakdown

### Phase 2: Permission plumbing
- Add `MANAGE_EXTERNAL_STORAGE` to `AndroidManifest.xml`
- Add `requestLegacyExternalStorage=true` to `<application>`
- Add `StorageUtils.java`: `hasDirectFileAccess()`, `treeUriToFile()`
- Add permission request UI in `Search.java`
- Unit tests for `StorageUtils.treeUriToFile()` (various URI formats,
  edge cases, non-local providers)

### Phase 3: Direct grep engine
- Add `DirectFileGrepEngine.java` implementing `GrepEngine`
- Extract shared `matchesExtension()` to a utility (currently duplicated
  in `ExecutorGrepEngine`)
- Wire up engine selection in `SearchViewModel.initializeEngine()`
- Add `reinitializeEngine()` for permission changes

### Phase 4: Testing + Play Store
- Instrumented test: direct engine on local storage
- Instrumented test: SAF engine on cloud provider (if feasible)
- Benchmark: compare DirectFileGrepEngine vs ExecutorGrepEngine on same
  corpus (extend existing `SAF_SEARCH_PERFORMANCE_PLAN` benchmark
  harness)
- Update data safety form / MANAGE_EXTERNAL_STORAGE declaration in Play
  Console
- Submit for review

### Phase 5: Multi-backend routing (future)
- Detect `file://` vs `content://` per directory at search time
- Allow mixed engines in a single search (some dirs direct, some SAF)
- Add a filesystem-based directory browser as an alternative to SAF
  picker when `MANAGE_EXTERNAL_STORAGE` is granted
- This is the "universal grep" vision — SAF for cloud, direct for local

## Files to create/modify

```
New:
  app/src/main/java/dev/kappa/agrep_again/StorageUtils.java
  app/src/main/java/dev/kappa/agrep_again/DirectFileGrepEngine.java
  app/src/test/java/dev/kappa/agrep_again/StorageUtilsTest.java

Modified:
  app/src/main/AndroidManifest.xml
  app/src/main/java/dev/kappa/agrep_again/SearchViewModel.java
  app/src/main/java/dev/kappa/agrep_again/Search.java
  app/src/main/java/dev/kappa/agrep_again/ExecutorGrepEngine.java (extract matchesExtension)
```

## Open questions

1. **String resources**: The SAF migration commit added
   `label_manage_storage_title` / `label_manage_storage_message` strings
   that were later removed. Re-add them or write new ones?

2. **`isAllFilesMode()` heuristic**: The SAF migration had a check for
   whether any directory URI ended with `:` (volume root). Do we need
   this, or should we always prefer direct access when the permission is
   available?
   **Recommendation**: always prefer direct when available. Simpler,
   and the user already granted the permission.

3. **versionCode bump**: Should Phase 2 (permission only) get its own
   release, or wait until Phase 3 (engine) is done?
   **Recommendation**: ship permission + engine together. No point
   requesting a permission you don't use yet.
