# International Google Play Store Reviews - Additional Insights

Analysis of 82 non-English reviews (Japanese, Russian, German, Spanish, Chinese) from Google Play Store.

## Summary Statistics
- **Japanese**: 40 reviews (most substantial feedback)
- **Russian**: 12 reviews
- **German**: 10 reviews
- **Spanish**: 8 reviews
- **Chinese (CN + TW)**: 12 reviews

---

## NEW Issues Found (Not in English Reviews)

### 🔴 1. Cannot select SD card on Android 12
**Language**: Japanese
**Reviews**: #8 (ja)
**Quote**: "Android12ではSDCardを選択できない。使えないです。" (Cannot select SD card on Android 12. Unusable.)

**Analysis**: More specific than general "SD card access" - explicitly broken on Android 12+. This is CRITICAL.

### 🟢 2. Search history management
**Language**: Japanese
**Reviews**: #2 (ja), #16 (ja)
**Quotes**:
- "検索履歴を削除出来る機能が欲しいですね" (Want ability to delete search history)
- "履歴の消し方がわからない" (Don't know how to clear history)

**Feature Request**: Add ability to view and clear search history

### 🟢 3. NAS/Network storage support
**Language**: Japanese, German
**Reviews**: #5 (ja), #4 (de)
**Quotes**:
- "ＮＡＳ上のテキストファイル群を指定出来ない" (Cannot specify text files on NAS)
- "Suchmöglichkeit in Dateisystemen des Heimnetzwerks von Vorteil" (Would be useful to search home network filesystems)

**Feature Request**: Support for network/NAS file searching

### 🟢 4. Directory naming/labeling
**Language**: Japanese
**Review**: #11 (ja)
**Quote**: "パス表示となっているせいで階層の深い場所をいくつも登録すると見にくくなってしまう。任意でタイトルなどが付けられるようになると便利" (Deep paths become hard to read when multiple registered. Would be convenient to assign custom titles)

**Feature Request**: Allow custom labels/aliases for target directories instead of just showing full paths

### 🟡 5. File format support requests
**Languages**: Japanese
**Reviews**: #18 (ja), #20 (ja)
**Quotes**:
- "docx、doc、xlsx 等は、そもそも検索対象ではないのですね" (docx, doc, xlsx are not search targets)
- "PDF/.doc/.docxを検索したり、Zipの中を検索して貰えるとありがたい" (Would appreciate searching PDF/doc/docx and inside ZIP files)

**Feature Request**: Support for Office documents (Word, Excel) and searching inside ZIP/compressed files

### 🟢 6. Filename with '#' character causes errors
**Language**: Japanese
**Review**: #21 (ja)
**Quote**: "検索結果のファイル名に「#」が含まれているとテキストエディタなどのアプリで開こうとした際、エラーが出て開けません" (When search result filename contains '#', error occurs when trying to open with text editor)

**Bug**: Files with '#' in filename cannot be opened from search results (URI encoding issue)

### 🟢 7. Subdirectory search control
**Language**: Japanese
**Review**: #22 (ja)
**Quote**: "サブディレクトリを見ないモード指定とか、ファイル名の絞り込み用のキーワード指定とか" (Want mode to not search subdirectories, and filename keyword filtering)

**Feature Request**:
- Option to disable recursive subdirectory searching (search only specified folder, not children)
- Filename pattern filtering (separate from extension filtering)

### 🟢 8. Search result count display
**Language**: Japanese
**Review**: #23 (ja)
**Quote**: "検索結果に、hitした件数表示が欲しいです　〜○○件hitしました〜" (Want hit count display in search results "~XX matches found~")

**Feature Request**: Display total match count in results summary

### 🟡 9. Search progress cannot be cancelled
**Language**: Japanese
**Review**: #24 (ja)
**Quote**: "大量のファイルを検索する場合にそれを中断できず、その上まれに応答無しで落ちます" (When searching large number of files cannot cancel, and sometimes crashes with no response)

**Bug**: Search cannot be cancelled, and app sometimes becomes unresponsive/crashes during large searches

**Note**: This is related to issue #4 in codex_tasks_quality.md but more severe - crashes, not just UI issues

### 🟢 10. Performance mode - disable live results
**Language**: Japanese
**Review**: #25 (ja)
**Quote**: "検索中は表示をオフにするモードもあればと思っております。それは表示をしないことにより、少しでも検索スピードが上がるのではと思うから" (Would like mode to disable display during search. Turning off display might speed up search)

**Feature Request**: Option to disable live result updates during search for better performance

### 🟢 11. Fuzzy search / character normalization
**Languages**: Japanese
**Reviews**: #28 (ja), #34 (ja)
**Quotes**:
- "新字旧字も同一視してしまえるあいまい機能（できれば曖昧情報を自分で登録できる）" (Fuzzy matching for new/old kanji, with custom fuzzy rules)
- "全角カナ半角カナなどを同じと見なしたり、表現の相違を補完したりする、『あいまい検索』や『ゆらぎ検索』" (Treat full-width/half-width kana as same, fuzzy/variation search)

**Feature Request**: Japanese-specific fuzzy matching:
- Full-width ⟷ half-width katakana equivalence
- New kanji ⟷ old kanji equivalence
- Custom character equivalence rules

**Note**: This is Japanese-specific but similar features could apply to other languages (accented characters, etc.)

### 🔴 12. File size limit (5MB) - Additional confirmation
**Language**: Japanese
**Review**: #28 (ja)
**Quote**: "ファイルサイズが約５ＭＢより大きいと検索されないようです。制限がありますか？" (Files larger than about 5MB don't seem to be searched. Is there a limit?)

**Confirmation**: This corroborates English review #131 about file size limits. The limit is around 5MB, not 10k lines specifically.

### 🟢 13. Cross-line search
**Language**: Japanese
**Review**: #30 (ja)
**Quote**: "改行をまたいだ文字列も検索にかかるようにしていただけたら文句なしですね" (Would be perfect if could search strings across line breaks)

**Feature Request**: Multiline/cross-line search capability (search patterns that span multiple lines)

### 🟡 14. Case sensitivity issues with German characters
**Language**: Russian
**Review**: #2 (ru)
**Quote**: "ищеш ""ß тест"" а оно помимо искомого находит ""ss"" и ""sS""" (Searching for "ß test" also finds "ss" and "sS")

**Bug**: German ß (eszett) character is being matched against "ss" when it shouldn't be (or vice versa depending on normalization expectations)

### 🟢 15. Large file (10MB) search failure
**Language**: Russian
**Review**: #3 (ru)
**Quote**: "Даёт ноль результатов. Но я не ставлю одну звезду поскольку я пытаюсь найти пару символов в файле с размером 10 мегабайт" (Gives zero results. But I don't give one star because I'm trying to find a couple characters in a 10 megabyte file)

**Confirmation**: Another data point confirming file size limit issues

---

## Confirmed Issues (Also in English Reviews)

These issues appear in both English AND international reviews, indicating high user impact:

1. ✅ **Android 14 incompatibility** - Multiple Japanese reviews (RESOLVED)
2. **SD card access issues** - Japanese, English
3. **Find & Replace** - Russian reviews explicitly request this
4. **Filename search** - Chinese, German reviews want this
5. **App doesn't work / compatibility** - Multiple languages

---

## Cultural/Language-Specific Observations

### Japanese Users
- **Most detailed feedback**: Japanese users provide very specific, constructive suggestions
- **Text-heavy workflows**: Many use for recipe collections, expense tracking, research
- **Integration with Jota editor**: Multiple mentions of using aGrep with Jota text editor
- **Character encoding**: Japanese reviews mention UTF-8, Shift-JIS handling
- **Politeness**: Even critical reviews are very polite and appreciative

### Russian Users
- **Technical users**: Programmers and advanced users ("must have для продвинутых юзеров и программеров")
- **Find & replace highly requested**: Multiple reviews specifically ask for this
- **Appreciate the small size**: Explicit thanks for 0.5MB app size

### German Users
- **Network storage interest**: Want to search files on home network
- **Filename search requested**: Want to search by filename in addition to content
- **Purist appreciation**: Appreciate simple, clean, focused design

### Chinese Users
- **Brief reviews**: Most are short positive feedback
- **Filename search**: Explicitly requested ("希望增加同时搜索文件名的功能")

### Spanish Users
- **Very positive**: Mostly 5-star reviews with brief positive comments
- **Few specific requests**: Less detailed feedback compared to Japanese/German

---

## Priority Adjustments Based on International Feedback

### Newly Critical
- **SD card selection on Android 12+** (Japanese users heavily impacted)
- **Search cancellation + crash issues** (affects large file workflows common in Japan)

### Elevated Priority
- **Search history management** (requested by Japanese users)
- **File size limit fix** (confirmed by multiple languages: Japanese, Russian, English)
- **Cross-line/multiline search** (Japanese academic/research use case)
- **Filename '#' character bug** (Japanese users encounter this)

### New Features to Consider
- **NAS/Network storage support** (Japanese + German users)
- **Directory labeling/aliases** (Japanese UX improvement)
- **Fuzzy search for Japanese** (could be extended to other languages)
- **Subdirectory control** (requested by Japanese users)
- **Result count display** (Japanese UX improvement)
- **Performance mode** (disable live updates) (Japanese suggestion)
- **Office document support** (PDF, DOCX, XLSX) (Japanese users)
- **ZIP file searching** (Japanese users)

### Confirmed Low-Priority
- **Voice interface** - Not mentioned in any non-English reviews
- **Widget** - Not mentioned in international reviews

---

## Recommended Action Items

### Immediate (Critical Bugs - Week 1)
1. Fix SD card selection on Android 12+ (Japanese users blocked)
2. Fix search cancellation + crash on large searches
3. Fix file size limit (5MB→10MB issue)
4. Fix '#' character in filename bug

### High Priority (Weeks 2-4)
5. Implement search history view/clear functionality
6. Add result count display
7. Cross-line search support (multiline mode)
8. Subdirectory recursion toggle

### Medium Priority (Weeks 5-8)
9. Directory custom labels/aliases
10. Office document format support (at least PDF)
11. ZIP archive searching
12. Performance mode (disable live updates)

### Research/Future
13. NAS/Network storage support (significant architectural change)
14. Japanese fuzzy search (kanji normalization, kana width)
15. Advanced filename filtering

---

## Testing Priorities for International Markets

Based on review volume and specificity:

1. **Japanese market** (40 reviews, very detailed):
   - SD card access on modern Android
   - Large file handling
   - Shift-JIS and other encodings
   - Integration with Jota editor

2. **Russian market** (12 reviews, technical users):
   - Find & replace functionality
   - Large file performance
   - UTF-8/Cyrillic encoding

3. **German market** (10 reviews):
   - Network storage scenarios
   - Filename search
   - German character handling (ß, umlauts)

4. **Chinese market** (12 reviews):
   - Filename search
   - GB2312/UTF-8 encoding
   - Simplified vs Traditional character handling

5. **Spanish market** (8 reviews):
   - General functionality (fewer specific issues reported)
