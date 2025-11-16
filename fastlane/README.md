# Fastlane Configuration for aGrep-again

This directory contains Fastlane configuration for automating releases to F-Droid and Google Play Store.

## Setup

1. Install Fastlane:
   ```bash
   sudo gem install fastlane
   ```

2. For Google Play Store uploads, you'll need to:
   - Create a Google Play Console service account
   - Download the JSON key file
   - Update the `json_key_file` path in `Appfile`
   - See: https://docs.fastlane.tools/actions/upload_to_play_store/#setup

## Available Lanes

### Beta Lane
Builds a release APK for testing:
```bash
fastlane beta
```
This will:
- Clean the project
- Build a release APK
- Output the APK location

### F-Droid Lane
Prepares the app for F-Droid submission:
```bash
fastlane fdroid
```
This will:
- Verify metadata exists
- Build a test APK to ensure everything works
- Provide guidance on metadata requirements

F-Droid builds apps from source, so make sure:
- All metadata is up to date in `metadata/android/`
- Screenshots are added to `metadata/android/en-US/images/`
- The app builds successfully with Gradle

### Google Play Store Lane
Uploads to Google Play Store:
```bash
fastlane playstore
```
This will:
- Build a release AAB (Android App Bundle)
- Upload to Google Play Store internal track
- Upload metadata and screenshots

For beta releases:
```bash
fastlane playstore_beta
```

## Metadata Structure

```
fastlane/
├── Appfile                 # App configuration
├── Fastfile               # Lane definitions
└── metadata/
    └── android/
        └── en-US/         # English metadata (add more locales as needed)
            ├── title.txt
            ├── short_description.txt
            ├── full_description.txt
            ├── changelogs/
            │   └── 1.txt  # Changelog for versionCode 1
            └── images/
                ├── icon.png                    # App icon
                ├── phoneScreenshots/           # Phone screenshots (max 8)
                ├── sevenInchScreenshots/       # 7" tablet screenshots
                └── tenInchScreenshots/         # 10" tablet screenshots
```

## Adding Screenshots

1. Take screenshots of your app (ideally on different device sizes)
2. Add them to the appropriate directories:
   - `phoneScreenshots/` - For phones
   - `sevenInchScreenshots/` - For 7" tablets
   - `tenInchScreenshots/` - For 10" tablets

Screenshots should be:
- PNG or JPG format
- Named sequentially (e.g., 1.png, 2.png, 3.png)
- Maximum 8 screenshots per device type

## Updating Metadata

Edit the text files in `metadata/android/en-US/`:
- `title.txt` - App name (max 50 characters)
- `short_description.txt` - Brief description (max 80 characters)
- `full_description.txt` - Full app description (max 4000 characters)

## Changelogs

Create a new file for each version in `changelogs/`:
- Filename should be the versionCode (e.g., `1.txt`, `2.txt`)
- Max 500 characters

## More Information

- Fastlane docs: https://docs.fastlane.tools/
- F-Droid docs: https://f-droid.org/docs/
- Google Play Store setup: https://docs.fastlane.tools/actions/upload_to_play_store/
