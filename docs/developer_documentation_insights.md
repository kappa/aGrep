# Developer Documentation & Official Sources - Insights

Information extracted from official aGrep documentation and developer sources.

## Sources Analyzed
- Official page: https://sites.google.com/site/aquamarinepandora/home/agrep (English)
- Official page: https://sites.google.com/site/aquamarinepandora/home/agrepja (Japanese)
- Developer blog: http://pandora.sblo.jp/ (Ades Gadget Diary - アドエス小物日記)
- F-Droid repository: https://f-droid.org/packages/jp.sblo.pandora.aGrep/
- GitHub: https://github.com/jiro-aqua/aGrep

## Developer Information
- **Developer**: jiro-aqua / Aquamarine Networks
- **Twitter**: @jiro_aqua
- **Contact**: For bug reports or feature requests, contact via Twitter
- **Other apps**: Jota/Jota+ Text Editor, aDice (PDIC viewer)
- **License**: NYSL (Niru Yaru Sul License) - extremely permissive "Everyone'sWare" license
  - Free to use, redistribute, and modify
  - No warranty provided

## Official Features (from documentation)

### Core Functionality
1. **Text search from files in external storage**
   - Supports Regular Expression
   - Can search text files on SD card

2. **Asian language encoding detection**
   - Automatically detects character codes
   - Supports: Japanese, Chinese, Korean, etc.
   - Multi-encoding support (UTF-8, Shift-JIS, etc.)

3. **Integration capabilities**
   - Can be started by SEARCH-intent
   - Works with vertical text viewers and other apps that issue search intents
   - Can launch viewers that receive ACTION_VIEW intent

4. **Result viewing**
   - View results by filename, line-number, and content
   - Internal text viewer (simple viewer for full text display)
   - Can launch external apps via VIEW-intent

5. **Platform support**
   - Originally: Android 1.5 or later (docs mention 1.5 / 1.6 / 2.1 / 2.2)
   - Latest version (2014.03): Android 4.0.3+

## Version Information
- **Latest version**: 2014.03
- **Last update**: November 18, 2014
- **Package name**: jp.sblo.pandora.aGrep
- **Rating**: 4.24/5 stars (430 ratings as of search date)

## Known Limitations (from official sources)

### Explicitly Documented
1. **Cannot search System Directories**
   - App is specifically NOT for searching in system directories
   - Limited to external storage/SD card access

### Implied from Documentation
2. **Platform compatibility**
   - Designed for older Android versions (1.5-2.2 initially)
   - Current version targets Android 4.0.3+
   - Modern Android compatibility issues likely not anticipated in original design

## Icon Design
- Icon designer: 宮比のん (Non Miyabi) / @non_cozy
- Attribution: "Non's Illust Gallery"
- Site: http://sites.google.com/site/nonsillustgallery/

## Distribution Channels
1. Google Play Store
2. F-Droid (open source repository)
3. GitHub (source code)
4. Various APK mirrors (APKPure, APKCombo, etc.)

## Integration with Jota Editor

aGrep has special integration with Jota/Jota+ text editor (also by same developer):
- Search results can be opened directly in Jota
- Line number navigation supported
- Complementary workflow: search with aGrep → edit with Jota

This explains why many Japanese reviews mention using both apps together.

## Design Philosophy (inferred)

Based on the documentation and NYSL license:
- **Minimalist**: Simple, focused tool (just grep functionality)
- **Open**: Completely open source with permissive license
- **Integrative**: Designed to work with other apps via Android intents
- **Lightweight**: Small app size (~300-500KB)
- **Ad-free**: No advertisements
- **Privacy-focused**: No data collection mentioned

## Technical Details from Documentation

### Character Code Detection
- Automatic detection feature for Asian languages
- Handles multiple encodings without user intervention
- This is a key differentiator from other search apps

### Regular Expression Support
- Full regex support mentioned
- Uses Java regex engine (Pattern class)
- Can do lookaheads and other advanced patterns (confirmed in English reviews)

### Intent System
- **SEARCH-intent**: Can be launched from other apps
- **VIEW-intent**: Can launch other apps to view results
- This makes aGrep a "connector" in the Android app ecosystem

## Gaps in Official Documentation

The official documentation does NOT mention:
1. File size limits (but users report ~5MB limit)
2. Line reading limits (but users report ~10k line limit)
3. SD card access issues on modern Android (Scoped Storage)
4. Performance characteristics
5. Memory usage limits
6. Subdirectory recursion behavior (assumed default: recursive)
7. Maximum search result limits
8. Cancellation behavior during search
9. Error handling details
10. Temporary file handling

## Comparison: Documentation vs User Reality

| Feature | Documentation Says | Users Report |
|---------|-------------------|--------------|
| SD card access | "Search text files in external storage" | Broken on Android 12+, requires SAF |
| System directories | "Not for searching system directories" | Users want root/system access |
| File size | No mention | ~5MB limit causes issues |
| Encoding | "Auto-detects Asian languages" | Generally works well |
| Regex | "Supports Regular Expression" | Works but no help/examples |
| Performance | "Fast" (implied) | Slower than command-line grep |
| Intent integration | "Can be started by SEARCH-intent" | Works as documented |
| Text viewer | "Internal text viewer" | Basic but functional |

## Insights for Modernization

### What to Preserve
1. **Minimalist design** - Users appreciate simplicity
2. **Open source** - Community values this
3. **Ad-free** - Strong selling point
4. **Encoding detection** - Key feature for Asian users
5. **Intent integration** - Enables ecosystem workflows
6. **Jota integration** - Many users rely on this

### What to Update
1. **Storage access** - Migrate to Scoped Storage / SAF for Android 10+
2. **File size limits** - Remove or significantly increase
3. **Modern Android support** - Target SDK 34+
4. **Performance** - Optimize for modern multi-core devices
5. **Documentation** - Add in-app help, regex examples
6. **UI/UX** - Modernize while keeping simplicity

### New Features to Consider
1. **Search history** - Requested by Japanese users
2. **Find & replace** - Highly requested
3. **Export results** - Workflow enhancement
4. **Dark theme** - Modern UI expectation
5. **NAS support** - Power user feature
6. **ZIP searching** - Extend file type support

## Developer's Other Apps Context

Understanding that the developer also created **Jota/Jota+ Text Editor** provides context:

1. **Ecosystem approach**: aGrep + Jota form a complete text workflow
2. **Intent-based design**: Apps designed to work together via Android intents
3. **Asian language focus**: Both apps emphasize proper encoding handling
4. **Minimalist philosophy**: Both are lightweight, focused tools
5. **Open source commitment**: Both use permissive licenses

This suggests aGrep was never intended as a standalone "super app" but rather as a focused component in a larger ecosystem.

## Recommendations Based on Official Sources

### Priority 1: Honor Original Design Philosophy
- Keep the app simple and focused
- Maintain NYSL or similarly permissive license
- Preserve intent-based integration
- Keep it lightweight and ad-free

### Priority 2: Address Platform Evolution
- Update for modern Android storage (SAF)
- Fix compatibility with Android 12-14
- Address scoped storage restrictions
- Target latest SDK while maintaining backward compatibility where possible

### Priority 3: Remove Undocumented Limitations
- File size limits (5MB)
- Line reading limits (10k)
- Search cannot be cancelled
- Crashes on large searches

### Priority 4: Document Reality
- Add in-app help explaining:
  - How folder selection works (long-press)
  - Regular expression examples
  - File size/type limitations
  - SD card access requirements
  - Intent integration usage

### Priority 5: Community-Requested Features
Based on synthesis of official design + user needs:
- Search history (fits minimalist design)
- Result count (simple addition)
- Dark theme (modern standard)
- Better path display (UX improvement)
- Export results (extends utility without bloat)

## Blog & Historical Information

### Developer Blog (http://pandora.sblo.jp/)
- Blog name: "アドエス小物日記" (Ades Gadget Diary)
- Developer posts about aGrep, Jota/Jota+, and other Android apps
- August 2014 archive mentions icon design by Miyahi Non (@non_cozy)
- Blog contains development history and updates (HTTP only site)
- aGrep category: http://pandora.sblo.jp/category/911937-6.html

### Version Timeline (from available sources)
- **2010**: Copyright year, likely initial development
- **0.2.x series**: Early versions (0.2.0, 0.2.1, 0.2.2, etc.)
  - 0.2.5: Mentioned in several user reviews
  - 0.2.7: Multiple user reviews reference this version
  - 0.2.8: Pre-2014 version mentioned in reviews
- **2014.01**: Version mentioned in reviews
- **2014.02**: Version mentioned in reviews
- **2014.03**: Final published version (November 18, 2014)

### Development Activity
- Active development: ~2010-2014
- Last update: November 18, 2014
- No updates for 11 years (2014-2025)
- Source code available on GitHub: https://github.com/jiro-aqua/aGrep
- Developer Twitter: @jiro_aqua

## Questions for Further Research

1. Why was development stopped in 2014?
2. Is the original developer still active? (Twitter: @jiro_aqua)
3. Are there any official changelogs beyond what's in git history?
4. Was there ever a roadmap or TODO list?
5. Were there beta versions with additional features?
6. Is there a user manual beyond the basic website?
7. Were there any official blog posts explaining design decisions?
8. Can we access and parse the full blog archives at http://pandora.sblo.jp/?

## Conclusion

The official documentation reveals aGrep as a **carefully designed, minimalist tool** with a specific purpose: efficient text searching in external storage with proper Asian language support, integrated into the Android ecosystem via intents.

The gap between 2014 (last update) and 2025 (current modernization effort) represents a decade of Android platform evolution that has broken many of the original assumptions:
- Storage access model changed (Scoped Storage)
- Platform security tightened (runtime permissions)
- User expectations evolved (dark theme, modern UI)
- Device capabilities increased (multi-core, more RAM)

Modernization should **preserve the core philosophy** while adapting to platform changes and addressing limitations that were likely implementation constraints (file size limits) rather than design choices.
