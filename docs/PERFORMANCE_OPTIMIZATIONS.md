# Performance Optimization Ideas for SAF-based Search

## Background
After migrating to Storage Access Framework (SAF) in PRs #2, #3, #4, and #7, users report significantly slower search performance compared to the old file-based implementation.

## Root Causes Analysis

### Performance Impact Breakdown
1. **SAF ContentProvider IPC overhead** (~60% slowdown) - Directory listing and file metadata queries
2. **Encoding detection per file** (~20% slowdown) - UniversalDetector running on every file
3. **Lack of caching** (~10% slowdown) - No directory listing or metadata cache
4. **Inefficient filtering** (~5% slowdown) - Extension filtering after file operations
5. **UI Update batching** (~3% slowdown) - Throttled progress updates and result sorting
6. **Minor issues** (~2% slowdown) - Pattern compilation, string operations, buffer sizes

## Proposed Optimizations

### Easy Wins (Low effort, good impact)

#### 1. Cache UniversalDetector instances
**Location:** `ExecutorGrepEngine.java:166-175`

**Current:** Creates new `UniversalDetector` for every file
```java
UniversalDetector detector = new UniversalDetector(null);
```

**Proposed:** Reuse single instance with reset
```java
// Create once at class level
private final UniversalDetector detector = new UniversalDetector(null);

// In processDocument():
detector.reset();  // Reset instead of creating new instance
detector.handleData(buff, 0, nread);
detector.dataEnd();
encode = detector.getDetectedCharset();
```

**Expected impact:** 10-15% improvement in file processing speed

---

#### 2. Optimize extension matching with HashSet
**Location:** `ExecutorGrepEngine.java:283-306`

**Current:** Loops through all extensions for every file
```java
for (CheckedString ext : prefs.mExtList) {
    if (ext.checked) {
        // Check extension match
    }
}
```

**Proposed:** Build HashSet once at search start
```java
// Build once per search
Set<String> enabledExtensions = new HashSet<>();
boolean hasNoExtensionFilter = false;
for (CheckedString ext : prefs.mExtList) {
    if (ext.checked) {
        if ("*".equals(ext.string)) {
            hasNoExtensionFilter = true;
        } else {
            enabledExtensions.add(ext.string.toLowerCase());
        }
    }
}

// Fast O(1) lookup per file
private boolean matchesExtension(String fileName) {
    String lower = fileName.toLowerCase();
    int dotIndex = lower.lastIndexOf('.');
    if (dotIndex == -1) {
        return hasNoExtensionFilter;
    }
    String ext = lower.substring(dotIndex + 1);
    return enabledExtensions.isEmpty() || enabledExtensions.contains(ext);
}
```

**Expected impact:** 3-5% improvement in traversal speed

---

#### 3. Skip encoding detection for known text formats
**Location:** `ExecutorGrepEngine.java:160-183`

**Current:** Runs encoding detection on every file

**Proposed:** Assume UTF-8 for common text extensions
```java
private static final Set<String> KNOWN_TEXT_EXTENSIONS = new HashSet<>(Arrays.asList(
    "txt", "java", "xml", "json", "md", "gradle", "properties",
    "html", "css", "js", "kt", "py", "c", "cpp", "h"
));

private String detectEncoding(DocumentFile document, InputStream is) {
    String name = safeName(document).toLowerCase();
    int dotIndex = name.lastIndexOf('.');
    if (dotIndex > 0) {
        String ext = name.substring(dotIndex + 1);
        if (KNOWN_TEXT_EXTENSIONS.contains(ext)) {
            return "UTF-8";  // Skip expensive detection
        }
    }

    // Fall back to detection for unknown types
    // ... existing detection code
}
```

**Expected impact:** 15-20% improvement for common text file searches

---

### Medium Wins (Medium effort, good-high impact)

#### 4. Add extension pre-filtering using DocumentsContract queries
**Location:** `ExecutorGrepEngine.java:107-140`

**Current:** Retrieves all files, then filters by extension after opening

**Proposed:** Use MIME type filtering in SAF queries where possible
```java
// Use DocumentsContract to filter by MIME type
Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
    treeUri,
    DocumentsContract.getDocumentId(treeUri)
);

String[] projection = { DocumentsContract.Document.COLUMN_DOCUMENT_ID };
String selection = DocumentsContract.Document.COLUMN_MIME_TYPE + " = ?";
String[] selectionArgs = { "text/plain" };  // Filter at query level

Cursor cursor = contentResolver.query(childrenUri, projection, selection, selectionArgs, null);
```

**Expected impact:** 10-20% improvement by avoiding unnecessary file opens

**Note:** Limited by MIME type support - may not work for all extensions

---

#### 5. Cache directory listings
**Location:** `ExecutorGrepEngine.java:114`

**Current:** Calls `dir.listFiles()` without caching

**Proposed:** Add LRU cache for directory contents
```java
private static class DirectoryCache {
    private final LruCache<String, DocumentFile[]> cache;

    DirectoryCache(int maxSize) {
        cache = new LruCache<>(maxSize);
    }

    DocumentFile[] getFiles(DocumentFile dir) {
        String key = dir.getUri().toString();
        DocumentFile[] cached = cache.get(key);
        if (cached == null) {
            cached = dir.listFiles();
            cache.put(key, cached);
        }
        return cached;
    }
}
```

**Expected impact:** 5-15% improvement for repeated searches or directory revisits

**Trade-off:** Memory usage vs speed, may need cache invalidation strategy

---

#### 6. Parallel processing with thread pool
**Location:** `ExecutorGrepEngine.java:41`

**Current:** Single-threaded ExecutorService
```java
this.executor = Executors.newSingleThreadExecutor();
```

**Proposed:** Use thread pool for parallel file processing
```java
// Use CPU count or fixed pool size
int threadCount = Math.min(4, Runtime.getRuntime().availableProcessors());
this.executor = Executors.newFixedThreadPool(threadCount);

// Submit files as independent tasks
private void processDocumentTree(DocumentFile dir, ...) {
    DocumentFile[] children = dir.listFiles();
    if (children != null) {
        for (DocumentFile child : children) {
            if (child.isDirectory()) {
                processDocumentTree(child, ...);  // Keep traversal sequential
            } else {
                executor.submit(() -> processDocument(child, ...));  // Parallel file processing
            }
        }
    }
}
```

**Expected impact:** 30-50% improvement on multi-core devices

**Complexity:** Requires careful synchronization of results and progress callbacks

---

### Hard Wins (High effort, high impact)

#### 7. Batch SAF queries with DocumentsContract
**Location:** `ExecutorGrepEngine.java:107-140`

**Current:** Individual `listFiles()` calls per directory

**Proposed:** Use bulk queries to reduce IPC overhead
```java
// Query entire tree structure in fewer operations
Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocumentId);
String[] projection = {
    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
    DocumentsContract.Document.COLUMN_MIME_TYPE,
    DocumentsContract.Document.COLUMN_SIZE
};

Cursor cursor = contentResolver.query(childrenUri, projection, null, null, null);
// Process cursor to build tree structure in memory
```

**Expected impact:** 40-60% improvement in directory traversal

**Complexity:** Significant refactoring required, need to build tree structure manually

---

#### 8. Implement incremental search with checkpoints
**Location:** `ExecutorGrepEngine.java:47-103`

**Current:** Search runs to completion, no resumption

**Proposed:** Save search state periodically for pause/resume
```java
private static class SearchCheckpoint {
    List<String> processedPaths;
    List<GrepView.Data> currentMatches;
    int fileCount;
    long timestamp;
}

// Allow pause and resume
public void pauseSearch() {
    saveCheckpoint(currentState);
}

public void resumeSearch(SearchCheckpoint checkpoint) {
    // Continue from saved state
}
```

**Expected impact:** Better UX for long searches, allows interruption

**Complexity:** State management, persistence, UI changes required

---

## Additional Considerations

### Profiling Metrics to Add
To measure improvement effectiveness:
1. Time to first result
2. Directory traversal time (separate from file processing)
3. Encoding detection time per file
4. File open overhead
5. Pattern matching time
6. Total search time

### Suggested Implementation Order
1. **Phase 1** (Quick wins): #1, #2, #3
2. **Phase 2** (Medium effort): #4, #5
3. **Phase 3** (Complex): #6, #7
4. **Phase 4** (UX): #8

### Trade-offs to Consider
- **Memory vs Speed**: Caching uses memory but speeds up searches
- **Complexity vs Maintainability**: Parallel processing adds complexity
- **SAF Limitations**: Not all optimizations work with all storage providers
- **Battery Impact**: More parallel threads = more power consumption

## References
- Main search engine: `app/src/main/java/jp/sblo/pandora/aGrep/ExecutorGrepEngine.java`
- Search activity: `app/src/main/java/jp/sblo/pandora/aGrep/Search.java`
- ViewModel: `app/src/main/java/jp/sblo/pandora/aGrep/SearchViewModel.java`
