# Changelog

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
