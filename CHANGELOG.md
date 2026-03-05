# Changelog

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
