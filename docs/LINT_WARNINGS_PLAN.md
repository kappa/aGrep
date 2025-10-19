# Lint Warnings Resolution Plan

## Current Status
- **Errors**: 0 ✅
- **Warnings**: 39
- **Last Updated**: 2025-10-19

---

## Analysis of 39 Warnings - Categorized by Priority

### HIGH PRIORITY - Should Fix (5 warnings)

#### 1. StaticFieldLeak - `TextViewer.TextLoadTask` (1 warning)
**File**: `app/src/main/java/jp/sblo/pandora/aGrep/TextViewer.java:162`

**Issue**: Non-static inner class holds reference to Activity, can cause memory leak

```java
class TextLoadTask extends AsyncTask<Uri, Integer, Boolean >{
      ~~~~~~~~~~~~
```

**Explanation**:
- Non-static inner classes have an implicit reference to their outer class
- If outer class is an Activity, the AsyncTask will prevent the Activity from being garbage collected
- This causes memory leaks if the task outlives the Activity

**Fix**: Make the class static and use WeakReference for the Activity
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
        // ... rest of implementation
    }
}
```

**Impact**: Prevents memory leaks

---

#### 2. InefficientWeight - `fileselector.xml` (1 warning)
**File**: `app/src/main/res/layout/fileselector.xml:16`

**Issue**: LinearLayout with weight should use 0dp for better performance

```xml
android:layout_height="wrap_content"
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
```

**Explanation**:
- When a widget in LinearLayout has `layout_weight`, it will consume remaining space
- Using `wrap_content` forces the widget to measure itself first, then apply weight
- Using `0dp` skips the initial measurement, more efficient

**Fix**: Change to `android:layout_height="0dp"`

**Impact**: Minor performance improvement in layout measurement

---

#### 3. KeyboardInaccessibleWidget - `fileselector.xml` (1 warning)
**File**: `app/src/main/res/layout/fileselector.xml:19`

**Issue**: Clickable widget without focusable attribute - accessibility issue

```xml
android:clickable="true"
~~~~~~~~~~~~~~~~~~~~~~~~
```

**Explanation**:
- Keyboard users cannot focus on elements that are clickable but not focusable
- This violates accessibility guidelines

**Fix**: Add `android:focusable="true"` alongside `android:clickable="true"`

**Impact**: Better accessibility for keyboard and switch device users

---

#### 4. ClickableViewAccessibility - `GrepView.java` (1 warning)
**File**: `app/src/main/java/jp/sblo/pandora/aGrep/GrepView.java:110`

**Issue**: Custom view overrides `onTouchEvent` but not `performClick` - accessibility issue

```java
public boolean onTouchEvent(MotionEvent ev)
           ~~~~~~~~~~~~
```

**Explanation**:
- Accessibility services (TalkBack, Switch Access) invoke `performClick()` instead of sending touch events
- If you handle clicks in `onTouchEvent` but don't implement `performClick()`, accessibility users can't interact with the view

**Fix**: Override `performClick()` and call it from `onTouchEvent()` when click is detected
```java
@Override
public boolean performClick() {
    super.performClick();
    // Handle click logic here
    return true;
}

@Override
public boolean onTouchEvent(MotionEvent ev) {
    // Detect click gesture
    if (isClickGesture) {
        performClick(); // Call performClick instead of handling directly
        return true;
    }
    return super.onTouchEvent(ev);
}
```

**Impact**: Enables accessibility services to work properly

---

#### 5. NotifyDataSetChanged - `Settings.java` (1 warning)
**File**: `app/src/main/java/jp/sblo/pandora/aGrep/Settings.java:330`

**Issue**: Using generic refresh instead of specific item changes

```java
notifyDataSetChanged();
~~~~~~~~~~~~~~~~~~~~~~
```

**Explanation**:
- `notifyDataSetChanged()` forces RecyclerView to rebind all visible items
- More specific methods like `notifyItemInserted()`, `notifyItemRemoved()`, `notifyItemChanged()` only update affected items
- This enables animations and better performance

**Fix Options**:
1. If entire list changes: Keep as-is, this is fine
2. If specific items change: Use `notifyItemChanged(position)`, `notifyItemInserted(position)`, etc.

**Current Usage Context**: Called in `updateItems()` which replaces the entire list
**Recommendation**: Might be acceptable as-is since the entire dataset changes

**Impact**: Better RecyclerView performance and animations (if applicable)

---

### MEDIUM PRIORITY - Consider Fixing (2 warnings)

#### 6. InlinedApi - `Search.java` using `ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION` (1 warning)
**File**: `app/src/main/java/jp/sblo/pandora/aGrep/Search.java:390`

**Issue**: Using API 30+ constant on minSdk 21

```java
Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                           ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
```

**Explanation**:
- `ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION` was added in API 30 (Android 11)
- App minSdk is 21
- However, constants are inlined at compile time, so the string value is copied into the APK
- This is actually safe - the constant value works on all Android versions

**Why it's OK**:
- The dialog containing this code is only shown when the app needs "All Files Access" permission
- This permission doesn't exist on Android < 11, so this code path won't be reached on older devices

**Fix Options**:
1. **Suppress with explanation**: `@SuppressLint("InlinedApi")` with comment
2. **Use string constant**: Define the string ourselves instead of using the system constant
3. **Leave as-is**: It's safe, just noisy

**Recommendation**: Suppress with comment explaining why it's safe

**Example**:
```java
// API 30+ constant is safe to use - value is inlined at compile time
// This dialog only shown on Android 11+ where permission exists
@SuppressLint("InlinedApi")
Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
```

---

#### 7. ScopedStorage - Using deprecated `READ_EXTERNAL_STORAGE` (1 warning)
**File**: `app/src/main/AndroidManifest.xml:5`

**Issue**: Permission deprecated on Android 13+ (API 33)

```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

**Explanation**:
- Android 13+ introduced granular media permissions: `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `READ_MEDIA_AUDIO`
- `READ_EXTERNAL_STORAGE` is no longer granted on Android 13+
- Apps should request specific media permissions or use SAF

**Current Situation**:
- App already migrated to SAF (Storage Access Framework)
- SAF doesn't require permissions - user grants access per-directory
- This permission might be vestigial from pre-SAF implementation

**Investigation Needed**:
1. Check if app actually uses this permission anywhere
2. Check if permission is needed for any legacy code paths
3. Determine if permission can be removed entirely

**Fix Options**:
1. **Remove permission entirely** if SAF handles all file access
2. **Add maxSdkVersion**: `<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="32" />`
3. **Add granular permissions** for Android 13+:
   ```xml
   <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="32" />
   <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
   <uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
   <uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />
   ```

**Recommendation**: Research actual usage, then likely add `maxSdkVersion="32"` or remove entirely

---

### LOW PRIORITY - Can Disable/Suppress (31 warnings)

#### 8. UnusedResources (20 warnings)

**Files**:
- `app/src/main/res/values/colors.xml` - 10 color resources
- `app/src/main/res/layout/*.xml` - 3 layouts: `file_row`, `fileselector`, `list_dir`
- `app/src/main/res/values/strings.xml` - 7 strings

**Unused Colors** (10):
- `color_primary`, `color_primary_variant`, `color_secondary`, `color_secondary_variant`
- `color_surface`, `color_on_primary`, `color_on_secondary`, `color_on_surface`
- `color_background`, `color_on_background`

**Unused Layouts** (3):
- `file_row.xml` - Old layout, replaced by newer implementation
- `fileselector.xml` - Legacy file selector UI
- `list_dir.xml` - Old directory list item

**Unused Strings** (7):
- `app_selector` - "Select directory"
- `grep_canceled` - "Canceled."
- `label_YES` / `label_NO` - "Yes" / "No"
- `label_long_press` - "LongPress to apply the directory."
- `label_migrate_directories_message` - Migration message (different from blocking message)

**Why They Might Exist**:
- Legacy code kept for reference
- Future features
- Used in build variants not being checked
- False positives from lint

**Fix Options**:
1. **Delete unused resources** - Clean up, reduce APK size
2. **Disable lint check** - Keep resources, silence warnings
3. **Mark with tools:ignore** - Keep specific resources, silence individually

**Recommendation**: Disable the lint check globally, clean up resources in separate housekeeping task

**Lint Config**:
```gradle
lint {
    disable 'UnusedResources'
}
```

---

#### 9. GradleDependency (7 warnings - outdated dependencies)

**Files**: `app/build.gradle`

**Outdated Dependencies**:
1. `compileSdk 34` → 36 available
2. `androidx.appcompat:appcompat:1.6.1` → 1.7.1 available
3. `com.google.android.material:material:1.11.0` → 1.13.0 available
4. `androidx.lifecycle:lifecycle-runtime:2.7.0` → 2.9.4 available
5. `androidx.core:core-ktx:1.12.0` → 1.17.0 available
6. `androidx.test.ext:junit:1.1.5` → 1.3.0 available
7. `androidx.test.espresso:espresso-core:3.5.1` → 3.7.0 available

**Why Not Update Immediately**:
- Dependency updates can introduce breaking changes
- Need to test after each update
- Some updates might require code changes
- Better to batch updates and test thoroughly

**Recommendation**:
- Create separate task for dependency updates
- Test thoroughly after updating
- Disable this lint check to reduce noise

**Lint Config**:
```gradle
lint {
    disable 'GradleDependency'
}
```

---

#### 10. OldTargetApi (1 warning - targetSdk 34 vs 36)

**File**: `app/build.gradle:12`

**Issue**: Not targeting the absolute latest Android version

```gradle
targetSdk 34
```

**Current Latest**: 36 (Android 16 preview)

**Why Not Update Immediately**:
- Each targetSdk increase requires handling new behavior changes
- Android 15 (API 35) introduced many breaking changes
- Need thorough testing on new Android versions
- Google Play requires targetSdk 34+ (already compliant)

**Recommendation**:
- Create separate task for targetSdk update
- Test on Android 15+ devices
- Review behavior changes documentation
- Disable lint check for now

**Lint Config**:
```gradle
lint {
    disable 'OldTargetApi'
}
```

---

#### 11. PluralsCandidate (1 warning)

**File**: `app/src/main/res/values/strings.xml:35`

**Issue**: Progress string doesn't handle singular/plural properly

```xml
<string name="progress">%1$s (in %2$d files)</string>
```

**Problem**:
- English: "in 1 files" is grammatically incorrect (should be "in 1 file")
- Other languages have different plural rules
- Should use `<plurals>` resource

**Fix**:
```xml
<plurals name="progress">
    <item quantity="one">%1$s (in %2$d file)</item>
    <item quantity="other">%1$s (in %2$d files)</item>
</plurals>
```

**Code change needed**:
```java
// Old
getString(R.string.progress, query, fileCount)

// New
getResources().getQuantityString(R.plurals.progress, fileCount, query, fileCount)
```

**Impact**: Minor i18n improvement, fixes grammatical error for count=1

**Recommendation**: Low priority, can fix if desired. Disable lint check if not planning to fix soon.

---

#### 12. IconDensities (3 warnings)

**Files**:
- `app/src/main/res/drawable-hdpi/` - Missing: add.png, clr.png, history.png, search.png
- `app/src/main/res/drawable-xhdpi/` - Missing: add.png, clr.png, history.png, ic_menu_preferences.png, ic_menu_share.png, search.png
- `app/src/main/res/drawable-xxhdpi/` - Missing same as xhdpi

**Issue**: Missing icons in some density folders

**Impact**:
- Android will scale from nearest available density
- Might look slightly blurry on some devices
- Not a functional issue, just quality

**Recommendation**: Design/asset creation task, not a code task. Disable lint check.

---

#### 13. IconDipSize (1 warning)

**File**: `app/src/main/res/drawable-xhdpi/icon.png`

**Issue**: App icon varies in density-independent size across densities
- drawable-hdpi: 48x48 dp (72x72 px)
- drawable-mdpi: 48x48 dp (48x48 px)
- drawable-xhdpi: 48x48 dp (96x96 px)
- **drawable-xxhdpi: 64x64 dp (192x192 px)** ← Different!

**Problem**: xxhdpi icon is wrong size, should be 144x144 px for 48dp

**Fix**: Resize xxhdpi icon to 144x144 px

**Recommendation**: Design task. Can fix by resizing icon or disable lint check.

---

## Summary of Recommendations

### Fix Now (Easy & High Impact)
1. ✅ **StaticFieldLeak** - Make TextLoadTask static with WeakReference
2. ✅ **InefficientWeight** - Change layout_height to 0dp
3. ✅ **KeyboardInaccessibleWidget** - Add android:focusable="true"
4. ✅ **ClickableViewAccessibility** - Implement performClick()

### Suppress with Explanation
5. ✅ **InlinedApi** - Add @SuppressLint with comment explaining why it's safe

### Investigate & Decide
6. ❓ **ScopedStorage** - Research if READ_EXTERNAL_STORAGE still needed, likely add maxSdkVersion="32"
7. ❓ **NotifyDataSetChanged** - Check if entire list changes, might be OK as-is

### Disable in Lint Config (Low Value)
8. ✅ **UnusedResources** - Disable check, clean up in separate housekeeping task
9. ✅ **GradleDependency** - Disable check, update dependencies in separate task
10. ✅ **OldTargetApi** - Disable check, update targetSdk in separate task
11. ✅ **IconDensities** - Disable check, asset creation task
12. ✅ **IconDipSize** - Disable check or fix icon size

### Optional (Low Priority)
13. ❓ **PluralsCandidate** - Minor i18n improvement, fix if desired

---

## Proposed Lint Configuration

Add to `app/build.gradle`:

```gradle
android {
    // ...

    lint {
        // Already disabled
        disable 'MissingTranslation'

        // Disable low-priority checks
        disable 'UnusedResources'      // Clean up separately
        disable 'GradleDependency'     // Update dependencies separately
        disable 'OldTargetApi'         // Update targetSdk separately
        disable 'IconDensities'        // Asset creation task
        disable 'IconDipSize'          // Asset creation task

        // Optional
        // disable 'PluralsCandidate'  // Minor i18n issue
    }
}
```

This would reduce warnings from 39 to approximately **5-7 actionable warnings**.

---

## Implementation Checklist

### Phase 1: Quick Fixes (30 mins)
- [ ] Fix StaticFieldLeak in TextViewer.java
- [ ] Fix InefficientWeight in fileselector.xml
- [ ] Fix KeyboardInaccessibleWidget in fileselector.xml
- [ ] Fix ClickableViewAccessibility in GrepView.java
- [ ] Add lint disables to build.gradle

### Phase 2: Suppressions (10 mins)
- [ ] Suppress InlinedApi in Search.java with explanation

### Phase 3: Research & Decide (variable)
- [ ] Investigate READ_EXTERNAL_STORAGE usage
- [ ] Decide on ScopedStorage fix approach
- [ ] Review NotifyDataSetChanged usage context

### Phase 4: Optional Improvements (variable)
- [ ] Convert progress string to plurals
- [ ] Update dependencies (separate PR)
- [ ] Update targetSdk (separate PR)
- [ ] Fix icon assets (design task)

---

## Testing After Fixes

1. Run lint: `./gradlew lintDebug`
2. Build app: `./gradlew assembleDebug`
3. Test on device:
   - File operations work
   - Accessibility services work (TalkBack)
   - No memory leaks (Android Studio Profiler)
   - Keyboard navigation works

---

## Expected Result

**After Phase 1 & 2**:
- Warnings reduced from 39 to ~5-7
- All high-priority accessibility and performance issues fixed
- Remaining warnings are research items or future tasks

**Final Target**:
- 0 errors ✅
- 0-5 warnings (only investigative items remaining)
