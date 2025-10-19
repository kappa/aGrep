# Google Play Store Reviews - Action Plan

Comprehensive plan to address user feedback from 144 Google Play Store reviews.

## Status Legend
- ✅ **RESOLVED** - Already fixed
- 🔴 **CRITICAL** - High priority bugs affecting core functionality
- 🟡 **HIGH** - Important UX/compatibility issues
- 🟢 **MEDIUM** - Feature requests and enhancements
- 🔵 **LOW** - Nice-to-have improvements

---

## Critical Bugs

### ✅ 1. App doesn't work on newer Android versions (RESOLVED)
**Reviews**: #2, #23, #42, #61, #65, #71, #72, #73, #95
**Issue**: Runtime permissions issues on Android 6+, general compatibility problems
**Status**: Already addressed in recent updates

### 🔴 2. App crashes when searching certain characters
**Review**: #89
**Issue**: Searching for backslash `\` and potentially other special regex characters causes instant crash

**Tasks**:
1. Add input validation and sanitization for search queries when regex is disabled
2. Properly escape special regex characters in user input when regex mode is off
3. Add try-catch blocks around regex compilation to handle malformed patterns gracefully
4. Display user-friendly error messages instead of crashing
5. Add unit tests for special character handling (backslash, brackets, parentheses, etc.)
6. Test with common problematic inputs: `\`, `(`, `)`, `[`, `]`, `{`, `}`, `*`, `+`, `?`, `.`, `^`, `$`, `|`

**Files**: `app/src/main/java/jp/sblo/pandora/aGrep/ExecutorGrepEngine.java`, `Search.java`

### 🔴 3. Only searches first 10k of files
**Review**: #131
**Issue**: File scanning limitation - doesn't search entire file content

**Tasks**:
1. Investigate the current file reading implementation in `ExecutorGrepEngine`
2. Identify if there's a hardcoded buffer/line limit
3. Remove or significantly increase the limit to handle large files
4. Implement streaming file reading to avoid memory issues with very large files
5. Add progress indicators for large file processing
6. Test with files of various sizes (1MB, 10MB, 50MB, 100MB+)
7. Document any practical file size limits based on memory constraints

**Files**: `app/src/main/java/jp/sblo/pandora/aGrep/ExecutorGrepEngine.java`

---

## High Priority Issues

### 🟡 4. Cannot access external SD card / OTG USB
**Reviews**: #7, #66, #92
**Issue**: App limited to internal storage only, cannot browse/search external SD or USB OTG devices

**Tasks**:
1. Implement Storage Access Framework (SAF) for modern Android storage access
2. Add DocumentFile support for external storage and SD cards
3. Update file browser to show all available storage locations
4. Add permission handling for external storage access
5. Test on devices with SD cards and OTG USB drives
6. Update directory picker UI to clearly show all available storage locations
7. Maintain backward compatibility with direct file access where available

**Files**: `app/src/main/java/jp/sblo/pandora/aGrep/FileSelector.java`, `ExecutorGrepEngine.java`, AndroidManifest.xml

### 🟡 5. Phrase search doesn't work correctly
**Review**: #49
**Issue**: Searching for multi-word phrases only finds individual words, not the complete phrase

**Tasks**:
1. Fix phrase search logic when regex is disabled
2. When regex is off and query contains spaces, treat entire input as literal string to find
3. Add option/checkbox for "Match exact phrase" mode
4. Update UI to clarify difference between word search and phrase search
5. Add help text or tooltip explaining how to search for phrases
6. Test with various multi-word queries: "hello world", "test phrase here", etc.
7. Document phrase search behavior in help/about section

**Files**: `app/src/main/java/jp/sblo/pandora/aGrep/ExecutorGrepEngine.java`, `Search.java`

### 🟡 6. UI/UX not intuitive
**Reviews**: #12, #50, #51, #55, #69, #128, #137
**Issues**:
- Folder selection confusing (requires long-press, not obvious)
- No instructions or help available
- Non-intuitive navigation
- Results list hard to read

**Tasks**:
1. **Add visual hints for folder/extension selection**:
   - Add hint text: "Long-press to select/deselect" below target directories list
   - Add visual indicator (checkbox or highlight) showing selected state
   - Consider allowing single tap to select with confirmation dialog

2. **Create in-app help/tutorial**:
   - Add "Help" menu item with quick start guide
   - Create first-run tutorial overlay or dialog
   - Add "?" icons next to confusing UI elements with tooltips

3. **Improve results list readability**:
   - Add visual separators between different files in results
   - Use alternating background colors or dividers
   - Make file headers more prominent (bold, different background)
   - Show file path more clearly in results

4. **Add empty state messages**:
   - When no directories selected: "Please add and select a target directory"
   - When no extensions selected: "Please add and select file extensions"
   - When no results found: "No matches found. Try adjusting your search."

**Files**: `app/src/main/java/jp/sblo/pandora/aGrep/Search.java`, `TextPreview.java`, layout files, add new Help activity

### 🟡 7. File path display issues
**Reviews**: #12, #50, #130
**Issue**: File paths displayed from beginning, important parts get cut off; need better path visibility

**Tasks**:
1. Display path from right-to-left (show filename and immediate parent first)
2. Add ellipsis in the middle of long paths instead of end: `/storage/.../folder/file.txt`
3. Make paths scrollable horizontally or show full path on long-press
4. Add option to show relative path vs absolute path
5. Display full path in tooltip/popup on tap
6. Consider showing path in multiple lines for better readability
7. Add "Copy path" action in context menu

**Files**: `app/src/main/java/jp/sblo/pandora/aGrep/Search.java`, `TextPreview.java`, result list adapters

---

## Medium Priority - Feature Requests

### 🟢 8. Export search results to file
**Review**: #5
**Feature**: Save/export search results to a text or CSV file

**Tasks**:
1. Add "Export Results" button/menu item in results screen
2. Implement export to text file (simple format: path:line_number:matched_line)
3. Implement export to CSV format (columns: file_path, line_number, line_content, match_count)
4. Use Storage Access Framework for saving files on modern Android
5. Add progress dialog for large result exports
6. Add sharing capability (share results via email, messaging, etc.)
7. Include search parameters in export header (query, directories, extensions, options)
8. Add preference for default export format

**Files**: `app/src/main/java/jp/sblo/pandora/aGrep/Search.java`, add new ExportManager class

### 🟢 9. Find and replace functionality
**Reviews**: #22, #56, #97
**Feature**: Add ability to replace found text, not just search

**Tasks**:
1. **Phase 1 - Preview mode**:
   - Add "Replace" tab/mode in main screen
   - Add "Replace with" text field
   - Show preview of changes before applying
   - Display count of matches that will be replaced

2. **Phase 2 - Replace execution**:
   - Implement batch replace across multiple files
   - Add option for "Replace All" vs "Replace in selected files"
   - Create backup/undo mechanism (optional backup before replace)
   - Add confirmation dialog with summary before executing

3. **Phase 3 - Safety features**:
   - Add "Dry run" mode showing what would be replaced
   - Implement regex capture group support for advanced replacements
   - Add file filter options (exclude certain files from replacement)
   - Log all replacements for audit trail

4. **Testing**:
   - Test with various encodings (UTF-8, Shift-JIS, etc.)
   - Test with large files
   - Test undo/backup functionality
   - Add warnings for dangerous operations (replacing in many files)

**Files**: Create new `ReplaceEngine.java`, update `Search.java` UI, add replace layouts

### 🟢 10. Widget support
**Review**: #45
**Feature**: Home screen widget for quick searches with saved settings

**Tasks**:
1. Design simple widget layout (search field + go button)
2. Implement AppWidgetProvider for aGrep
3. Allow widget to use last search settings or configure saved search profile
4. Widget tap opens main app with results
5. Support multiple widget instances with different search profiles
6. Add widget configuration activity for customization
7. Consider different widget sizes (1x1, 2x1, 4x1)

**Files**: Create `SearchWidget.java`, `SearchWidgetProvider.java`, widget layouts, update AndroidManifest.xml

### 🟢 11. Search single specific file
**Reviews**: #121, #142
**Feature**: Add ability to search just one specific file instead of folder + extension pattern

**Tasks**:
1. Add "File" mode toggle alongside "Folders" and "Extensions" modes
2. In file mode, show file picker instead of folder picker
3. Allow adding multiple specific files to search list
4. Store file selections separately from folder selections
5. Update search engine to handle both modes
6. Update UI to clearly show which mode is active
7. Allow mixing both modes (some folders + some specific files)

**Files**: `app/src/main/java/jp/sblo/pandora/aGrep/Search.java`, `FileSelector.java`, `ExecutorGrepEngine.java`

### 🟢 12. Copy file path to clipboard
**Review**: #62
**Feature**: Add ability to copy the full file path where a match was found

**Tasks**:
1. Add "Copy path" to context menu for result items
2. Add "Copy relative path" option as well
3. Show toast confirmation when path copied
4. Consider adding "Copy all paths" for multiple selections
5. Add "Share path" option to send path via other apps

**Files**: `app/src/main/java/jp/sblo/pandora/aGrep/Search.java`, result list adapter

### 🟢 13. Regex help and examples
**Reviews**: #43, #140
**Feature**: In-app documentation and examples for regex usage

**Tasks**:
1. Create regex help screen/dialog
2. Include common regex patterns:
   - Match any character: `.`
   - Match word boundary: `\b`
   - Match digits: `\d`
   - Match whitespace: `\s`
   - Quantifiers: `*`, `+`, `?`, `{n,m}`
   - Groups and alternation: `()`, `|`
3. Add practical examples:
   - Email addresses: `\b[\w._%+-]+@[\w.-]+\.[A-Z]{2,}\b`
   - Phone numbers, URLs, dates, etc.
4. Add "Examples" button next to regex checkbox
5. Allow tapping example to populate search field
6. Link to full Java Pattern documentation for advanced users

**Files**: Create `RegexHelpActivity.java` and layouts, update `Search.java`

### 🟢 14. More granular font sizes
**Review**: #6
**Feature**: Add more font size options (11, 12, 13 pt in addition to current sizes)

**Tasks**:
1. Update font size preference to use slider or list of more sizes
2. Add sizes: 8, 9, 10, 11, 12, 13, 14, 16, 18, 20, 22, 24
3. Apply font size consistently across results list and text viewer
4. Save preference persistently
5. Update preferences UI to show preview of selected size

**Files**: `app/src/main/java/jp/sblo/pandora/aGrep/Prefs.java`, preference layouts, `TextPreview.java`

---

## Low Priority - Nice to Have

### 🔵 15. Voice interface
**Review**: #21
**Feature**: Voice input for search queries

**Tasks**:
1. Add microphone button next to search field
2. Integrate Android Speech Recognition API
3. Convert voice input to text and populate search field
4. Handle voice errors gracefully
5. Consider adding voice commands for other actions (not just search text)

**Files**: `app/src/main/java/jp/sblo/pandora/aGrep/Search.java`

### 🔵 16. Search in filenames (in addition to content)
**Review**: #91
**Feature**: Option to search in filenames, not just file contents

**Tasks**:
1. Add checkbox: "Also search in filenames"
2. When enabled, also match against file paths/names
3. Display filename matches separately from content matches in results
4. Add option to search "Only filenames" vs "Only content" vs "Both"
5. Update result display to clearly indicate filename vs content matches

**Files**: `app/src/main/java/jp/sblo/pandora/aGrep/ExecutorGrepEngine.java`, `Search.java`

### 🔵 17. Better file result separators
**Review**: #50
**Feature**: Improve visual distinction between different files in results list

**Tasks**:
1. Add clear visual dividers between files
2. Use alternating background colors (e.g., light gray, white)
3. Make file header rows sticky when scrolling
4. Add file icon and metadata (size, modified date) in header
5. Add collapse/expand for each file's results

**Files**: `app/src/main/java/jp/sblo/pandora/aGrep/Search.java`, result list layouts and adapters

### 🔵 18. Performance optimizations
**Reviews**: #36, #58
**Feature**: Improve search speed to compete with command-line grep

**Tasks**:
1. Profile current performance bottlenecks (already documented in codex_tasks_quality.md)
2. Implement parallel file processing across multiple threads
3. Optimize encoding detection (cache UniversalDetector instances)
4. Add native code (JNI) for hot path if needed
5. Benchmark against command-line grep and document results
6. Add progress indicators showing files/second processing rate

**Note**: This overlaps with existing performance tasks in `codex_tasks_quality.md` - consolidate efforts.

**Files**: `app/src/main/java/jp/sblo/pandora/aGrep/ExecutorGrepEngine.java`

---

## Implementation Priority Roadmap

### Sprint 1 - Critical Fixes (Weeks 1-2)
- 🔴 Fix special character crash (#2)
- 🔴 Fix file size limit (#3)
- 🟡 Fix phrase search (#5)

### Sprint 2 - Storage & Compatibility (Weeks 3-4)
- 🟡 External SD card / OTG support (#4)
- 🟡 Path display improvements (#7)

### Sprint 3 - UX Improvements (Weeks 5-6)
- 🟡 UI/UX intuitiveness (#6)
- 🟢 Copy file path (#12)
- 🟢 Regex help (#13)

### Sprint 4 - Major Features (Weeks 7-10)
- 🟢 Export results (#8)
- 🟢 Find and replace (#9)

### Sprint 5 - Enhancement Features (Weeks 11-13)
- 🟢 Single file search (#11)
- 🟢 Widget support (#10)
- 🟢 Font size options (#14)

### Sprint 6 - Polish & Nice-to-Haves (Weeks 14-15)
- 🔵 Filename search (#16)
- 🔵 Better separators (#17)
- 🔵 Voice interface (#15)
- 🔵 Performance optimizations (#18) - coordinate with existing performance plan

---

## Testing Strategy

For each implemented feature/fix:
1. **Unit tests**: Core logic and edge cases
2. **Instrumentation tests**: UI interactions and integration
3. **Manual testing**: Real devices with various Android versions
4. **Regression testing**: Ensure existing functionality still works
5. **Performance testing**: Measure impact on search speed and memory
6. **User acceptance**: Beta testing with subset of users

## Documentation Updates

After implementing each feature:
1. Update in-app help text
2. Update README with new features
3. Update Play Store description
4. Create release notes
5. Consider creating video tutorial for complex features
6. Update screenshots in Play Store listing

## Success Metrics

Track these metrics to measure improvement:
- Crash-free rate increase
- Average rating improvement on Play Store
- Reduction in "doesn't work" reviews
- User engagement (sessions per user, retention rate)
- Feature adoption rates (for new features)
