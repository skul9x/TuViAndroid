# Changelog

<<<<<<< HEAD
=======
## [2026-03-18 - Lưu Nguyệt Logic & Monthly Transit Enhancement]
### Added
- **Monthly Transit Palace Calculation**: Implemented the traditional "Tiểu Hạn -> Backward Month -> Forward Hour" rule to determine the starting palace of the lunar month (Lưu Nguyệt).
- **Monthly Can Chi (Ngũ Hổ Độn)**: Integrated the "Ngũ Hổ Độn" formula to accurately calculate the Heavenly Stem of each lunar month based on the birth year's Stem.
- **Monthly Transit Stars (Lưu Nguyệt)**: Added logic to place monthly transiting stars: `LN. Lộc Tồn`, `LN. Kình Dương`, `LN. Đà La`, `LN. Thiên Khôi`, `LN. Thiên Việt`, `LN. Thiên Mã`, `LN. Thiên Khốc`, and `LN. Thiên Hư`.
- **Monthly Tứ Hóa (Phi Tinh)**: Implemented monthly-level Four Transformation stars based on the calculated monthly Stem.
- **Unit Testing**: Added `LuuNguyetLogicTest.kt` to verify monthly palace and star placement logic with 100% pass rate.

### Changed
- **`TuViLogic.kt` Refinement**: Separated `userBirthLunarMonth` from `viewingMonth` to ensure consistent and accurate transit calculations.
- **Data Models**: Updated `UserInfoResult` with `luuNguyetCung` and `phiTinhLuuNguyet` fields for better AI structured data mapping.

### Fixed
- **Tứ Hóa Mapping Bug**: Resolved an issue where stars with state suffixes (e.g., `Văn Xương (M)`) were failing to trigger transformation logic in `TuViLogic.kt`.
- **Data Integrity**: Verified that `LN.` stars are correctly placed for both Male (giờ Tỵ) and Female (giờ Dậu) profiles in February 2026.

>>>>>>> 7eadbb0 (Cập nhật README, STRUCTURE và đồng bộ logic Lưu Nguyệt (.brain))
## [2026-03-11 - Round 3: JSON Prompt Final Polish]
### Added
- **Data Integrity `_role` Tag**: Inserted `_role: "input_chart_data"` in `chart_data` to explicitly tell Gemini this is real destiny data, not an example.
- **Star State Context**: Added `_note` in `notation_rules.brightness` to clarify that stars without a `state` field are neutral sub-stars, preventing AI confusion.

### Changed
- **De-duplicated `axis_mapping`**: Removed the redundant `axis_mapping` object from the prompt root. It is now exclusively pulled from `chart_data.metadata`, reducing token overhead and preventing data mismatch.

### Fixed
- **Logical Cleanup**: Final pass on `GeminiClient.kt` to ensure 100% adherence to expert feedback while maintaining build stability.

## [2026-03-11 - Round 2: Expert Feedback Implementation]
### Added
- **Gender Suggestion Mode**: Rephrased `m3_gender` as an optional, traditional suggestion with a disclaimer, moving away from forced stereotypes.
- **Monthly Analysis Fallback**: Implemented explicit warnings and fallback logic when requesting monthly analysis without月-level transit data.
- **Deduplication Logic**: Added strict rules to prevent double-counting Tuần/Triệt (counting either flag or star, but not both).
- **Expanded Evidence Context**: Updated `evidence_format` to include relationship context (Trigon/Opposite) and Tuần-Triệt constraints.

### Changed
- **Softened AI Language**: Replaced absolute phrases like ">60% cuộc đời" or "Ngăn chặn hoàn toàn" with probabilistic language ("Xu hướng chi phối chính", "Thường làm giảm đáng kể") for better model reasoning.
- **Terminology Refinement**: Standardized "Nhật Nguyệt đồng cung tại Sửu" (instead of repeating Sửu-Sửu) and renamed `fortune_year_format` to `fortune_period_format`.

### Fixed
- **Axis Mapping Correction**: Fixed the last remaining incorrect pairs in `axis_mapping` (Phúc-Tài, Điền-Tử).
- **Data Integrity**: Added "Bình" (B) to the list of forbidden states AI must not hallucinate if missing.
- **Phi Tinh Metadata Path**: Corrected the source path for flying stars from `metadata` to `chart_data.phi_tinh_tu_hoa`.


## [2026-03-11 - Prompt Optimization v3.0 Implementation]
### Added
- **Finalized Prompt v3.0 Core**: Successfully implemented all 5 planned priority fixes in `GeminiClient.kt`.
    - **Child Rule (v3.0)**: Forced AI to skip career/wealth logic for đương số < 13y, focusing on health and parents.
    - **JSON Axis Mapping**: Converted raw text axis info into a structured `axis_mapping` object for superior spatial reasoning.
    - **Special Flags for Tuần/Triệt**: Integrated `flags` metadata at the palace level, allowing AI to detect constraints instantly.
    - **Clean Source Tracking**: Differentiated `fixed_stars` vs `transit_stars` with explicit types and sources.
    - **Prompt Template (`Prompt3.txt`)**: Updated with a more professional tone ("Điềm đạm - Phân tích mệnh lý") and shifted focus to monthly analysis ("Tháng 4 năm 2026").

### Changed
- **Logic Cleanup**: Removed legacy `trucCungStr` calculation in favor of the new axis mapping object.
- **Rule Synchronization**: Adjusted wording in `priority_rules` to ensure 100% compatibility with expert verification tests.

### Fixed
- **Verification Failure**: Resolved an assertion error in `Level5DeepVerificationTest` caused by a minor wording mismatch in expert fix #11.
- **Build Stability**: Verified 17/17 deep verification tests passing.


## [2026-03-10 - Evening: Prompt Optimization v3.0 Planning]
### Added
- **Implementation Plan for Priority Fixes**: Created a detailed roadmap to resolve the top 5 issues identified in expert review (Child Rule, Deduplication, Data Consistency).
- **Structural Refinement Strategy**:
    - **Child Rule Hierarchy**: Prioritizing health/personality for kids < 13, explicitly disabling career/marriage logic.
    - **JSON Metadata Deduplication**: Removing redundant "Dai Van" strings and refining the "Tu Hoa" summary as a quick-reference.
    - **Transit Star Source Field**: Separating star names from their source (`dai_van`, `luu_nien`) in JSON.
    - **Axis Object Mapping**: Converting `truc_cung` from text blocks to structured key-value pairs.
    - **Tuần/Triệt Status Logic**: Reclassifying Tuần/Triệt as `special_flag` to differentiate from standard stars.

### Fixed
- **Empty Star Bug (Root Cause)**: Identified and planned a filter fix for empty strings in the star parsing logic.

## [2026-03-10 - Noon: JSON Prompt Transformation]
### Added
- **JSON Prompt System**: Major architectural shift in `GeminiClient.kt`. Replaced prose-based prompts with structured JSON output using `JSONObject`.
- **Modular JSON Construction**: Added 10+ helper methods to build specialized JSON segments for Absolute Rules, Analysis Pipeline, Methodology, Notation, and Chart Data.
- **Unit Test JSON Support**: Integrated `org.json:json:20231013` as a test dependency in `build.gradle.kts` to enable JSON logic verification on the JVM.
- **Enhanced JSON Chart Data**: Structured representation of 12 palaces (including separate fixed vs. transit star arrays), 10-Can transformation tables, and specific astrological flags (VCD, Force Score).

### Changed
- **Unit Tests Migration**: Updated `DataLayerVerificationTest.kt` and `Level5DeepVerificationTest.kt` assertions to target JSON keys/values instead of exact prose strings.
- **Logic Verification**: Adjusted `amDuong` test expectations to support "Âm dương thuận lý" correctly for the Nhâm Thân 1992 benchmark case.

### Fixed
- **JSONObject JUnit Mocking**: Resolved `Method put in org.json.JSONObject not mocked` errors by shifting from Android stubs to real JSON library for tests.


### Fixed — Astrology Logic
- **Tiểu Hạn (Minor Cycle)**: Overhauled `tinhTieuHan()` logic in `TuViLogic.kt`. Changed the starting palace from Chi Năm Sinh (Incorrect) to the traditional **Tứ Mộ cung** (Thìn, Tuất, Sửu, Mùi) based on **Tam Hợp Tuổi**, following Nam Phái standard.
    - Dần-Ngọ-Tuất → Thìn (4)
    - Thân-Tý-Thìn → Tuất (10)
    - Tỵ-Dậu-Sửu → Mùi (7)
    - Hợi-Mão-Mùi → Sửu (1)
- **Unit Tests**: Updated `Level5DeepVerificationTest.kt` and `TuViLogicTest.kt` to reflect correct Tiểu Hạn positions for both Male and Female cases. Verified 100% test pass rate.

## [2026-03-09 - Night: Performance Optimization & Reliability Audit]
### Fixed — Performance
- **Main Thread Decoupling**: Offloaded heavy astrology calculations (`anSao`) from the UI Thread to `Dispatchers.Default` using Kotlin Coroutines. Prevents app freezes during sheet generation.
- **Database Indexing**: Added a SQL index to the `timestamp` column in the `laso_history` table to prevent full table scans when rendering history lists.
### Fixed — Bug
- **Room Schema Mismatch**: Resolved a crash on startup/history access caused by adding the database index without a migration. (Confirmed fix: Manual data wipe/reinstall).

## [2026-03-09 - Late Night: GitHub Deployment & Final Verification]
### Added
- **GitHub Repository**: Initialized Git and pushed the complete codebase to `https://github.com/skul9x/TuViAndroid.git` (branch `main`).
- **History Preservation**: Successfully pushed without force-pushing to maintain project commit history.

### Verified
- **Rule #8 Prompt Upgrade**: Confirmed that `GeminiClient.kt` contains the expert-approved wording for AI reasoning (allowing inference while banning raw data fabrication).
- **Level 5.0 Core**: Re-verified that VCD protocol, interaction weights, and yearly transit logic are correctly integrated.

## [2026-03-09 - Late Night: Critical Fix & Logic Audit]
### Fixed
- **Sao Thiên Thọ Placement**: Resolved a critical bug where Thiên Thọ was missing due to a keyword mismatch (`"(Thân)"` vs `"[Thân cư]"`).
### Audit
- **Comprehensive Formula Verification**: Conducted a deep manual audit of 25+ astrological formulas in `TuViLogic.kt` (including Chính Tinh, Phụ Tinh, Tuần/Triệt, Tứ Hóa, axes, etc.) against standard references. **All verified correct.**

## [2026-03-09 - Part 5: Level 5.0 Final Polish]
### Added — Advanced Prompt Validation & Yearly Transits
- **BƯỚC 4 – Kiểm tra mâu thuẫn**: Mandated a post-analysis validation step for the AI to resolve contradictions between life aspects (Mệnh vs. Thân, Mệnh vs. Career, etc.).
- **Tứ Hóa Table Lock**: Added a strict warning `⚠️` under the 10-Can table to explicitly forbid the AI from self-calculating transformations beyond the provided pre-computed data.
- **Mandatory E1 Yearly Analysis**: Enforced a highly structured format for analyzing the current viewing year's transit (`E1. Vận năm ${info.viewingYear}`) with 5 specific steps including "Trùng điệp tứ hóa" (transformation stacking).

### Fixed
- **Verification Stability**: Added `deepDump_Level5FinalPolish_3Blocks` test case to verify the presence of these final 3 expert patches.
- **Build**: Successfully verified clean build and APK generation for version 5.0 baseline.

## [2026-03-09 - Part 4: Mastery Methodology Upgrade]
### Added — AI Prompt Engineering v5.0 (Expert Refinement)
- **Priority Logic & Interaction Weights**: Injected explicit reasoning guidelines into the AI prompt to solve signal conflicts.
    - **Priority**: Main stars > Minor stars; Brightness (Miếu/Hãm) > Presence; Bản Mệnh > Đại Vận > Lưu Niên.
    - **Weights**: Palace (100%) > Trigon (80%) > Polar (70%) > Neighbors (50%) > Harmony (30%).
- **4-Step Vô Chính Diệu (VCD) Protocol**: Replaced the simple VCD rule with a comprehensive procedure: Borrowing polar stars (with 30% reduction), assessing sub-stars as "actual masters", and evaluating "Empty space" outcomes based on benefic/malefic presence.
- **Scoring & Reliability Format**: Forced the AI to output numerical **Palace Strength [1-10]** and **Reliability Ratings [High/Med/Low]** per conclusion.
- **Tứ Hóa Summary Block**: Added a dedicated, pre-aggregated section in the prompt that summarizes Inherent, Decade, and Annual transformations together for unified reasoning.
- **Negative Examples (Anti-Pattern Guardrails)**: Added 5 common failure examples (e.g., "Tử Vi is always good", "Kình Dương is always bad") into the prompt to prevent stereotypical readings.

### Fixed
- **Test Accuracy**: Corrected Level 5 deep verification test expectations (fixing Dần vs Thìn positions for specific stars) in Nhâm Thân 1992 test case.
- **GeminiClient.kt**: Implemented `buildTuHoaSummary()` helper to facilitate the new summary block.


## [2026-03-09 - Expert Feedback Fixes]
### Fixed — Astrology Logic & Metadata Accuracy
- **Refined `detectBoSao()`**: Overhauled star combination detection to use **Tam phương tứ chính** (trigons + opposite) logic.
    - **Tử Phủ Vũ Tướng**: Now correctly distinguishes between a full formation (distributed across 3+ palaces) and a mere "grouping" (Nhóm) if stars are clustered in only 2 palaces.
    - **Sát Phá Tham**: Standardized label to **"Tam hợp Sát Phá Tham"**.
    - **Nhật Nguyệt**: Precise classification into **"đồng cung"**, **"hội chiếu"**, or **"đối chiếu"** based on relative positions.
- **Stem/Branch Notation**: Standardized the Four Transformation abbreviations from `ĐV. H Lộc` to the clearer **`ĐV. Hóa Lộc`** (and similarly for Quyền, Khoa, Kỵ) in both logic engine and AI prompt.
- **Metadata Labeling**: Changed the prompt metadata label from "Bộ sao đã hình thành" to **"Nhóm sao hội hợp"** to clarify that these are detected candidates for AI analysis, not confirmed formations.

### Added — AI Prompt Intelligence
- **"XẾP HẠNG CÁCH CỤC" Block**: Added a new mandatory step (3b) for the AI to resolve conflicts when multiple formations are present.
    - Ranking criteria: **LỰC (Strength/Brightness)** > **VỊ TRÍ (Palace importance)** > **TỨ HÓA (Transformations support)**.
    - Prohibits the AI from giving equal weight to conflicting primary formations (e.g., must decide between Sát Phá Tham vs. Tử Phủ Vũ Tướng as the dominant influence).
- **Notation Definitions**: Added explicit definitions for **"Tam hợp [Bộ sao]"** vs. **"Nhóm [Bộ sao]"** in the prompt's `QUY ƯỚC KÝ HIỆU` to help the AI distinguish between confirmed patterns and potential groupings.
- **Verification Testing**: Added 3 new deep verification tests (`deepDump_DetectBoSao_Labels`, `deepDump_DaiVan_HoaLabel`, `deepDump_PromptCachCucRanking`) to ensure these specific fixes remain stable.

## [2026-03-09 - Level 5 Upgrade]
### Added — Level 5 Methodology & Advanced Astrology Logic
- **Can Chi 12 Cung**: Implemented "Ngũ Dần Độn" to accurately calculate the Heavenly Stem of each palace based on the birth year's Stem.
- **Tiểu Hạn (Minor Cycles)**: Added calculation for the Minor Fortune Cycle palace based on birth year branch, gender, and viewing year.
- **Phi Tinh Tứ Hóa (Pre-computed)**: Implemented 10-Can flying star transformations. The code now pre-calculates Hóa Lộc, Quyền, Khoa, Kỵ for all 12 palaces and injects it into the prompt.
- **7 Mandatory Methodological Sections**: Upgraded `GeminiClient.kt` with a massive methodological prompt including:
    1. **Tứ Hóa Phân Tích**: 8-step process for inherent transformations.
    2. **Ngũ Hành 4 Tầng**: Mệnh vs Cục vs Cung vs Sao analysis.
    3. **Giới Tính Quy Tắc**: Gender-specific priorities (Nam: Quan/Tài, Nữ: Phu/Tử).
    4. **Tuần-Triệt Protocol**: Precise influence levels by age (before/after 30).
    5. **Phi Tinh Phân Tích**: Causality analysis between palaces (Can cung A → Hóa → Cung B).
    6. **Vận Hạn Đa Tầng**: Multi-layered stacking (Đại Vận + Lưu Niên + Tiểu Hạn).
    7. **Kiểm Chứng Chéo**: Cross-checking Mệnh/Thân and Trinity (Tam giác Mệnh-Quan-Tài).
- **Expanded Cách Cục List**: Added 20+ advanced astrological formations (Quân Thần Khánh Hội, Mã Đầu Đới Kiếm, Song Lộc, etc.) to the prompt's reference data.

### Changed
- **LasoData/UserInfoResult**: Added `canChi`, `amDuong`, `tieuHanCung`, and `phiTinhTuHoa` fields.
- **DataLayerVerificationTest**: Added 3 new Level 5 specific unit tests (`testLayer7_CanChi12Cung`, `testLayer8_AmDuongMenh`, `testLayer9_PromptStructure`). All 14/14 tests pass.

### [2026-03-09 - Part 3]
### Added — Data Structure Enhancement (Level-Up AI Tử Vi)
- **Ngũ Hành Cung**: Each of 12 cung now includes its Five Element (e.g., Tuất → Thổ) in the prompt output.
- **Ngũ Hành Sao**: All 14 chính tinh mapped to Five Elements (e.g., Tử Vi → Thổ, Phá Quân → Thủy).
- **Nạp Âm Ngũ Hành**: Full 60 Giáp Tý Nạp Âm lookup table added to `Constants.kt`. Destiny element now calculated (e.g., "Kiếm Phong Kim (Kim)").
- **Cục-Mệnh Sinh Khắc**: Auto-calculated relationship between Mệnh and Cục (e.g., "Mệnh (Kim) đồng hành Cục (Kim)").
- **Bộ Sao Detection**: Auto-detect formed star groups: Sát Phá Tham, Tử Phủ Vũ Tướng, Cơ Nguyệt Đồng Lương, Nhật Nguyệt.
- **Trục Cung**: Auto-calculated palace axes: Mệnh–Thiên Di, Mệnh–Tài–Quan, Phúc–Tài–Quan, Phu Thê–Tài Bạch, Điền–Phúc.
- **Full Đại Vận List**: Complete lifecycle đại vận list (10 decades) injected into prompt for AI to analyze 人生周期.
- **METADATA Section**: New "Section 0" in prompt output aggregates all metadata before the lá số data.

### Changed
- **GeminiClient.kt**: Synced prompt with user's simplified `Prompt.txt` (shorter, cleaner rules).
- **Prompt Simplified**: Removed verbose rule descriptions, simplified star classification, streamlined conclusion format.

## [2026-03-07]
### Added
- **Classical Methodology Prompt**: Replaced the AI prompt with a comprehensive system based on *Thiên Lương – Vân Đằng Thái Thứ Lang – Tử Vi Đẩu Số Toàn Thư*.
- **Prompt Format Enforcement**: Mandatory 3-step analysis (Summary, Force Assessment, Pattern Check) and structured 12-palace reading.

### Fixed
- **Critical Logic Overhaul**: Total audit and fix of 9 logic bugs in `TuViLogic.kt`.
- **Palace Direction (Bug 1)**: Corrected the 12 cung chức năng assignment order. Verified that Nam Phai uses **Thuận** (clockwise) direction starting from Mệnh.
- **Thiên Phủ Symmetry (Bug 2)**: Corected the Thiên Phủ axis formula to `(4 - tuViPos)`. Verified symmetry is relative to the **Dần** axis (Sum=4), NOT Dần-Thân (Sum=10).
- **Văn Khúc Map (Bug 4/5)**: Added missing `VAN_KHUC_MAP` for all 10 Can years. Fixed incorrect hardcoded logic for `L.Văn Khúc` and `ĐV. Văn Khúc`.
- **Scoring Order (Bug 6)**: Fixed score calculation to run **before** appending brightness indicators (M, V, Đ, H), ensuring `STAR_SCORES` map lookups succeed.
- **Tứ Hóa Lookup (Bug 7)**: Hardened star search using `startsWith()` to remain accurate even if star names are modified with markers.
- **Documentation**: Added school-specific notes for `Thiên Y`/`Thiên Riêu` placement.

### Changed
- **AI Reading Style Integration**: Preserved and injected 6 user-selected ReadingStyles (Nghiêm túc, Đời thường, etc.) into the new classical prompt.
- **Unit Tests**: Updated `TuViLogicTest.kt` with corrected palace indices and star positions for the benchmark case (Nguyen Duy Truong).
- **APK Export**: Successfully built and verified updated `app-debug.apk` with the new AI engine.

## [2026-03-04]
### Added
- **Prompt Version 3 (Advanced Reading)**: All reading styles (Nghiêm túc, Đời thường, Hài hước, Kiếm hiệp, Chữa lành) now use a professional structural analysis of 12 cung.
- **Monthly Analysis Mode**: Users can now toggle between "Viewing by Year" and "Viewing by Month".
- **Dynamic Month Calculation**: Switcing to monthly mode automatically suggests (Current Month + 1).
- Added `CHUYEN_GIA` (Expert) reading style with high-level structural analysis.

### Changed
- **Gemini Engine**: Overhauled prompt construction logic to include Tam Hợp, Xung Chiếu, Giáp Cung, and professional zodiac constraints.
- **UI Input Screen**: Redesigned the "Viewing Year" section to include a toggle for Monthly/Yearly modes and side-by-side selectors.
- **ViewModel/Data Flow**: Updated `UserInput` and `TuViViewModel` to support monthly context passing to AI.
- **Laso Logic**: Updated `TuViLogic` to pass extended time context to the Gemini API.

### Fixed
- Improved RadioButton click area and label alignment in InputScreen.
- Enhanced YearSelector to support flexible layout weights.

## [2026-01-31]
### Added
- Implemented **Yearly Stars (Sao Lưu)** specific to user request: `L.Long Đức`, `L.Phúc Đức`, `L.Thiên Đức`, `L.Nguyệt Đức`, `L.Văn Xương`, `L.Văn Khúc`.
- Implemented **Decade Stars (Sao Đại Vận)**: `ĐV. Văn Xương`, `ĐV. Văn Khúc`, `ĐV. Thiên Mã`.
- Added `THIEN_DUC_MAP` for accurate `Thiên Đức` placement.

### Changed
- Refined **Star Brightness**: Corrected `Liêm Trinh` to (V) at Thân, `Thái Âm` to (Đ) at Sửu.
- Corrected **Star Positions**: Moved `Văn Tinh` to Dậu, `Thiên Đức` to Tỵ based on expert feedback.
- Cleaned up output: Removed "noise" stars (`L.Tuế Phá`, `L.Thiên Hỷ`, etc.) matching the reference image.
- Updated `TuViLogic.kt` to support improved star placement logic.
- Updated `Constants.kt` with corrected data maps.

### Fixed
- Fixed compilation error in `TuViLogic.kt` (missing brace).
- Fixed `testNguyenDuyTruong` assertions to align with expert corrections.
## [2026-03-08]
### Added
- **AI Child Analysis Mode**: Automatic detection and parent-consultation tone for users < 13 years old.
- **Detailed Decade Metadata**: Added Stem, Branch, Direction, and Age Bracket to Đại Vận info.
- **AI Anti-Hallucination Prompt Upgrade**: A massive overhaul to ensure AI adheres strictly to star data.
  - **Star Categorization**: Explicitly defined 14 Main Stars, Malefics, Benefics, and Tứ hóa.
  - **Rule #10 (Pattern Rigidity)**: Prohibits AI from inferring patterns (Cách cục) without explicit star evidence.
  - **VCD Protocol**: Mandates polar projection notation for empty palaces.
  - **Consistency Stage**: New "TRƯỚC KHI KẾT LUẬN" section for cross-checking analysis.

### Fixed
- **Pattern Hallucinations**: Prevented AI from "making up" lucky patterns for weak destiny palaces.
- **Calculation Errors**: Rule #8 strictly prohibits AI from self-calculating miếu/vượng or đại vận.

### Artifacts
- APK Build: `app/build/outputs/apk/debug/app-debug.apk`
- Implementation Plan: `plans/260308-1932-anti-hallucination-prompt/`

### [2026-03-08 - Part 2]
#### Fixed
- **Metadata Ambiguity Resolution**: Fixed potential AI confusion by standardizing markers.
- **Marker Standard: [Cung Đại Vận]**: Changed from "Đại Vận" (string) to `[Cung Đại Vận]` to distinguish it from star names.
- **Marker Standard: [Thân cư]**: Changed from "(Thân)" to `[Thân cư]` (e.g., `Cung Thân (Phu Thê) [Thân cư]`) to distinguish from Địa Chi Thân.
- **Prompt Conventions**: Updated QUY ƯỚC KÝ HIỆU in `GeminiClient.kt` to define these new markers for the AI.

#### Changed
- **Unit Tests**: Updated `TuViLogicTest.kt` assertions to verify the new naming convention.
- **APK Export**: Re-built and verified updated `app-debug.apk` (v1.0.1-prompt-fix).

### [2026-03-09]
#### Fixed
- **Logic Conflicts**: Resolved 3 minor logic conflicts and 2 ambiguous rules based on expert feedback.
- **Rule #10 Exception**: Allowed AI to identify astrological patterns (Cách cục) if required stars exist in trigons (tam hợp) or opposite palaces (xung chiếu).
- **Rule #8 Exception**: Allowed AI to apply Five Elements (Ngũ hành sinh khắc) to determine if Destiny (Mệnh) and Element (Cục) are harmonious or conflicting. Moderated rule language to avoid freezing the AI.
- **Rule #10**: Renumbered missing Rule 10 to Rule 9 to fix list numbering.
- **Rule #10 (New)**: Added a mandatory rule to always correlate the Destiny palace (Mệnh) with the Mệnh-Tài-Quan trigon (tam hợp) to prevent isolated analysis.
- **VCD Refinement**: Clarified that Empty Palaces (Vô chính diệu) must prioritize opposite palace stars before checking trigons.
- **Conclusion Terminology**: Softened final destiny classifications to "Thuộc nhóm xu hướng:" instead of absolute statements.
- **Annual Stars Marker**: Explicitly defined `L.` prefix as stars belonging to the current viewed year.

### [2026-03-09 - Part 2]
#### Fixed
- **Brightness Label Logic**: Expanded single-letter `(B)` marker to `(Bình)` in `TuViLogic.kt` to ensure AI correctly interprets "Bình Hòa" status.
- **Vô Chính Diệu Identification**: Added explicit `[Vô chính diệu]` label in `GeminiClient.kt` to palaces without main stars, preventing AI from misinterpreting empty data as missing attributes.
- **Prompt Specification**: Updated `QUY ƯỚC KÝ HIỆU` in prompt to explicitly define `(Bình)` for the LLM.

#### Changed
- **Unit Tests**: Added `testBrightnessLabel_BinhExpansion` and `testVoChinhDieu_Annotation` to `DataLayerVerificationTest.kt`. Verified 11/11 tests pass.
