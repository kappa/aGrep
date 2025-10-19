# Test Coverage Improvement Plan for aGrep

## Current Status
- **Existing Tests**: 1 test file with 5 tests (`UniversalDetectorRegressionTest.java`)
- **Coverage**: ~1% of codebase tested
- **Framework**: JUnit 4 already configured
- **Test Location**: `app/src/test/java/jp/sblo/pandora/aGrep/`

## Goals
- Achieve 60-80% code coverage for business logic
- Focus on pure logic first (no Android dependencies)
- Ensure critical paths are tested (search, filtering, parsing)
- Enable confident refactoring and performance optimizations

---

## Phase 1: Pure Logic Tests (No Android Dependencies)

**Priority: HIGH | Estimated: 40-50 tests | Time: 2-4 hours**

These classes have zero Android dependencies and can be tested with simple JUnit tests.

### 1.1 CheckedString.java - Data Class
**File**: `app/src/main/java/jp/sblo/pandora/aGrep/CheckedString.java`

**Tests to Write** (8-10 tests):
```
✓ Constructor with single arg defaults to checked=true
✓ Constructor with two args sets checked state
✓ Constructor with three args sets all fields
✓ Constructor handles null label (falls back to value)
✓ getDisplayName() returns displayName when set
✓ getDisplayName() falls back to string when displayName is null
✓ setDisplayName() updates displayName
✓ hasValue() returns true for non-empty string
✓ hasValue() returns false for empty string
✓ hasValue() returns false for null string
✓ Handles special characters and Unicode
```

**File to Create**: `app/src/test/java/jp/sblo/pandora/aGrep/CheckedStringTest.java`

---

### 1.2 Search.java - Utility Methods
**File**: `app/src/main/java/jp/sblo/pandora/aGrep/Search.java`

**Methods to Extract and Test**:

#### `escapeMetaChar(String pattern)` (Lines 167-179)
**Tests to Write** (6-8 tests):
```
✓ Escapes dot (.) to \\.
✓ Escapes asterisk (*) to \\*
✓ Escapes question mark (?) to \\?
✓ Escapes brackets ([]) to \\[\\]
✓ Escapes all metacharacters: .^${}[]*+?|()\\
✓ Returns empty string for empty input
✓ Returns null for null input
✓ Handles already-escaped characters
```

#### `convertOrPattern(String pattern)` (Lines 181-196)
**Tests to Write** (5-7 tests):
```
✓ Converts "word1 word2" to "(word1|word2)"
✓ Converts "a b c" to "(a|b|c)"
✓ Single word without spaces returns unchanged
✓ Empty string returns empty
✓ Null returns null
✓ Handles multiple consecutive spaces
✓ Handles leading/trailing spaces
```

#### `buildExtensionSet(String[] values)` (Lines 198-206)
**Tests to Write** (4-5 tests):
```
✓ Converts array to lowercase HashSet
✓ Handles duplicates (case-insensitive)
✓ Handles null array
✓ Handles empty array
✓ Handles array with null elements
```

**Action**: Extract these static utility methods to a separate `SearchUtils.java` class for easier testing, OR make them package-private and test via reflection/test utils.

**File to Create**: `app/src/test/java/jp/sblo/pandora/aGrep/SearchUtilsTest.java`

---

### 1.3 GrepView.Data - Comparator Logic
**File**: `app/src/main/java/jp/sblo/pandora/aGrep/GrepView.java` (nested class Data)

**Tests to Write** (6-8 tests):
```
✓ Sorts by displayName (case-insensitive)
✓ Secondary sort by line number when displayName is equal
✓ Handles null displayName
✓ Case-insensitive comparison ("File.txt" == "file.txt")
✓ Sorts correctly: file_a.txt:10 < file_a.txt:20 < file_b.txt:1
✓ Compare returns 0 for identical Data
✓ Compare is transitive (a < b && b < c => a < c)
✓ Unicode filename sorting
```

**File to Create**: `app/src/test/java/jp/sblo/pandora/aGrep/GrepViewDataTest.java`

---

### 1.4 SearchRequest.java - Immutable Data Class
**File**: `app/src/main/java/jp/sblo/pandora/aGrep/SearchRequest.java`

**Tests to Write** (4-6 tests):
```
✓ Constructor initializes all fields
✓ Fields are accessible
✓ Handles null pattern
✓ Handles null prefs
✓ Handles empty query string
✓ Pattern regex is valid
```

**File to Create**: `app/src/test/java/jp/sblo/pandora/aGrep/SearchRequestTest.java`

---

### 1.5 SearchProgress.java - Data Class
**File**: `app/src/main/java/jp/sblo/pandora/aGrep/SearchProgress.java`

**Tests to Write** (4-6 tests):
```
✓ Constructor initializes all fields
✓ newMatches defaults to empty list when null
✓ newMatches is stored correctly
✓ Counters are accessible
✓ Handles negative counters (defensive)
✓ Handles large counter values
```

**File to Create**: `app/src/test/java/jp/sblo/pandora/aGrep/SearchProgressTest.java`

---

### 1.6 SearchSummary.java - Data Class
**File**: `app/src/main/java/jp/sblo/pandora/aGrep/SearchSummary.java`

**Tests to Write** (4-6 tests):
```
✓ Constructor initializes all fields
✓ allMatches defaults to empty list when null
✓ Duration is stored correctly
✓ Handles null query
✓ Handles large match lists
✓ Handles zero duration
```

**File to Create**: `app/src/test/java/jp/sblo/pandora/aGrep/SearchSummaryTest.java`

---

### 1.7 SearchUiState.java - State Factory Methods
**File**: `app/src/main/java/jp/sblo/pandora/aGrep/SearchUiState.java`

**Tests to Write** (8-10 tests):
```
✓ idle() creates state with isSearching=false, all fields null
✓ searching(progress) creates state with isSearching=true
✓ searching(progress) sets progress field
✓ completed(summary) creates state with isSearching=false
✓ completed(summary) sets summary field
✓ error(message) creates state with isSearching=false
✓ error(message) sets errorMessage field
✓ State consistency: only one of (progress, summary, errorMessage) is non-null
✓ Handles null progress in searching()
✓ Handles null summary in completed()
```

**File to Create**: `app/src/test/java/jp/sblo/pandora/aGrep/SearchUiStateTest.java`

---

## Phase 2: Business Logic Tests (Minimal Mocking)

**Priority: HIGH | Estimated: 25-35 tests | Time: 3-5 hours**

### 2.1 ExecutorGrepEngine.java - Extension Matching Logic
**File**: `app/src/main/java/jp/sblo/pandora/aGrep/ExecutorGrepEngine.java`

**Method to Test**: `matchesExtension(String fileName, Prefs prefs)` (Lines 283-306)

**Tests to Write** (10-12 tests):
```
✓ Wildcard "*" matches files WITHOUT extensions (README, Makefile)
✓ Wildcard "*" does NOT match files WITH extensions (file.txt)
✓ Extension "txt" matches "file.txt"
✓ Extension "txt" matches "FILE.TXT" (case-insensitive)
✓ Extension "txt" matches "file.Txt"
✓ Multiple extensions: "txt" and "md" match both
✓ Extension "gz" matches "file.tar.gz" (last extension)
✓ No enabled extensions returns true (accepts all files)
✓ Null fileName returns false
✓ Empty fileName returns false (or matches "*")
✓ fileName without extension matches only "*"
✓ Handles filenames with multiple dots
```

**Setup Required**:
```java
private Prefs createPrefsWithExtensions(String... extensions) {
    Prefs prefs = new Prefs();
    prefs.mExtList = new ArrayList<>();
    for (String ext : extensions) {
        prefs.mExtList.add(new CheckedString(true, ext));
    }
    return prefs;
}
```

**File to Create**: `app/src/test/java/jp/sblo/pandora/aGrep/ExecutorGrepEngineTest.java`

---

### 2.2 Prefs.java - Preference Parsing Logic
**File**: `app/src/main/java/jp/sblo/pandora/aGrep/Prefs.java`

**Methods to Test** (requires mocking `SharedPreferences`):

#### `loadExtensions(SharedPreferences)` (Lines 157-173)
**Tests to Write** (6-8 tests):
```
✓ Parses pipe-delimited format: "true|txt|false|md" → ["txt" (checked), "md" (unchecked)]
✓ Handles empty preference → defaults to "txt"
✓ Handles odd number of pipe-separated values
✓ Handles malformed input gracefully
✓ Preserves checked state correctly
✓ Trims whitespace from extensions
✓ Handles null SharedPreferences
```

#### `loadDirectories(SharedPreferences)` (Lines 127-155)
**Tests to Write** (8-10 tests):
```
✓ Parses JSON tree format correctly
✓ Extracts checked state from JSON
✓ Falls back to legacy KEY_TARGET_DIRECTORIES_NEW format
✓ Falls back to KEY_TARGET_DIRECTORIES_OLD format
✓ Handles migration flag
✓ Handles empty/null directory list
✓ Handles malformed JSON gracefully
✓ Preserves displayName from JSON
```

#### `clearLegacyDirectories()` (Lines 175-181)
**Tests to Write** (3-4 tests):
```
✓ Removes entries where hasValue() == false
✓ Keeps entries where hasValue() == true
✓ Clears migration flags
✓ Handles empty list
```

**Setup Required**: Use Mockito to mock `SharedPreferences`
```gradle
// Add to app/build.gradle dependencies if not present
testImplementation 'org.mockito:mockito-core:5.+'
```

**File to Create**: `app/src/test/java/jp/sblo/pandora/aGrep/PrefsTest.java`

---

## Phase 3: Integration & ViewModel Tests (Android Testing)

**Priority: MEDIUM | Estimated: 15-25 tests | Time: 4-6 hours**

### 3.1 SearchViewModel.java - State Management
**File**: `app/src/main/java/jp/sblo/pandora/aGrep/SearchViewModel.java`

**Tests to Write** (8-12 tests):
```
✓ Initial state is idle
✓ search() transitions to searching state
✓ search() triggers GrepEngine.search()
✓ Progress callback updates uiState LiveData
✓ Complete callback transitions to completed state
✓ cancelSearch() calls GrepEngine.cancel()
✓ cancelSearch() transitions to idle state
✓ onCleared() calls GrepEngine.shutdown()
✓ Handles null SearchRequest
✓ Handles multiple rapid search() calls
✓ Progress updates are posted on main thread
```

**Dependencies Required**:
```gradle
testImplementation 'androidx.arch.core:core-testing:2.2.0'  // InstantTaskExecutorRule
testImplementation 'org.mockito:mockito-core:5.+'
```

**File to Create**: `app/src/test/java/jp/sblo/pandora/aGrep/SearchViewModelTest.java`

---

### 3.2 Settings.java - Activity Logic (Extractable)
**File**: `app/src/main/java/jp/sblo/pandora/aGrep/Settings.java`

**Methods to Extract and Test**:

#### `sortCheckedStrings(List<CheckedString>)` (Lines 263-273)
**Tests to Write** (4-5 tests):
```
✓ Sorts by displayName (case-insensitive)
✓ Handles null list
✓ Handles empty list
✓ Stable sort for equal displayNames
✓ Unicode sorting
```

**File to Create**: `app/src/test/java/jp/sblo/pandora/aGrep/SettingsUtilsTest.java`

---

## Phase 4: Performance & Regression Tests

**Priority: LOW-MEDIUM | Estimated: 5-10 tests | Time: 2-3 hours**

### 4.1 UniversalDetector Performance Tests
**File**: `app/src/test/java/jp/sblo/pandora/aGrep/UniversalDetectorPerformanceTest.java`

**Tests to Write**:
```
✓ Reusing detector is faster than creating new instances
✓ Detector handles large files (1MB+) without hanging
✓ reset() clears all internal state
✓ Multiple resets don't degrade performance
✓ Concurrent detector usage (if applicable)
```

---

### 4.2 Extension Matching Performance
**File**: `app/src/test/java/jp/sblo/pandora/aGrep/ExtensionMatchingBenchmarkTest.java`

**Tests to Write**:
```
✓ Extension matching is O(1) for HashSet-based implementation
✓ Case-insensitive matching doesn't create excessive string objects
✓ Handles 1000+ file names efficiently
```

---

## Testing Infrastructure Setup

### Dependencies to Add (if not present)
Add to `app/build.gradle`:
```gradle
dependencies {
    // Unit testing
    testImplementation 'junit:junit:4.13.2'
    testImplementation 'org.mockito:mockito-core:5.11.0'
    testImplementation 'org.mockito:mockito-inline:5.2.0'  // For mocking static/final methods

    // Android testing utilities
    testImplementation 'androidx.arch.core:core-testing:2.2.0'  // LiveData testing
    testImplementation 'org.robolectric:robolectric:4.12'  // Optional: for Android framework

    // Optional: Better assertions
    testImplementation 'org.assertj:assertj-core:3.25.3'
}
```

### Test Utilities to Create

**File**: `app/src/test/java/jp/sblo/pandora/aGrep/TestUtils.java`
```java
public class TestUtils {
    public static Prefs createPrefsWithExtensions(String... extensions) {
        Prefs prefs = new Prefs();
        prefs.mExtList = new ArrayList<>();
        for (String ext : extensions) {
            prefs.mExtList.add(new CheckedString(true, ext));
        }
        return prefs;
    }

    public static SharedPreferences mockSharedPreferences(Map<String, Object> values) {
        // Mockito-based SharedPreferences mock
    }
}
```

---

## Success Metrics

### Code Coverage Targets
- **Phase 1 Complete**: 40% coverage of core business logic
- **Phase 2 Complete**: 60% coverage of core business logic
- **Phase 3 Complete**: 70% coverage overall
- **Phase 4 Complete**: 75% coverage with performance benchmarks

### Test Execution Time
- All unit tests should complete in < 5 seconds
- Integration tests should complete in < 30 seconds

### Quality Metrics
- Zero flaky tests (tests must be deterministic)
- All tests must have clear, descriptive names
- Each test should test ONE thing
- Follow AAA pattern: Arrange, Act, Assert

---

## Implementation Order (Prioritized)

1. **Week 1**: CheckedStringTest, SearchUtilsTest, GrepViewDataTest
2. **Week 2**: SearchRequest/Progress/Summary/UiStateTest
3. **Week 3**: ExecutorGrepEngineTest (extension matching)
4. **Week 4**: PrefsTest (with mocking)
5. **Week 5**: SearchViewModelTest
6. **Week 6**: Performance tests and remaining gaps

---

## Notes & Considerations

### What NOT to Test (Yet)
- UI components (Activities, Fragments) - require instrumentation tests
- SAF DocumentFile operations - require Android framework
- ContentResolver operations - require Android context
- Actual file I/O - requires filesystem access

### Future Improvements
- Add instrumentation tests for UI flows
- Add integration tests for full search pipeline
- Add screenshot tests for UI regression
- Consider property-based testing for complex logic

### Potential Roadblocks
- **Prefs.java** uses SharedPreferences heavily - requires good mocking strategy
- **Search.java** has many private utility methods - may need to extract to utils class
- **ExecutorGrepEngine** has DocumentFile dependencies - may need to create abstraction layer
- Some classes are tightly coupled to Android framework - consider refactoring for testability

---

## File Structure After Completion

```
app/src/test/java/jp/sblo/pandora/aGrep/
├── CheckedStringTest.java                    [NEW - Phase 1]
├── SearchUtilsTest.java                      [NEW - Phase 1]
├── GrepViewDataTest.java                     [NEW - Phase 1]
├── SearchRequestTest.java                    [NEW - Phase 1]
├── SearchProgressTest.java                   [NEW - Phase 1]
├── SearchSummaryTest.java                    [NEW - Phase 1]
├── SearchUiStateTest.java                    [NEW - Phase 1]
├── ExecutorGrepEngineTest.java              [NEW - Phase 2]
├── PrefsTest.java                            [NEW - Phase 2]
├── SearchViewModelTest.java                  [NEW - Phase 3]
├── SettingsUtilsTest.java                    [NEW - Phase 3]
├── UniversalDetectorPerformanceTest.java    [NEW - Phase 4]
├── UniversalDetectorRegressionTest.java     [EXISTING]
└── TestUtils.java                            [NEW - Helper]
```

---

## Quick Start: First Test to Write

Start here for immediate impact:

**File**: `app/src/test/java/jp/sblo/pandora/aGrep/CheckedStringTest.java`

This is the easiest and most valuable first test:
- Zero Android dependencies
- Simple data class
- High value (used throughout codebase)
- Good practice for the pattern

Run with: `./gradlew test`
