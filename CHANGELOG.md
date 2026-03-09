# Changelog

## [2026-03-09]
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
