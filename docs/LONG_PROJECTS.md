# Long Projects & Paid Version Features

Tasks that require significant effort (10+ hours) or are out of scope for the core free version. These could be candidates for:
- **Paid/Pro version** features
- **Major version releases** (e.g., 2.0)
- **Community contribution** projects
- **Research & exploration**

---

## 💰 Paid Version Candidates

Features that provide significant value but are out of scope for core grep functionality.

### 1. Find & Replace

**Impact**: ⭐⭐⭐⭐⭐ (Highest - heavily requested)
**Effort**: 🔨🔨🔨🔨🔨 (20-40 hours)
**Status**: **OUT OF SCOPE** for core free version (grep is read-only)
**Paid Version**: **HIGH PRIORITY** feature differentiator

**Why Out of Core Scope**:
- Grep is fundamentally a search/read-only tool
- Write access adds complexity and risk
- File modification requires careful safety features

**Why Good for Paid Version**:
- Heavily requested by users (multiple reviews in Russian, English)
- Clear value proposition: "aGrep Pro - Search AND Replace"
- Justifies premium pricing (powerful feature)
- Power user feature (fits technical audience)

**Requirements**:

**Phase 1: Preview Mode**
- Add "Replace" tab in main UI
- "Replace with" text field
- Preview changes before applying (show before/after)
- Display count of matches that will be replaced
- Support regex capture groups (`$1`, `$2`, etc.)

**Phase 2: Safety Features**
- Dry run mode (show what would change without changing)
- Automatic backup before replace (or optional backup)
- Undo mechanism (revert replacements)
- Confirmation dialog with summary

**Phase 3: Replace Execution**
- Batch replace across multiple files
- "Replace All" vs. "Replace in selected files"
- Progress indicator for large replace operations
- Transaction-like behavior (all-or-nothing option)

**Phase 4: Advanced Features**
- File filter options (exclude certain files from replacement)
- Replace only in selected line ranges
- Case-preserving replace (replace "hello" → "HELLO" becomes "world" → "WORLD")
- Audit log of all replacements

**Technical Challenges**:
- File encoding handling (read in one encoding, write in same)
- Large file handling (can't load entire file into memory)
- Atomic file updates (don't corrupt on failure)
- Permission handling (write access to selected directories)
- Backup storage management

**Files to Create**:
- `app/src/main/java/jp/sblo/pandora/aGrep/ReplaceEngine.java`
- `app/src/main/java/jp/sblo/pandora/aGrep/ReplaceViewModel.java`
- UI layouts for replace mode
- Backup manager for undo functionality

**User Stories**:
- "As a developer, I want to rename variables across my codebase"
- "As a writer, I want to fix consistent typos across all my documents"
- "As a translator, I want to update terminology across translation files"

**Monetization**:
- One-time purchase: $2.99-$4.99
- Or: Free version limited to N replacements per session
- Or: In-app purchase to unlock replace feature

---

### 2. Network/NAS Storage Support

**Impact**: ⭐⭐⭐⭐ (High - requested by Japanese/German users)
**Effort**: 🔨🔨🔨🔨🔨 (40+ hours)
**Status**: **OUT OF SCOPE** if SAF doesn't support it
**Paid Version**: **MEDIUM PRIORITY** differentiator

**Investigation Result Pending**:
- Test if SAF file picker already supports network storage
- If YES: Document and include in free version (no extra work)
- If NO: Move to paid version as premium feature

**Why Out of Core Scope**:
- Requires SMB/CIFS client implementation (or cloud storage APIs)
- Network latency makes search slow
- Authentication/credential management complexity
- Not part of basic local file grep functionality

**Why Good for Paid Version**:
- Clear professional/power user feature
- Requested by advanced users (NAS = tech-savvy)
- Significant implementation effort justifies pricing
- Differentiates from other grep apps

**Requirements**:

**Phase 1: SMB/CIFS Support**
- Integrate jCIFS-NG library for SMB client
- Network location configuration UI
- Credential storage (encrypted)
- Browse network shares

**Phase 2: Cloud Storage Integration**
- Google Drive API integration
- Dropbox API integration
- OneDrive API integration
- OAuth authentication flow

**Phase 3: Performance Optimization**
- File listing cache (network is slow)
- Background sync for frequently accessed locations
- Offline mode (cached search in downloaded files)
- Smart prefetching

**Phase 4: Advanced Features**
- VPN integration hints
- SSH/SFTP support (for Linux servers)
- WebDAV support
- Saved network locations

**Technical Challenges**:
- Network latency (search will be slow)
- Authentication across different protocols
- Credential security (encrypted storage)
- Offline/online state handling
- Battery drain from network operations

**Files to Create**:
- `app/src/main/java/jp/sblo/pandora/aGrep/NetworkStorageManager.java`
- Cloud storage API integrations
- Network credential management
- UI for network location configuration

**User Stories**:
- "As a sysadmin, I want to search log files on my NAS"
- "As a remote worker, I want to search files in company cloud storage"
- "As a developer, I want to grep source code on remote servers"

**Monetization**:
- Subscription: $0.99/month or $9.99/year (ongoing cloud API costs)
- Or: One-time purchase: $4.99-$7.99 (SMB/local network only)
- Or: Free tier with limited network locations

---

## 🔬 Research & Long-Term Projects

Features that are technically interesting and valuable but require significant R&D.

### 3. Fuzzy Search / Character Normalization

**Impact**: ⭐⭐⭐ (Medium-High if done well)
**Effort**: 🔨🔨🔨🔨 (10-20 hours)
**Status**: **RESEARCH** - Interesting technical challenge
**Paid Version**: Possible differentiator

**What**: Generalized fuzzy matching system (NOT language-specific)

**Original Request**:
- Japanese users requested: full-width ⟷ half-width kana, new kanji ⟷ old kanji
- But this should be generalized, not Japanese-only

**Generalized Approach**:

1. **Character Equivalence Rules**
   - Define custom equivalence classes: `a ≈ á ≈ à ≈ â ≈ ä` (accents)
   - Unicode normalization: NFD, NFC, NFKD, NFKC
   - Case folding beyond simple toLowerCase()
   - Width normalization: full-width ⟷ half-width

2. **Edit Distance Fuzzy Matching**
   - Levenshtein distance: allow N character differences
   - Damerau-Levenshtein: allow transpositions
   - Configure tolerance: exact, 1 char off, 2 chars off

3. **Configurable Fuzzy Rules**
   - User-defined equivalence files: `config/fuzzy_rules.txt`
   - Format: `group: a,á,à,â,ä,A,Á,À,Â,Ä`
   - Language-specific rules as optional plugins

**Use Cases**:
- **European languages**: Accented characters (café ≈ cafe)
- **Japanese**: Kana width, kanji variants
- **Typo tolerance**: "teh" finds "the"
- **OCR errors**: Common character misrecognition

**Technical Challenges**:
- Performance: Fuzzy matching is expensive
- False positives: Too fuzzy = irrelevant results
- UI/UX: How to communicate fuzzy matches to user?
- Configuration: Balance power vs. complexity

**Research Questions**:
1. Can we use finite automata for efficient fuzzy matching?
2. How to visualize/highlight fuzzy matches in results?
3. Should fuzzy rules be per-search or global preference?
4. How to handle fuzzy matching with regex?

**Potential Libraries/Algorithms**:
- Apache Commons Text: Levenshtein distance
- ICU4J: Unicode normalization, transliteration
- Lucene: Fuzzy query algorithms
- Custom: Build equivalence-class NFA

**Why Interesting**:
- Cross-linguistic text processing
- Algorithm design (efficient fuzzy matching)
- Balancing power vs. usability
- Could be published as separate library

**Monetization Potential**:
- Free: Basic fuzzy (typo tolerance)
- Paid: Advanced fuzzy rules, language-specific plugins

---

### 4. Performance Profiling & Optimization Deep Dive

**Impact**: ⭐⭐⭐⭐⭐ (Highest - affects all searches)
**Effort**: 🔨🔨🔨🔨 (10-15 hours for comprehensive analysis)
**Status**: **IMPORTANT** - Should be done for free version quality

**Implementation Plan**: See `docs/SAF_SEARCH_PERFORMANCE_PLAN.md` for detailed methodology

**What**: Systematic performance analysis and optimization

**Phase 1: Measurement Infrastructure** (Already in Top 5 Impact)
- Build reproducible benchmark suite
- Measure baseline performance
- Identify bottlenecks with profiling

**Phase 2: Deep Dive Analysis** (Research)
- **SAF IPC Overhead Analysis**
  - Compare DocumentFile vs. direct File API performance
  - Measure per-operation costs (listFiles, openInputStream, metadata)
  - Identify if batching/caching can reduce IPC calls

- **Encoding Detection Profiling**
  - Measure UniversalDetector performance on various file types
  - UTF-8 dominance in modern files - can we skip detection more often?
  - Compare: detect-always vs. extension-based skip vs. BOM-only detection

- **Pattern Matching Optimization**
  - Regex compilation overhead
  - Boyer-Moore string search for literal patterns
  - Compare Java Pattern vs. native code (JNI grep port?)

- **Memory vs. Speed Tradeoffs**
  - In-memory result caching
  - Streaming vs. buffered file reading
  - Parallel file processing (thread pool sizing)

**Research Questions**:
1. Is SAF IPC overhead the #1 bottleneck? (Hypothesis: yes)
2. Can we predict encoding from file metadata to skip detection?
3. Would native C/C++ grep port be faster than Java Pattern?
4. What's the optimal thread pool size for parallel file processing?
5. Does caching directory listings help repeated searches?

**Potential Optimizations** (After Measurement):
- Directory listing cache with LRU eviction
- Parallel file processing (multi-threaded search)
- Native regex engine via JNI
- Smarter encoding detection (BOM-first, then detect)
- Batch SAF queries using DocumentsContract

**Why Important**:
- SAF migration already caused performance regression
- Need data-driven optimization, not guesswork
- Could contribute findings to Android community

**Free vs. Paid**:
- Optimization benefits should be in free version (quality baseline)
- Paid version could have "Performance Mode" with aggressive optimizations

---

### 5. Advanced Search Modes

**Impact**: ⭐⭐⭐ (Medium - power user features)
**Effort**: 🔨🔨🔨 to 🔨🔨🔨🔨 (6-15 hours depending on mode)
**Status**: **FUTURE** - Power user features
**Paid Version**: Could bundle as "Advanced Search Pack"

**Potential Modes**:

1. **Multiline / Cross-Line Search**
   - Requested by Japanese users
   - Search patterns that span multiple lines
   - Regex with DOTALL flag: `.` matches newlines
   - UI: Checkbox "Multiline mode"
   - Example: Find XML tags spanning lines

2. **Context Lines (Before/After)**
   - Like `grep -A 2 -B 2`
   - Show N lines before and after match
   - UI: Spinner "Show context: [0] [1] [2] [5] lines"
   - Improves match understanding

3. **Inverse Matching**
   - Like `grep -v`
   - Find files that DON'T contain pattern
   - UI: Checkbox "Invert match (exclude)"
   - Use case: Find files without certain text

4. **Line Number Range Filtering**
   - Search only lines 10-100
   - UI: Optional "Line range: [start] to [end]"
   - Use case: Search specific sections of large files

5. **Binary File Handling**
   - Currently may try to search binary files
   - Add: "Skip binary files" option
   - Detect binary via null bytes or magic numbers

**Monetization**:
- Free: Basic search modes
- Paid: Advanced modes (multiline, context, inverse)

---

### 6. Search Result Post-Processing & Filtering

**Impact**: ⭐⭐⭐ (Medium - workflow enhancement)
**Effort**: 🔨🔨🔨🔨 (8-12 hours)
**Status**: **FUTURE** - UX enhancement
**Paid Version**: Could be "Pro Workflow Features"

**Features**:

1. **Filter Results by Filename Pattern**
   - After search: "Show only results from *.java files"
   - Client-side filtering (fast, no re-search)

2. **Sort Results by Different Criteria**
   - Currently: sorted by filename
   - Add: sort by match count, file size, date modified
   - UI: Dropdown in results screen

3. **Group/Collapse Results**
   - Collapsible file sections (show/hide matches per file)
   - Group by directory
   - Summary view: files with match counts only

4. **Secondary Search within Results**
   - Search-within-search: filter results by another pattern
   - Example: Search for "error", then filter to "database" errors
   - UI: Secondary search field in results screen

5. **Result Highlighting & Annotation**
   - Different colors for different match groups
   - User annotations on results
   - Star/flag important results

**Monetization**:
- Free: Basic results display
- Paid: Advanced filtering, annotations, saved result sets

---

## 🧪 Experimental / Low Priority

### 7. Machine Learning for Encoding Detection

**Impact**: ⭐⭐ (Low-Medium - encoding detection already works)
**Effort**: 🔨🔨🔨🔨🔨 (20+ hours)
**Status**: **EXPERIMENTAL** - Academic interest only

**What**: Use ML to predict file encoding faster/better than UniversalDetector

**Why Experimental**:
- UniversalDetector already works well
- ML adds complexity and model size
- UTF-8 dominates modern files - benefit minimal
- Academic interest > practical value

**Research Value**:
- Explore lightweight on-device ML
- Could publish as academic case study

---

### 8. Distributed/Cloud Search

**Impact**: ⭐⭐ (Low - niche use case)
**Effort**: 🔨🔨🔨🔨🔨 (30+ hours)
**Status**: **OUT OF SCOPE** - Too complex

**What**: Search files on remote servers or cloud storage

**Why Out of Scope**:
- Network latency kills performance
- Security/authentication complexity
- Better served by server-side solutions (SSH + grep)
- Overlaps with Network Storage (#2 above)

**If Pursued**: Would be part of Network Storage feature (#2)

---

## 📊 Prioritization Matrix

### Free Version Must-Haves
- Performance optimization (quality baseline)
- Core stability and bug fixes
- Basic feature completeness

### Paid Version Tier 1 (High Value)
1. **Find & Replace** - Most requested, clear value
2. **Network/NAS Storage** - Professional users (if SAF doesn't provide)

### Paid Version Tier 2 (Nice to Have)
3. **Fuzzy Search** - Power user feature
4. **Advanced Search Modes** - Grep power users
5. **Result Post-Processing** - Workflow enhancement

### Research / Future
6. **Performance Deep Dive** - Do for free version
7. **ML Encoding** - Academic only
8. **Distributed Search** - Out of scope

---

## 💡 Paid Version Strategy

### Feature Bundling Options

**Option A: Single Paid Upgrade**
- Price: $4.99 one-time
- Includes: Find & Replace, Network Storage, Fuzzy Search, Advanced Modes
- Name: "aGrep Pro"

**Option B: Feature Packs**
- Replace Pack: $2.99 (Find & Replace only)
- Network Pack: $2.99 (NAS/Cloud storage)
- Power User Pack: $2.99 (Fuzzy + Advanced modes)
- Or bundle all: $6.99

**Option C: Subscription**
- Price: $0.99/month or $9.99/year
- Includes: All features + ongoing updates
- Justification: Cloud API costs for network storage

### Recommended Strategy
**Hybrid Approach**:
- Free version: Excellent core grep functionality (maintain current quality)
- One-time purchase: $3.99-4.99 for "aGrep Pro"
  - Includes: Find & Replace, Advanced Search Modes, Fuzzy Search
- Network Storage: Optional $2.99 add-on OR included in Pro
- No subscription (matches minimalist philosophy)

---

## 🎯 Implementation Timeline

### Phase 1: Stabilize Free Version (Now - 2 months)
- Fix critical bugs (file size limit, cancellation)
- Add test coverage
- Performance benchmarking and optimization
- Complete "Easy Wins" list

### Phase 2: Research & Validation (2-4 months)
- Deep dive performance analysis
- Prototype fuzzy search
- Test network storage feasibility
- User research: which paid features most valuable?

### Phase 3: Paid Version Development (4-6 months)
- Implement Find & Replace (most requested)
- Implement network storage (if feasible)
- Beta test with existing users
- Set up payment infrastructure

### Phase 4: Launch & Iterate (6+ months)
- Launch paid version
- Gather user feedback
- Add additional paid features based on demand
- Maintain free version quality

---

## 📋 Success Metrics

### Free Version
- 5-star rating improvement
- Reduced crash rate
- Performance improvement (faster searches)
- Active user retention

### Paid Version
- Conversion rate: X% of free users upgrade
- User reviews mention paid features positively
- Support burden reasonable (features work well)
- Revenue sustains development

---

## 🤝 Community Contribution

Long projects are good candidates for:
- **External contributors**: Well-scoped, significant impact
- **Bounties**: Offer payment for complex features
- **Academic projects**: Students seeking real-world problems
- **Sponsored development**: Companies using aGrep could fund features

Consider:
- GitHub issues labeled `paid-version-candidate` or `long-project`
- Detailed specification documents for complex features
- Mentorship for contributors tackling long projects
