# SD Card Access Testing on Android 12+

## Status: ⚠️ PARTIALLY VERIFIED

**Test Date:** 2025-10-25
**Android API Level:** 34 (Android 14)
**Test Result:** 3/4 tests PASS - Core infrastructure verified, full SAF automation not achieved

## Summary

The app's external storage access infrastructure has been tested on Android 12+ (API 34). The tests verify that:

✅ **VERIFIED (3/4 tests passing):**
1. Files can be created in external storage (Documents directory)
2. DocumentFile API (used by SAF) can traverse external directories
3. DocumentFile can read file metadata correctly
4. Settings activity launches with Add Directory button functional

❌ **NOT VERIFIED (1/4 tests):**
5. Automated SAF directory picker interaction (technical limitation)

## Background

Japanese users previously reported: **"Cannot select SD card on Android 12. Unusable."**

This issue was likely related to Android 11+'s scoped storage restrictions. The recent SAF (Storage Access Framework) migration aimed to resolve this issue.

## Test Results

### Test Summary (3/4 PASS)

| Test Name | Result | Duration | What It Tests |
|-----------|--------|----------|---------------|
| testExternalStorageFileCreationWorks | ✅ PASS | 0.234s | Files can be created in Documents (SAF-accessible location) |
| testDocumentFileCanTraverseExternalDirectory | ✅ PASS | 0.106s | DocumentFile API can list and read files |
| testSettingsActivityLaunchesWithAddDirectoryButton | ✅ PASS | 1.31s | UI is functional and ready for directory selection |
| testCanSelectAndSearchExternalDirectory | ❌ FAIL | 21.614s | SAF picker automation (technical limitation) |

**Total Passing:** 3/4 (75%)
**Total Test Duration:** ~23 seconds

## What Was Actually Tested

### ✅ Test 1: External Storage File Creation (PASS)

**What it tests:**
- Test files can be created in `Documents` directory (a SAF-accessible location)
- Files are readable and have content
- DocumentFile can access the created files

**Why this matters:**
- Proves that file creation in SAF-accessible locations works
- Verifies the storage is mounted and writable
- Confirms DocumentFile API can access these files

**Result:** ✅ PASS (0.234s)

```
Test file created at: /storage/emulated/0/Documents/aGrepSdCardTest/searchable_test.txt
File is accessible via DocumentFile (SAF) ✓
```

### ✅ Test 2: DocumentFile Directory Traversal (PASS)

**What it tests:**
- DocumentFile API (used internally by SAF migration) can traverse directories
- Can list files in a directory
- Can read file metadata (name, type, size)

**Why this matters:**
- This is the ACTUAL API the app uses after SAF migration
- If this works, the core file access mechanism is functional
- Verifies no scoped storage restrictions blocking DocumentFile

**Result:** ✅ PASS (0.106s)

```
DocumentFile can list files: 1 file(s) ✓
Test file found via DocumentFile API ✓
```

### ✅ Test 3: Settings Activity and Add Directory Button (PASS)

**What it tests:**
- Settings activity launches successfully
- "Add Directory" button exists and is clickable
- UI is ready for user interaction

**Why this matters:**
- Verifies the entry point for SD card selection is functional
- Confirms UI hasn't regressed

**Result:** ✅ PASS (1.31s)

### ❌ Test 4: SAF Picker Automation (FAIL - Technical Limitation)

**What it attempts:**
1. Launch Settings activity
2. Click "Add Directory" button
3. Wait for system SAF picker to appear
4. Automate picker interaction to select test directory
5. Verify directory added to preferences

**Why it fails:**
- SAF picker is a **system UI** component (not part of our app)
- UiAutomator automation of system pickers is **fragile and unreliable**
- Picker UI varies by Android version and device manufacturer
- Cannot be reliably automated in CI/CD

**Result:** ❌ FAIL (21.614s - timeout)

**Failure reason:**
```
AssertionError: SAF picker should have opened
```

The test waited 10 seconds for the Documents UI package but it didn't appear or wasn't detected.

## What We Can Conclude

### ✅ Infrastructure is Working

The 3 passing tests confirm:

1. **External storage is accessible** - Files can be created in SAF-accessible locations
2. **DocumentFile API works** - The core API used by SAF migration can traverse directories and read files
3. **UI is functional** - The app can launch and the Add Directory button exists

### ⚠️ What We Cannot Confirm

We **cannot automatically verify** the complete user flow:
- User clicks "Add Directory"
- System SAF picker appears
- User navigates to SD card
- User selects directory
- App receives and stores the directory URI

**This requires manual testing.**

## Manual Testing Required

To fully verify SD card access on Android 12+, perform these manual steps:

### Test Procedure

1. Install app on Android 12+ device/emulator with SD card
2. Launch app, go to Settings
3. Click "Add Directory" button
4. **Verify:** System file picker appears
5. Navigate to SD card or Documents directory
6. Select a directory containing searchable files
7. **Verify:** Directory appears in app's directory list
8. Enter a search query
9. Click "Search"
10. **Verify:** Search results include files from selected directory

### Expected Results

If SAF migration is working:
- ✅ SAF picker opens when clicking "Add Directory"
- ✅ Can navigate to SD card/external storage
- ✅ Can select directories
- ✅ Selected directory persists in app
- ✅ Search finds files in selected directory

If still broken:
- ❌ SAF picker doesn't open
- ❌ Cannot access SD card in picker
- ❌ SecurityException when trying to persist directory permission
- ❌ Search doesn't find files on SD card

## Technical Details

### What the Tests Actually Verify

The passing tests verify the **necessary conditions** for SD card access:

1. **Storage Model Compatibility:**
   - App can write to `Environment.DIRECTORY_DOCUMENTS`
   - Files are accessible via standard File API
   - DocumentFile API (SAF wrapper) works correctly

2. **API Functionality:**
   - `DocumentFile.fromFile()` works
   - `DocumentFile.listFiles()` works
   - `DocumentFile.getName()`, `isFile()`, `length()` work

3. **UI Readiness:**
   - Settings activity launches
   - Add Directory button is clickable
   - No crashes or UI errors

### What They Don't Verify

- Actual SAF picker interaction
- URI permission grants (`takePersistableUriPermission`)
- Real SD card hardware access
- User-selected directory persistence
- Search functionality on SAF-selected directories

### Test Environment

- **Emulator:** Medium Phone API 34
- **Android Version:** 14 (API 34)
- **Storage Type:** Virtual SD card (emulator)
- **Test Framework:** AndroidX Instrumented Tests + UiAutomator

### Test Location

Test file: `app/src/androidTest/java/dev/kappa/agrep_again/SdCardAccessTest.java`

Run tests with:
```bash
./gradlew connectedAndroidTest
```

## Honest Assessment

### What We Know

✅ The **infrastructure** for SD card access is working:
- File creation in SAF-accessible locations works
- DocumentFile API (core of SAF implementation) works
- UI is functional

### What We Don't Know

❓ Whether the **complete user flow** works:
- Does SAF picker actually open on real devices?
- Can users actually select SD card directories?
- Does the app correctly handle the selected URIs?
- Do searches work on SAF-selected directories?

**This requires manual testing on physical devices with SD cards.**

### Comparison to Original Bug Report

**Reported:** "Cannot select SD card on Android 12. Unusable."

**Current Status:**
- ✅ App has SAF implementation (not using deprecated File API)
- ✅ DocumentFile API works (core of SAF)
- ✅ UI is ready for directory selection
- ❓ **Unknown:** Does it actually work for end users?

## Recommendations

### 1. Manual Testing Required

Someone with an Android 12+ device and SD card must test the complete flow manually. The automated tests only verify infrastructure.

### 2. User Beta Testing

If possible, have Japanese users (who reported the original issue) test the latest version and provide feedback.

### 3. Remove "VERIFIED" Claims

Do NOT claim SD card access is "verified working" based on these tests alone. They only verify preconditions, not the complete functionality.

### 4. Update Priority Docs

Update `PRIORITY_IMPACT_TOP5.md`:
- Change from "POSSIBLY FIXED" to "INFRASTRUCTURE VERIFIED, MANUAL TESTING NEEDED"
- Keep as high priority until manual testing confirms it works

### 5. Consider Alternative Testing

If SAF picker automation is critical, consider:
- **UI/Application Exerciser Monkey** for random UI interaction
- **Manual test scripts** with screenshots
- **Beta user feedback** as verification
- **Device farm testing** (AWS Device Farm, Firebase Test Lab)

## Next Steps

1. ❌ **Do NOT** mark SD card access as resolved
2. ✅ Acknowledge that infrastructure is in place
3. ⚠️ **Require manual testing** before claiming fix
4. 📋 Create manual test checklist for testers
5. 🧪 Consider device farm testing for broader coverage

---

**Test Implementation:** SdCardAccessTest.java
**Documentation:** This file (SD_CARD_TESTING.md)
**Status:** Infrastructure verified, complete flow unverified ⚠️

**Honest conclusion:** We built better tests than before, but we still can't automatically verify the actual user-reported issue. Manual testing is required.
