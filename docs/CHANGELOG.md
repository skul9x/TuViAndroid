# Changelog

All notable changes to this project will be documented in this file.

## [2026-03-10]
### Fixed
- **Expert AI Prompt (GeminiClient):**
    - **Fix #11 (Ambiguity):** Reworded `priority_rules[1]` to remove "sáng" (brightness) ambiguity. Now explicitly enforces use of provided M/V/Đ/Bình/H symbols and forbids AI from self-evaluating star brightness.
    - **Fix #12 (Contradiction):** Rewrote `validation_warning` to align with `data_integrity.forbidden`. AI is now instructed to verify configuration validity using ONLY the provided metadata (states, Tứ hóa, Tuần/Triệt flags) instead of trying to calculate them.
    - **Fix #13 (Empty Stars):** Implemented final cleaning of empty star names in JSON output to ensure payload integrity.
- **Star Calculation:** 
    - Fixed **Thiên Thọ** and **Thiên Tài** calculation logic by using direct palace indices.
    - Corrected **Tiểu Hạn** (Minor Cycle) starting position to follow Nam Phái standard (Tam Hợp Tuổi -> Tứ Mộ).
    - **Star Brightness Sync:** Synchronized 3 main stars (Cự Môn, Thiên Lương, Thiên Đồng) and 4 existing auxiliary stars (Văn Xương, Văn Khúc, Hỏa Tinh, Linh Tinh) with the **AlTuVi** reference image.
- **Stability:** Fixed UI thread blocking by offloading astrological calculations to `Dispatchers.Default`.

### Added
- **Expert AI Prompt v5.2 (JSON):**
    - **Fix #6 (Tuần/Triệt Cross-check):** Added `tuan_triet_check_rule` requiring a full scan of all 12 palaces using the `palace data` flags, preventing AI from missing Tuần/Triệt effects outside of metadata summaries.
    - **Fix #9 (Fortune Mapping):** Added a structured `fortune_context` block mapping the viewing year (e.g., 2026) to the active Major Cycle (Đại vận) and providing an "Overlap Guide" for Song Kỵ/Song Lộc analysis.
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
