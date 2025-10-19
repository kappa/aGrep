# GitHub Issues - Task List

Tasks derived from open issues at https://github.com/jiro-aqua/aGrep/issues

## Issue #10: Unable to copy from the search results

**Status**: Open
**Platform**: Reported on Android 8.1
**Description**: Users cannot select or copy text from search results, either in the results list or when viewing occurrences in the text file view.

**Tasks**:
1. Investigate why text selection is disabled in the search results list adapter (`app/src/main/java/jp/sblo/pandora/aGrep/Search.java`).
2. Enable text selection for result items - implement proper `TextView` configuration or add copy-to-clipboard functionality via context menu.
3. Verify text selection works correctly in `TextViewer` when viewing individual search result occurrences.
4. Test on multiple Android versions (8.1, current target) to ensure consistent behavior.

## Issue #4, #5, #6: State loss on device rotation

**Status**: Open
**Description**: Multiple UI state issues occur during orientation changes, affecting checkboxes, dialogs, and context menus.

**Affected scenarios**:
- Checkbox selections (target extensions, target directories) reset on rotation
- Color picker dialogs (Highlight BG/Text Color) disappear on rotation
- Target Extensions dialog disappears on rotation
- Context menus from long-press disappear on rotation

**Tasks**:
1. Implement `ViewModel` or `onSaveInstanceState`/`onRestoreInstanceState` for main search activity to preserve checkbox states (`app/src/main/java/jp/sblo/pandora/aGrep/Search.java`).
2. Fix color picker dialog state preservation - ensure `ColorPickerActivity` handles configuration changes properly (`app/src/main/java/jp/sblo/pandora/aGrep/ColorPickerActivity.java`).
3. Preserve extension dialog state across rotations using `DialogFragment` instead of plain dialogs if applicable.
4. Add instrumentation tests that verify state preservation across rotation for all affected UI components.
5. Document the fix approach in `docs/` once resolved.

## Issue #7: Add Russian (ru) translation

**Status**: Open
**Description**: Complete Russian translation has been contributed and is ready for integration.

**Tasks**:
1. Create `app/src/main/res/values-ru/strings.xml` file.
2. Copy the provided Russian translation XML content from issue #7 into the new file.
3. Verify all string keys match the existing English strings.xml structure.
4. Test the app with device locale set to Russian to ensure proper display.
5. Update any documentation about supported languages.
6. Credit the contributor in appropriate documentation/changelog.

## Issue #3: Add dark theme support

**Status**: Open
**Description**: User request for dark theme support to reduce eye strain in low-light conditions.

**Tasks**:
1. Evaluate current theme implementation and identify all UI components that need dark theme variants.
2. Create dark theme resources:
   - Add `themes.xml` entries for dark mode (DayNight theme or explicit dark theme)
   - Define dark color palette in `colors.xml`
   - Create dark variants for backgrounds, text colors, highlight colors
3. Update all layouts and custom views to reference theme attributes instead of hardcoded colors.
4. Add preference setting to allow users to choose theme (Light/Dark/System default).
5. Implement theme switching without requiring app restart if possible.
6. Test dark theme across all activities (Search, TextViewer, Preferences, ColorPicker).
7. Ensure highlight colors remain readable in both light and dark themes.
8. Update screenshots in documentation if needed.
