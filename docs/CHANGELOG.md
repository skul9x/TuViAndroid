# Changelog

All notable changes to this project will be documented in this file.

## [2026-01-31]
### Added
- **Advanced Tu Vi Logic:**
    - **Tuần/Triệt:** Added logic and lookup tables for Year Stem.
    - **Tứ Hóa:** Added calculation for Lộc, Quyền, Khoa, Kỵ.
    - **Sát Tinh:** Added logic for Hỏa Tinh, Linh Tinh, Thiên Hình customized for user requirements.
    - **Star Brightness:** Implemented Miếu/Vượng/Đắc/Hãm/Bình mapping for 14 Main Stars.
- **UI:** New circular App Icon with AI/Bagua theme.

### Changed
- **Core Logic:**
    - Corrected 12 Palace Arrangement to run Clockwise (Nam Phai Standard).
    - Updated `GeminiClient` to include complete list of stars (`phuTinh`) and special modifiers in the prompt.
    - Swapped **Thiên Khôi / Thiên Việt** positions for **Can Nhâm** (Khôi at Mão, Việt at Tỵ) per user school.
- **Lunar Date:** Replaced algorithmic approach with Table Lookup for stability (1900-2049).
