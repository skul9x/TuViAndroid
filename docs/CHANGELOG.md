# Changelog

All notable changes to this project will be documented in this file.

## [2026-03-10]
### Fixed
- **Star Calculation:** 
    - Fixed **Thiên Thọ** and **Thiên Tài** calculation logic by using direct palace indices.
    - Corrected **Tiểu Hạn** (Minor Cycle) starting position to follow Nam Phái standard (Tam Hợp Tuổi -> Tứ Mộ).
    - **Star Brightness Sync:** Synchronized 3 main stars (Cự Môn, Thiên Lương, Thiên Đồng) and 4 existing auxiliary stars (Văn Xương, Văn Khúc, Hỏa Tinh, Linh Tinh) with the **AlTuVi** reference image.
- **Stability:** Fixed UI thread blocking by offloading astrological calculations to `Dispatchers.Default`.

### Added
- **Major Cycle (Đại Vận):** Implemented advanced Major Cycle star placing logic (ĐV. Lộc Tồn, ĐV. Tứ Hóa, etc.) based on decade-specific Stems.
- **AI Integration:** 
    - Updated `GeminiClient` with **Expert Prompt v5.1**.
    - **Star Brightness:** 
        - Added Miếu/Hãm mapping for **9 new auxiliary stars**: Tang Môn, Bạch Hổ, Tiểu Hao, Đại Hao, Thiên Khốc, Thiên Hư, Thiên Hình, Thiên Riêu, Thiên Mã.
        - Sourced from multiple Vietnamese astrology references (tuvi.vn, lyso.vn, aituvi.com) and cross-verified with AlTuVi image.
    - **Tuần/Triệt Emphasis:** Added explicit tagging `[BỊ TRIỆT LỘ]` and `[BỊ TUẦN KHÔNG]` for main stars.
    - **Prompt Restructuring:** Separated static stars and transit stars (Đại Vận/Lưu Niên) in the palace view for better AI clarity.

### Build
- Successful debug APK generation with integrated fixes.


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
