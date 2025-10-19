# Japanese Translation Tasks

## Missing Translations in values-ja/strings.xml

The following strings need professional Japanese translations. They are currently falling back to English.

### SAF Migration Messages (Added for Storage Access Framework support)

**Context**: These messages appear when the app migrated from legacy file access to Android's Storage Access Framework (SAF). They help users understand why they need to re-grant permissions.

| String ID | English Text | Notes |
|-----------|--------------|-------|
| `label_migrate_directories_title` | "Update directory access" | Dialog title |
| `label_migrate_directories_message` | "aGrep now uses the system file picker. Please re-select your directories." | Informational message (currently unused) |
| `label_migrate_directories_blocking` | "aGrep needs permission to your directories again. Open settings to re-select the folders you want to search." | Blocking message when migration needed |
| `label_permission_denied` | "Storage permission denied. Please enable it in system settings to continue searching." | Error when permission denied |
| `label_manage_storage_title` | "Allow access to all files" | Dialog title for storage manager permission |
| `label_manage_storage_message` | "Android requires \"All files access\" to search every file on this device. Continue to the next screen to grant permission." | Explanation before opening settings |
| `label_open_settings` | "Open Settings" | Button text |

---

## How to Get Translations

### Option 1: Professional Translation Service
- Use services like Gengo, One Hour Translation, or similar
- Estimated cost: ~$0.10-0.15 per word × 70 words ≈ $7-11 USD
- Provide context: "Android file search app, permission/access messages"

### Option 2: Community Translation
- Post on Reddit r/translator
- Ask Japanese-speaking Android developers
- Provide context about the app's purpose

### Option 3: AI-Assisted (Requires Verification)
- Use DeepL or Google Translate for drafts
- **Must be reviewed by native Japanese speaker**
- Technical terms should match Android's official Japanese translations

---

## How to Add Translations

Once you have verified Japanese translations:

1. Edit `/home/kappa/work/aGrep/app/src/main/res/values-ja/strings.xml`
2. Add the translations before the closing `</resources>` tag
3. Example format:
   ```xml
   <string name="label_open_settings">設定を開く</string>
   ```
4. Test the app with device language set to Japanese
5. Commit the changes

---

## Reference: Android Official Translations

For consistency, check how Android officially translates common terms:
- "Settings" → 設定
- "Permission" → 権限/許可
- "Storage" → ストレージ
- "Access" → アクセス
- "Directory/Folder" → ディレクトリ/フォルダ

Use the same terms that appear in Android's system settings.

---

## Already Marked as Non-Translatable

These strings don't need translation (technical/brand info):
- `app_name` - "aGrep" (app name)
- `version` - "aGrep version %s" (contains app name)
- `link` - "© 2014 Aquamarine Networks." (copyright)
- `iconlink` - URL

---

## Testing Translation Completeness

After adding translations, verify with:
```bash
./gradlew lintDebug
```

The MissingTranslation warning should no longer appear for these strings.

---

## Current Status

- ✅ English strings: Complete
- ❌ Japanese strings: 7 strings missing (listed above)
- 🔧 Fallback enabled: App will use English for missing Japanese strings
