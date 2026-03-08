# Changelog

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
