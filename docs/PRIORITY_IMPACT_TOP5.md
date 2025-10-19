# Top 5 Tasks by Impact Only

This list prioritizes tasks based purely on their impact to users, regardless of implementation difficulty.

## ⚠️ Test First - May Already Be Fixed

### 1. SD Card Access on Android 12+ (CRITICAL - VERIFY STATUS)

**Impact**: ⭐⭐⭐⭐⭐ (HIGHEST - App-breaking for major user segment)

**Status**: **POSSIBLY ALREADY FIXED** by recent SAF migration (PRs #2, #3, #4, #7)

**Source**:
- `playstore_reviews_international.md` - Japanese users explicitly report "Cannot select SD card on Android 12. Unusable."
- `playstore_reviews_action_plan.md` - Multiple reports of external SD/OTG access issues
- `modernization-plan.md` - Step 3 (Scoped storage implementation) was completed

**Why High Impact**:
- App is **completely unusable** for SD card users on Android 12+ if still broken
- Japanese market (40 reviews) heavily impacted - SD cards very common in Japan
- Primary use case (searching files on SD card) completely blocked

**ACTION REQUIRED FIRST**:
1. ✅ **TEST on Android 12+ emulator with virtual SD card**
2. ✅ **TEST on Android 13+ emulator with virtual SD card**
3. ✅ **TEST OTG USB drive access**
4. ✅ **Document test procedure for future verification**
5. If working: Remove from critical list and document as resolved
6. If broken: Investigate why SAF didn't solve it

**Testing Instructions Needed**:
- Create step-by-step guide using Android Studio emulator virtual storage configurations
- Test directory selection UI with SD card mounted
- Test actual file search on SD card storage
- Test persistence of SD card directory selections across app restarts

**Files to Check**:
- `app/src/main/java/jp/sblo/pandora/aGrep/FileSelector.java`
- `app/src/main/java/jp/sblo/pandora/aGrep/ExecutorGrepEngine.java`

---

## Critical Tasks (Confirmed Issues)

### 2. Create Performance Benchmarking Infrastructure

**Impact**: ⭐⭐⭐⭐⭐ (HIGHEST - Enables ALL optimization decisions)

**Status**: **URGENT** - Currently flying blind on performance

**Why This Is #2**:
- **Without measurements, we're guessing** on optimization priorities
- Performance doc claims 10-15%, 15-20% improvements but **no validation**
- SAF migration introduced known performance regression - **need baseline metrics**
- **Blocks confident optimization work** - can't know what actually helps
- Every claimed optimization needs before/after proof

**What to Measure**:
1. **Time to first result** (user-perceived responsiveness)
2. **Total search time** (overall performance)
3. **Files processed per second** (throughput)
4. **Encoding detection overhead** (per-file cost)
5. **SAF IPC overhead** (DocumentFile vs File comparison)
6. **Memory usage** (peak and average during search)

**Benchmark Suite Requirements**:
- Reproducible test corpus (known directory structure, file sizes, content)
- Automated benchmark runs (can integrate into CI)
- Statistical validity (multiple runs, median/p95 metrics)
- Before/after comparison reports
- Breakdown by operation (traversal, encoding, matching, UI updates)

**Output**:
- Baseline performance metrics for current implementation
- Identify actual bottlenecks (not theoretical ones)
- Validate or refute optimization claims from PERFORMANCE_OPTIMIZATIONS.md
- Guide prioritization of performance work

**Files to Create**:
- `app/src/androidTest/java/jp/sblo/pandora/aGrep/PerformanceBenchmarkTest.java`
- `docs/PERFORMANCE_BASELINE.md` (baseline results)
- `docs/BENCHMARK_METHODOLOGY.md` (how to run benchmarks)

**Estimated Effort**: 🔨🔨🔨 (2-4 hours)

---

### 3. Add Test Coverage for Recent SAF Changes

**Impact**: ⭐⭐⭐⭐⭐ (HIGHEST - Prevents regressions, enables confident development)

**Status**: **URGENT** - Recent extensive changes lack test coverage

**Source**:
- `TEST_COVERAGE_PLAN.md` - Comprehensive plan exists, only 1 test file with 5 tests currently (~1% coverage)
- Recent SAF migration (PRs #2, #3, #4, #7) introduced **known performance regression**
- `quality-improvement-plan.md` - Testing is Phase 1-4 priority

**Why High Impact**:
- **Proof of need**: SAF migration already introduced performance regression that tests would have caught
- Recent extensive changes mean **we don't know what else broke**
- **Blocks confident further development** - can't safely refactor or optimize without tests
- Current coverage: ~1% - need 60-80% for business logic
- Without tests, every fix risks breaking something else

**User Impact**:
- Performance regression already shipped to users (slower searches post-SAF)
- Unknown regressions may exist in: encoding detection, extension filtering, state management
- Future fixes could break working functionality
- Can't optimize performance without regression tests

**What to Test First** (from TEST_COVERAGE_PLAN.md Phase 1 - Pure Logic):
1. `CheckedString.java` - Data class (10 tests, easy)
2. `ExecutorGrepEngine.matchesExtension()` - Extension filtering (12 tests, critical path)
3. `SearchUiState.java` - State management (10 tests, affects all UI)
4. `Prefs.java` - Preference parsing (15 tests, data integrity)

**Success Metrics**:
- Phase 1: 40% coverage of business logic (40-50 tests)
- All tests deterministic (zero flaky tests)
- Tests run in < 5 seconds

**Files Affected**:
- Create: `app/src/test/java/jp/sblo/pandora/aGrep/*Test.java` (multiple new test files)
- Existing: `app/src/test/java/jp/sblo/pandora/aGrep/UniversalDetectorRegressionTest.java`

**Estimated Effort**: 🔨🔨🔨 (2-4 hours for Phase 1)

---

### 4. Fix File Size Limit (~5MB Ceiling)

**Impact**: ⭐⭐⭐⭐⭐ (HIGHEST - Core functionality broken)

**Status**: **NEEDS INVESTIGATION** - May be affected by SAF migration

**Source**:
- `playstore_reviews_action_plan.md` - Issue #3: "Only searches first 10k of files"
- `playstore_reviews_international.md` - Multiple confirmations: Japanese "~5MB", Russian "10 megabyte file gives zero results"
- Confirmed across multiple languages and reviews

**Why High Impact**:
- Core search functionality **silently fails** on moderately-sized files
- Users get **zero results** without warning - confusing and frustrating
- Files >5MB are increasingly common (logs, data exports, compiled documents)
- No indication to user that file was skipped
- **Silent failure creates distrust** in search results

**Investigation Steps**:
1. Search code for hardcoded buffer sizes (5242880 bytes = 5MB)
2. Check UniversalDetector buffer handling
3. Check InputStream read limits in ExecutorGrepEngine
4. Test with files: 1MB, 5MB, 10MB, 50MB, 100MB
5. Determine if limit is:
   - Buffer size (easy fix: increase buffer)
   - Line count (need streaming solution)
   - Memory constraint (need to handle large files differently)

**User Impact**:
- Silent failure creates **distrust** in search results ("Did I miss something?")
- Wastes user time searching files that will never be processed
- Makes app unsuitable for modern workflows (large log files, data analysis)

**Files Affected**:
- `app/src/main/java/jp/sblo/pandora/aGrep/ExecutorGrepEngine.java` (likely hardcoded buffer limit)

**Estimated Effort**: 🔨🔨🔨 (2-4 hours - investigation + fix + testing)

---

### 5. Fix Search Cancellation + Crashes on Large Searches

**Impact**: ⭐⭐⭐⭐⭐ (HIGHEST - Poor UX, potential data loss)

**Status**: **CONFIRMED** - Documented in codex_tasks_quality.md

**Source**:
- `codex_tasks_quality.md` - "Harden search cancellation handling" - flashes "Finished" even when cancelled
- `playstore_reviews_international.md` - Japanese review: "cannot cancel, and sometimes crashes with no response"

**Why High Impact**:
- Cannot cancel = **locked in** to potentially long searches
- Poor UX: "Finished" toast when search was cancelled confuses users
- Crashes mentioned in reviews (though likely obsolete on modern devices with more RAM)
- **Likely worsened by SAF migration** (more IPC overhead = longer searches)

**User Impact**:
- Accidentally search wrong directory? **Can't stop it**
- Large directory selected? **Stuck waiting**
- Background process continues eating CPU/battery even after user tries to cancel
- Confusing UI feedback undermines user trust

**Implementation** (from codex_tasks_quality.md):
1. Update `ExecutorGrepEngine.search` to short-circuit without calling `onComplete` when cancelled
2. Guard `SearchViewModel` callbacks so late callbacks after cancel are ignored
3. Add unit test for cancellation (requires test infrastructure from #3)

**Note on Crashes**:
- Old reviews (10+ years ago) mention crashes
- Modern devices have much more RAM
- Crashes likely obsolete, but cancellation UX still broken

**Files Affected**:
- `app/src/main/java/jp/sblo/pandora/aGrep/ExecutorGrepEngine.java`
- `app/src/main/java/jp/sblo/pandora/aGrep/SearchViewModel.java`
- `app/src/main/java/jp/sblo/pandora/aGrep/Search.java`

**Estimated Effort**: 🔨🔨 (1-2 hours - mostly UI wiring)

---

## Just Missed Top 5

### 6. Fix GitHub Issues (Rotation State Loss, Copy from Results)

**Impact**: ⭐⭐⭐⭐ (HIGH - Users invested effort to report)

**Source**: `github_issues_tasks.md`

**Issues**:
- **#10**: Cannot copy text from search results
- **#4, #5, #6**: State loss on rotation (checkboxes, dialogs reset)
- **#7**: Russian translation (covered by translation infrastructure task)
- **#3**: Dark theme support (separate evaluation)

**Why Important**:
- GitHub issues represent invested user effort - deserve respect
- State loss on rotation is extremely frustrating
- Copy from results affects workflow

**Estimated Effort**:
- Copy from results: 🔨🔨 (1-2 hours)
- State preservation: 🔨🔨🔨 (2-4 hours)

---

## Impact Scoring Methodology

Tasks were scored on:
1. **Severity**: Does it crash, break core functionality, or just annoy?
2. **Frequency**: How many users hit this issue?
3. **User Segment**: Does it affect majority or niche users?
4. **Workaround**: Can users work around it or is it a total blocker?
5. **Trust Impact**: Does it make users distrust the app?
6. **Development Impact**: Does it block other improvements?

## Key Insight: Measurement Before Optimization

The **known performance regression** from SAF migration proves that:
- Extensive changes without tests = hidden regressions
- Performance claims without benchmarks = guesswork
- Users report issues we should have caught in CI
- Can't safely optimize or refactor without regression tests

**Performance benchmarking (#2) and test coverage (#3) are now critical** because they:
- Enable confident development of fixes for other issues
- Prevent shipping new regressions
- Allow performance optimizations based on data, not guesses
- Provide documentation of expected behavior

## What Didn't Make Top 5 (But Is Still Important)

### UI/UX Non-Intuitiveness
- `playstore_reviews_action_plan.md` - 7 reviews citing confusing UI
- **Impact**: ⭐⭐⭐ - Lower than expected because:
  - No new users expected (app republished under different name)
  - "aGrep" name signals technical users who can figure it out
  - Existing users already know the UI
- **Still worth**: Simple hints like "Long-press to select" cost little

### Performance Optimizations (UniversalDetector cache, encoding skip, etc.)
- `PERFORMANCE_OPTIMIZATIONS.md` - Various optimization ideas
- **Impact**: Unknown until measured (that's why benchmarking is #2)
- All performance work should wait for benchmark infrastructure

### Lint Warnings
- `LINT_WARNINGS_PLAN.md` - 39 warnings, 5 high-priority fixes
- **Impact**: ⭐⭐⭐ - Only matters if pointing to real bugs
- StaticFieldLeak (memory leak), ClickableViewAccessibility (accessibility) are real issues
- Cosmetic warnings (UnusedResources, GradleDependency) can be ignored (NOT suppressed)

### Special Character Crash (Backslash)
- `playstore_reviews_action_plan.md` - Searching for `\` crashes app
- **Impact**: ⭐⭐⭐⭐ - Less frequent than top 5 issues
- **Effort**: 🔨 (30 mins - escape metacharacters when regex off)
