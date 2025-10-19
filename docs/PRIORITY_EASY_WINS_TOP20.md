# Top 20 Easy Wins (Impact × Low Effort)

Tasks prioritized by **high impact** combined with **low to medium effort**. These are the best return-on-investment improvements.

## Scoring System
- **Impact**: ⭐ (1) to ⭐⭐⭐⭐⭐ (5)
- **Effort**: 🔨 (< 1 hour) to 🔨🔨🔨🔨🔨 (40+ hours)
- **ROI Score**: Impact stars × (6 - Effort hammers) = Higher is better

---

## 🏆 Tier 1: Quick Wins (< 1 hour each)

### 1. Investigate Automatic Dark Theme Support
**Impact**: ⭐⭐⭐⭐⭐ IF automatic | **Effort**: 🔨 (30 mins investigation) | **ROI**: 25 if yes, 0 if no

**What**: Check if we can get dark theme for free using DayNight theme

**Why Investigate**:
- GitHub Issue #3 requests dark theme
- If Android DayNight theme gives it automatically: **MASSIVE WIN**
- If not: becomes 🔨🔨🔨🔨 effort (manual theme resources)

**Investigation Steps**:
1. Check current theme in `res/values/themes.xml`
2. Try changing to `Theme.AppCompat.DayNight` or `Theme.Material3.DayNight`
3. Test with device set to dark mode
4. Check if all screens look acceptable in dark mode
5. Document findings

**If Automatic Works**:
- **Top priority easy win** - just change parent theme
- Test all activities in dark mode
- Fix any hardcoded colors that don't adapt

**If Manual Work Needed**:
- Move to "Major Features" list (6-8 hours effort)
- Would need: dark color palette, theme variants, test all screens

**Files**:
- `app/src/main/res/values/themes.xml`
- Possibly: `app/src/main/res/values-night/` (if manual work needed)

---

### 2. Fix Special Character Crash (Backslash Search)
**Impact**: ⭐⭐⭐⭐ | **Effort**: 🔨 (30 mins) | **ROI**: 20

**What**: Escape regex metacharacters when regex mode is OFF

**Why Easy**:
- Clear bug: searching for `\` crashes app
- Fix: When regex checkbox is unchecked, escape input before creating Pattern
- Standard pattern: `Pattern.quote(userInput)`

**Why High Impact**:
- Instant crash on common characters
- Affects Windows paths (`C:\Users\...`)
- Affects code searches (brackets, braces, etc.)

**Implementation**:
```java
String pattern = userQuery;
if (!prefs.mUseRegex) {
    pattern = Pattern.quote(pattern);  // Escape all metacharacters
}
Pattern compiledPattern = Pattern.compile(pattern, flags);
```

**Files**:
- `app/src/main/java/jp/sblo/pandora/aGrep/ExecutorGrepEngine.java`
- Possibly: `app/src/main/java/jp/sblo/pandora/aGrep/Search.java`

**Test Cases**:
- Search for `\` (backslash)
- Search for `(`, `)`, `[`, `]`, `{`, `}` (brackets)
- Search for `*`, `+`, `?`, `.` (quantifiers)
- Search for `^`, `$`, `|` (anchors/alternation)

**From**: `playstore_reviews_action_plan.md` - Critical Bug #2

---

### 3. Fix StaticFieldLeak in TextViewer
**Impact**: ⭐⭐⭐⭐ | **Effort**: 🔨 (30 mins) | **ROI**: 20

**What**: Make TextLoadTask static with WeakReference to prevent Activity memory leak

**Why Easy**:
- Standard pattern, well-documented
- Small refactor to inner class

**Why High Impact**:
- Prevents memory leaks
- Activity leaks can cause crashes/OOM over time
- Affects all text viewing operations

**Implementation**:
```java
static class TextLoadTask extends AsyncTask<Uri, Integer, Boolean> {
    private final WeakReference<TextViewer> activityRef;

    TextLoadTask(TextViewer activity) {
        this.activityRef = new WeakReference<>(activity);
    }

    @Override
    protected Boolean doInBackground(Uri... params) {
        TextViewer activity = activityRef.get();
        if (activity == null) return false;
        // ... rest of implementation with null checks
    }
}
```

**Files**:
- `app/src/main/java/jp/sblo/pandora/aGrep/TextViewer.java:162`

**From**: `LINT_WARNINGS_PLAN.md` - High Priority #1

---

### 4. Fix InefficientWeight + KeyboardAccessibility Lint Warnings
**Impact**: ⭐⭐⭐ | **Effort**: 🔨 (15 mins) | **ROI**: 15

**What**: Fix 2 easy lint warnings in `fileselector.xml`

**Why Easy**:
- Line 16: Change `layout_height="wrap_content"` → `0dp` (1 line)
- Line 19: Add `android:focusable="true"` (1 line)

**Why Impact**:
- Improves accessibility for keyboard users
- Minor performance improvement (skip double layout measurement)
- Reduces lint warnings from 39 → 37

**Files**:
- `app/src/main/res/layout/fileselector.xml:16` (InefficientWeight)
- `app/src/main/res/layout/fileselector.xml:19` (KeyboardAccessibility)

**From**: `LINT_WARNINGS_PLAN.md` - Phase 1 Quick Fixes

---

### 5. Implement performClick() in GrepView
**Impact**: ⭐⭐⭐⭐ | **Effort**: 🔨 (30 mins) | **ROI**: 20

**What**: Override performClick() to enable accessibility services (TalkBack, Switch Access)

**Why Easy**:
- Standard accessibility pattern
- Well-documented fix

**Why High Impact**:
- Enables TalkBack and Switch Access
- Affects users with disabilities
- Google Play promotes accessible apps

**Implementation**:
```java
@Override
public boolean performClick() {
    super.performClick();
    // Handle click logic here (move from onTouchEvent)
    return true;
}

@Override
public boolean onTouchEvent(MotionEvent ev) {
    // Detect click gesture
    if (isClickGesture) {
        return performClick(); // Call performClick instead of handling directly
    }
    return super.onTouchEvent(ev);
}
```

**Files**:
- `app/src/main/java/jp/sblo/pandora/aGrep/GrepView.java:110`

**From**: `LINT_WARNINGS_PLAN.md` - High Priority #4

---

## 🥈 Tier 2: Weekend Morning Projects (1-2 hours each)

### 6. Test SD Card Access on Android 12+
**Impact**: ⭐⭐⭐⭐⭐ IF broken, ⭐ IF working | **Effort**: 🔨 (30-60 mins testing) | **ROI**: 25 or 5

**What**: Verify if recent SAF migration fixed SD card access

**Why Important**:
- Japanese users report "Cannot select SD card on Android 12"
- May already be fixed - need to verify
- If broken: becomes high-effort fix
- If working: document and celebrate

**Testing Steps**:
1. Create Android 12+ emulator with virtual SD card
2. Launch aGrep, try to select directory on SD card
3. Test actual search on SD card files
4. Test persistence across app restarts
5. Document test procedure for future verification

**Deliverable**:
- `docs/SD_CARD_TESTING.md` with test procedure
- Update PRIORITY_IMPACT_TOP5.md with test results

**Files to Check** (if broken):
- `app/src/main/java/jp/sblo/pandora/aGrep/FileSelector.java`
- `app/src/main/java/jp/sblo/pandora/aGrep/ExecutorGrepEngine.java`

**From**: `PRIORITY_IMPACT_TOP5.md` - Task #1

---

### 7. Export Search Results to File
**Impact**: ⭐⭐⭐ | **Effort**: 🔨🔨 (1-2 hours, good for AI assist) | **ROI**: 9

**What**: Add "Export Results" button to save search results as text/CSV

**Why Medium Effort**:
- SAF file picker for save location
- Format results as text or CSV
- Simple implementation with AI help

**Why Good Impact**:
- User-requested feature
- Improves workflow (share results with others)
- Professional polish

**Implementation**:
```java
private void exportResults() {
    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    intent.setType("text/plain");
    intent.putExtra(Intent.EXTRA_TITLE, "agrep_results.txt");
    exportLauncher.launch(intent);
}

// In callback:
private void writeResults(Uri uri) {
    try (OutputStream os = getContentResolver().openOutputStream(uri);
         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os))) {
        writer.write("aGrep Search Results\n");
        writer.write("Query: " + searchQuery + "\n\n");
        for (GrepView.Data result : allResults) {
            writer.write(result.displayName + ":" + result.lineNumber + ":" + result.content + "\n");
        }
    }
}
```

**Files**:
- `app/src/main/java/jp/sblo/pandora/aGrep/Search.java`

**From**: `playstore_reviews_action_plan.md` - Feature #8

---

### 8. Add Search in Filenames Feature
**Impact**: ⭐⭐⭐⭐ | **Effort**: 🔨🔨 (1-2 hours) | **ROI**: 12

**What**: Add checkbox "Also search in filenames" to match pattern against file paths

**Why Medium Effort**:
- Add checkbox to UI
- Match pattern against filename in addition to content
- Show filename matches in results

**Why High Impact**:
- Multiple user requests (Chinese, German, English reviews)
- Common use case: find files by name pattern
- Extends utility without bloat

**Implementation**:
```java
// In ExecutorGrepEngine.processDocument():
boolean filenameMatch = false;
if (prefs.mSearchFilenames) {
    String filename = safeName(document);
    if (pattern.matcher(filename).find()) {
        filenameMatch = true;
        // Add special result indicating filename match
    }
}

// Still search content as well if checkbox is "also" (not "only")
```

**UI**:
- Add checkbox: "☐ Also search in filenames"
- Or: Radio buttons: ( ) Content only  ( ) Filename only  (•) Both

**Files**:
- `app/src/main/java/jp/sblo/pandora/aGrep/Search.java` - Add checkbox
- `app/src/main/java/jp/sblo/pandora/aGrep/ExecutorGrepEngine.java` - Match logic
- `app/src/main/java/jp/sblo/pandora/aGrep/Prefs.java` - Save preference

**From**: `playstore_reviews_action_plan.md` - Feature #16

---

### 9. Add "Copy File Path" Context Menu
**Impact**: ⭐⭐⭐⭐ | **Effort**: 🔨🔨 (1-2 hours) | **ROI**: 12

**What**: Long-press result item → context menu → "Copy path"

**Why Medium Effort**:
- Add context menu to result list items
- Implement clipboard copy
- Add toast confirmation

**Why High Impact**:
- Requested feature
- Improves workflow (share paths with others, paste into terminal)
- Easy discoverability

**Implementation**:
```java
@Override
public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    menu.add(0, MENU_COPY_PATH, 0, "Copy file path");
    menu.add(0, MENU_COPY_RELATIVE_PATH, 0, "Copy relative path");
}

@Override
public boolean onContextItemSelected(MenuItem item) {
    if (item.getItemId() == MENU_COPY_PATH) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("File path", selectedPath);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Path copied", Toast.LENGTH_SHORT).show();
        return true;
    }
    // ...
}
```

**Files**:
- `app/src/main/java/jp/sblo/pandora/aGrep/Search.java`

**From**: `playstore_reviews_action_plan.md` - Feature #12, GitHub Issue #10

---

### 10. Add Search Result Count Display
**Impact**: ⭐⭐⭐⭐ | **Effort**: 🔨🔨 (1-2 hours) | **ROI**: 12

**What**: Show "XX matches found in YY files" in results summary

**Why Medium Effort**:
- Count already tracked internally
- Add TextView to layout
- Wire up data binding

**Why High Impact**:
- Requested by Japanese users
- Helps users understand search success
- Professional polish

**Implementation**:
```java
// In SearchViewModel or Search activity:
private void updateResultSummary(SearchSummary summary) {
    int matchCount = summary.allMatches.size();
    int fileCount = countUniqueFiles(summary.allMatches);
    String summaryText = String.format("%d matches found in %d files", matchCount, fileCount);
    resultSummaryTextView.setText(summaryText);
}
```

**Files**:
- `app/src/main/java/jp/sblo/pandora/aGrep/Search.java`
- Layout file for results screen

**From**: `playstore_reviews_international.md` - New Feature #8

---

### 11. Modernize TextViewer (AsyncTask → Coroutines)
**Impact**: ⭐⭐⭐ | **Effort**: 🔨🔨 (1-2 hours with AI assist) | **ROI**: 9

**What**: Replace deprecated AsyncTask with coroutines/ViewModel

**Why Medium Effort**:
- AI can help with boilerplate conversion
- Standard pattern (AsyncTask → coroutines)
- Still need thorough testing

**Why Impact**:
- Removes deprecation warnings
- More modern, maintainable code
- Better lifecycle handling

**Implementation**:
```kotlin
// Convert to:
class TextViewerViewModel : ViewModel() {
    fun loadText(uri: Uri) = viewModelScope.launch {
        val lines = withContext(Dispatchers.IO) {
            // Read file in background
        }
        // Update UI on main thread
        _textLines.value = lines
    }
}
```

**Files**:
- `app/src/main/java/jp/sblo/pandora/aGrep/TextViewer.java`

**From**: `codex_tasks_quality.md` - Modernize TextViewer file loading

---

### 12. Fix '#' Character in Filename Bug
**Impact**: ⭐⭐⭐⭐ | **Effort**: 🔨🔨 (1-2 hours) | **ROI**: 12

**What**: Fix URI encoding issue when filenames contain '#'

**Why Medium Effort**:
- Need to properly URL-encode URIs when opening files
- Test with various special characters
- May need FileProvider config update

**Why High Impact**:
- Files with '#' cannot be opened from results
- Silent failure frustrates users
- Common in some naming schemes (e.g., `track#01.txt`)

**Implementation**:
```java
// When building URI to open file:
Uri fileUri = FileProvider.getUriForFile(context, authority, file);
String encodedUri = Uri.encode(fileUri.toString());  // Proper encoding
Intent intent = new Intent(Intent.ACTION_VIEW);
intent.setDataAndType(Uri.parse(encodedUri), mimeType);
```

**Files**:
- File opening logic in Search.java or TextViewer.java
- Possibly: FileProvider configuration

**From**: `playstore_reviews_international.md` - Bug #6

---

## 🥉 Tier 3: Weekend Projects (2-4 hours each)

### 13. Set Up Translation Infrastructure (Crowdin/Weblate)
**Impact**: ⭐⭐⭐⭐ | **Effort**: 🔨🔨🔨 (3-4 hours) | **ROI**: 8

**What**: Set up Crowdin or Weblate for community-contributed translations

**Why Medium-High Effort**:
- Create account on Crowdin/Weblate
- Upload existing strings (EN, JA, RU)
- Configure GitHub integration (auto-PR on new translations)
- Document contribution process

**Why High Impact**:
- Enables community translations for all languages
- Japanese users are largest segment (40 reviews)
- GitHub Issue #7 has Russian translation ready
- Scalable solution vs. ad-hoc translations

**Steps**:
1. Choose platform (Crowdin recommended for open source)
2. Create project, upload `strings.xml` files
3. Configure GitHub integration
4. Add translation instructions to README
5. Announce translation portal to users

**Deliverable**:
- Public translation portal URL
- `docs/TRANSLATION_GUIDE.md` for contributors
- GitHub Actions integration for automated PRs

**Files**:
- `app/src/main/res/values/strings.xml` (upload to platform)
- `app/src/main/res/values-ja/strings.xml` (upload existing)
- `app/src/main/res/values-ru/strings.xml` (create with GitHub Issue #7 content)

**From**: Discussion - generalized from Japanese/Russian translation tasks

---

### 14. Fix Search Cancellation Logic
**Impact**: ⭐⭐⭐⭐⭐ | **Effort**: 🔨🔨 (1-2 hours) | **ROI**: 15

**What**: Fix cancellation so it doesn't show "Finished" toast and properly stops

**Why Medium Effort**:
- Update ExecutorGrepEngine to short-circuit when cancelled
- Guard SearchViewModel callbacks
- Mostly UI wiring, straightforward

**Why Very High Impact**:
- Can't cancel long searches = poor UX
- "Finished" toast when cancelled = confusing
- Worsened by SAF migration (longer searches)

**Implementation** (from codex_tasks_quality.md):
```java
// In ExecutorGrepEngine:
private void processDocumentTree(...) {
    if (cancelled.get()) {
        return;  // Short-circuit, don't call onComplete
    }
    // ...
}

// In SearchViewModel:
private boolean searchCancelled = false;

void cancelSearch() {
    searchCancelled = true;
    engine.cancel();
}

void onComplete(SearchSummary summary) {
    if (searchCancelled) {
        _uiState.value = SearchUiState.idle();  // Not completed
        return;
    }
    _uiState.value = SearchUiState.completed(summary);
}
```

**Files**:
- `app/src/main/java/jp/sblo/pandora/aGrep/ExecutorGrepEngine.java`
- `app/src/main/java/jp/sblo/pandora/aGrep/SearchViewModel.java`

**From**: `PRIORITY_IMPACT_TOP5.md` - Task #5

---

### 15. Fix GitHub Issue #10: Copy from Results
**Impact**: ⭐⭐⭐⭐ | **Effort**: 🔨🔨 (1-2 hours) | **ROI**: 12

**What**: Enable text selection in search results list

**Why Medium Effort**:
- Configure TextView to allow selection
- May need to adjust list item layout
- Test selection behavior

**Why High Impact**:
- GitHub issue = user invested effort to report
- Affects workflow (copy matched text)
- Expected feature in modern apps

**Implementation**:
```java
// In result list item TextView:
textView.setTextIsSelectable(true);

// Or provide context menu:
@Override
public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    menu.add(0, MENU_COPY_TEXT, 0, "Copy matched text");
}
```

**Files**:
- Result list adapter
- Result item layout

**From**: `github_issues_tasks.md` - Issue #10

---

### 16. Fix Rotation State Loss (Issues #4, #5, #6)
**Impact**: ⭐⭐⭐⭐ | **Effort**: 🔨🔨🔨 (2-4 hours) | **ROI**: 8

**What**: Preserve checkbox states, dialog state, search results on orientation change

**Why Medium-High Effort**:
- Need ViewModel or onSaveInstanceState
- Multiple Activities affected (Search, Settings, ColorPicker)
- Test all rotation scenarios

**Why High Impact**:
- Extremely frustrating UX currently
- Multiple GitHub issues confirm problem
- Affects all configuration changes

**Implementation Options**:
```java
// Option 1: ViewModel (modern, recommended)
class SearchViewModel : ViewModel() {
    val selectedDirectories = MutableLiveData<List<CheckedString>>()
    val selectedExtensions = MutableLiveData<List<CheckedString>>()
}

// Option 2: onSaveInstanceState (simpler for Activities)
@Override
protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putParcelableArrayList("directories", selectedDirectories);
    outState.putParcelableArrayList("extensions", selectedExtensions);
}

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (savedInstanceState != null) {
        selectedDirectories = savedInstanceState.getParcelableArrayList("directories");
        selectedExtensions = savedInstanceState.getParcelableArrayList("extensions");
    }
}
```

**Files**:
- `app/src/main/java/jp/sblo/pandora/aGrep/Search.java`
- `app/src/main/java/jp/sblo/pandora/aGrep/Settings.java`
- `app/src/main/java/jp/sblo/pandora/aGrep/ColorPickerActivity.java`

**From**: `github_issues_tasks.md` - Issues #4, #5, #6

---

### 17. Add Regex Help Dialog
**Impact**: ⭐⭐⭐⭐ | **Effort**: 🔨🔨🔨 (2-3 hours) | **ROI**: 8

**What**: Create help dialog with common regex patterns and examples

**Why Medium-High Effort**:
- Design help UI
- Write examples and explanations
- Add "Examples" or "Help" button

**Why High Impact**:
- Users request regex help
- Regex is powerful but confusing
- Increases feature adoption

**Content**:
- Basic patterns: `.` (any char), `\d` (digit), `\w` (word), `\s` (whitespace)
- Quantifiers: `*` (0+), `+` (1+), `?` (0-1), `{n,m}` (range)
- Groups: `()` (capture), `|` (or)
- Examples:
  - Email: `[\w._%+-]+@[\w.-]+\.[A-Z]{2,}`
  - Phone: `\d{3}-\d{3}-\d{4}`
  - URL: `https?://\S+`
- Link to Java Pattern docs

**Files**:
- Create: `app/src/main/java/jp/sblo/pandora/aGrep/RegexHelpActivity.java`
- Create: `app/src/main/res/layout/regex_help.xml`
- Update: `Search.java` to add Help button/menu

**From**: `playstore_reviews_action_plan.md` - Feature #13

---

### 18. Add Search History Management
**Impact**: ⭐⭐⭐⭐ | **Effort**: 🔨🔨🔨 (3-4 hours) | **ROI**: 8

**What**: Show recent searches in dropdown, allow clearing history

**Why Medium-High Effort**:
- Change EditText to AutoCompleteTextView
- Store history in SharedPreferences
- Add "Clear history" menu option
- Handle history item selection

**Why High Impact**:
- Requested by Japanese users
- Improves workflow (repeat searches)
- Professional feature

**Implementation**:
```java
// Change search field to AutoCompleteTextView
AutoCompleteTextView searchField = findViewById(R.id.search_field);
ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
    android.R.layout.simple_dropdown_item_1line, searchHistory);
searchField.setAdapter(adapter);

// Save search to history
private void addToHistory(String query) {
    searchHistory.add(0, query);
    if (searchHistory.size() > 20) {
        searchHistory.remove(20);  // Keep last 20
    }
    saveHistory();
}

// Load/save from SharedPreferences
private void saveHistory() {
    String json = new Gson().toJson(searchHistory);
    prefs.edit().putString("search_history", json).apply();
}
```

**Files**:
- `app/src/main/java/jp/sblo/pandora/aGrep/Search.java`
- `app/src/main/java/jp/sblo/pandora/aGrep/Prefs.java`

**From**: `playstore_reviews_international.md` - New Feature #2

---

### 19. Add Subdirectory Recursion Toggle
**Impact**: ⭐⭐⭐⭐ | **Effort**: 🔨🔨🔨 (2-3 hours) | **ROI**: 8

**What**: Add checkbox "Search subdirectories" (default: on)

**Why Medium-High Effort**:
- Add UI checkbox
- Update ExecutorGrepEngine traversal logic to skip recursion
- Save preference
- Test both modes

**Why High Impact**:
- Requested by Japanese users
- Performance benefit (skip deep trees when not needed)
- Precision control (search only specific folder)

**Implementation**:
```java
// In UI:
CheckBox recurseCheckbox = findViewById(R.id.recurse_subdirectories);
recurseCheckbox.setChecked(prefs.mRecurseSubdirectories);  // Default true

// In ExecutorGrepEngine.processDocumentTree():
if (child.isDirectory()) {
    if (prefs.mRecurseSubdirectories) {  // NEW: Check preference
        processDocumentTree(child, pattern, results, callback);
    }
    // else: skip subdirectory, don't recurse
} else {
    processDocument(child, pattern, results, callback);
}
```

**Files**:
- `app/src/main/java/jp/sblo/pandora/aGrep/Search.java` - Add checkbox
- `app/src/main/java/jp/sblo/pandora/aGrep/ExecutorGrepEngine.java` - Skip recursion when disabled
- `app/src/main/java/jp/sblo/pandora/aGrep/Prefs.java` - Store preference

**From**: `playstore_reviews_international.md` - New Feature #7

---

### 20. Test NAS/Network Storage Support
**Impact**: ⭐⭐ | **Effort**: 🔨 (30-60 mins testing + documentation) | **ROI**: 10

**What**: Test if SAF already supports network storage, document findings

**Why Low Effort**:
- Just testing with network storage
- Document whether it works or not
- No coding unless SAF doesn't support (then out of scope)

**Why Medium Impact**:
- Japanese/German users requested
- If SAF supports: free feature, just document
- If SAF doesn't support: out of scope for grep app

**Testing Steps**:
1. Set up SMB share or use cloud storage app
2. Try to select network directory in aGrep via SAF picker
3. Test if search works on network files
4. Test performance (network latency considerations)
5. Document findings

**Deliverable**:
- `docs/NETWORK_STORAGE_SUPPORT.md`
- If works: Update README with network storage capability
- If doesn't work: Document as out of scope

**From**: `playstore_reviews_international.md` - Feature request, discussion

---

## 📊 Summary Statistics

### By Effort Level
- **🔨 (< 1 hour)**: 5 tasks - Do these first!
- **🔨🔨 (1-2 hours)**: 10 tasks - Weekend morning projects
- **🔨🔨🔨 (2-4 hours)**: 5 tasks - Weekend day projects

### By Impact Level
- **⭐⭐⭐⭐⭐ (Highest)**: 3 tasks (Dark theme if automatic, SD card test, cancellation fix)
- **⭐⭐⭐⭐ (Very High)**: 13 tasks
- **⭐⭐⭐ (High)**: 3 tasks
- **⭐⭐ (Medium)**: 1 task

### Top 10 by ROI
1. **Dark Theme Investigation** (ROI: 25 if automatic)
2. **SD Card Testing** (ROI: 25 if broken)
3. **Special Char Crash** (ROI: 20)
4. **StaticFieldLeak** (ROI: 20)
5. **performClick()** (ROI: 20)
6. **InefficientWeight/Keyboard** (ROI: 15)
7. **Cancellation Fix** (ROI: 15)
8. **Search in Filenames** (ROI: 12)
9. **Copy File Path** (ROI: 12)
10. **Result Count Display** (ROI: 12)

---

## 🚫 Tasks NOT on This List

These have high impact but require significant effort (not "easy wins"):

### Major Features (Move to Separate Roadmap)
- **Performance Benchmarking** (Impact: ⭐⭐⭐⭐⭐, Effort: 🔨🔨🔨) - Critical but medium effort
- **Test Coverage Phase 1** (Impact: ⭐⭐⭐⭐⭐, Effort: 🔨🔨🔨) - Critical but medium effort
- **File Size Limit Fix** (Impact: ⭐⭐⭐⭐⭐, Effort: 🔨🔨🔨) - Investigation + fix
- **Find & Replace** (Impact: ⭐⭐, Effort: 🔨🔨🔨🔨🔨) - **ICEBOX** - out of core scope
- **Widget Support** (Impact: ⭐, Effort: 🔨🔨🔨🔨) - **WONTFIX** - out of scope
- **Fuzzy Search** (Impact: ⭐⭐⭐, Effort: 🔨🔨🔨🔨) - **RESEARCH** - interesting but high effort

### Low Impact
- **First-Run Tutorial** (Impact: ⭐⭐, Effort: 🔨🔨🔨) - No new users expected
- **UI Hints** (Impact: ⭐⭐, Effort: 🔨) - Technical users can figure it out
- **ColorPicker Modernization** (Impact: ⭐, Effort: 🔨🔨) - Works fine as-is

### Wait for Benchmarking
All performance optimizations should wait until benchmarking infrastructure exists:
- UniversalDetector cache (claimed 10-15% but unvalidated)
- HashSet extension matching (Impact: ⭐⭐ - tiny benefit)
- Encoding detection skip (Impact: ⭐⭐⭐ but needs measurement)

---

## 🎯 Recommended Implementation Order

### Sprint 0: "Investigation Week" (2-3 hours)
Critical investigations that determine priorities:
1. **Dark theme investigation** - If automatic: huge win
2. **SD card testing** - Verify if broken or fixed
3. **NAS storage testing** - Document capability

### Sprint 1: "Quick Fixes Week" (3-4 hours)
All Tier 1 quick wins:
4. Special character crash fix
5. StaticFieldLeak fix
6. performClick() fix
7. InefficientWeight/Keyboard fixes

### Sprint 2: "Features Week" (8-10 hours)
High-impact, medium-effort features:
8. Export results
9. Search in filenames
10. Copy file path
11. Result count display
12. Cancellation fix

### Sprint 3: "GitHub Issues Week" (6-8 hours)
Respect users who filed issues:
13. Copy from results (Issue #10)
14. Rotation state loss (Issues #4, #5, #6)
15. Translation infrastructure (Issue #7 + scalability)

### Sprint 4: "Polish Week" (6-8 hours)
Smaller features and polish:
16. Modernize TextViewer
17. Fix '#' character bug
18. Regex help dialog
19. Search history
20. Subdirectory toggle
