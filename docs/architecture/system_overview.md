# System Overview: TuViAI Level 5 Architecture

## Overview
Dự án TuViAI hiện tại đã đạt mức **Level 5 Astrology Engine**, kết hợp giữa logic an sao vần đế và mô hình LLM (Gemini) được hướng dẫn bằng bộ Prompt kỹ thuật cao (Anti-Hallucination & Structural Analysis).

---

## 🏗️ Core Architecture

### 1. Data Layer (`TuViLogic.kt` & `Constants.kt`)
- **Astrology Engine**: Thực hiện 100% việc an cung, an sao, tính độ sáng (M/V/Đ/H) và tính điểm.
- **Advanced Logic**:
    - **Ngũ Dần Độn**: Tính Can của 12 cung.
    - **Phi Tinh Tứ Hóa**: Tính toán causality (Nhân quả) giữa các cung dựa trên Can cung (10 can tra 4 sao).
    - **Tiểu Hạn**: Tính cung hạn năm dựa trên giới tính và chi năm sinh.
    - **Hệ thống Nạp Âm**: Tra cứu 60 hoa giáp để tính Mệnh ngũ hành.
    - **Trục Cung & Cục-Mệnh**: Phân tích quan hệ ngũ hành giữa các tầng dữ liệu.

### 2. Prompt Engine (`GeminiClient.kt`)
- **Metadata Infusion**: Trước khi gửi dữ liệu sao, hệ thống "bơm" 6 lớp metadata (Section 0) để AI có cái nhìn tổng quan:
    1. Ngũ hành Cung/Sao.
    2. Quan hệ Cục-Mệnh.
    3. Trục cung (Trục ngang/dọc/chéo).
    4. Nhóm sao hội hợp (Tam phương tứ chính).
    5. Full danh sách Đại Vận (10 thập kỷ).
    6. Phi Tinh Tứ Hóa pre-computed.
- **Anti-Hallucination Protocol**:
    - **Rule #8**: Cấm AI tự tính dữ liệu.
    - **Rule #9**: Hướng dẫn riêng cho trẻ em (<13 tuổi).
    - **Rule #10**: Ép AI phải luận Mệnh kèm theo tam hợp Mệnh-Tài-Quan.
- **Step-by-Step Methodology**:
    - Bước 1: Tóm tắt cấu trúc.
    - Bước 2: Đánh giá Lực (Mạnh/Yếu).
    - Bước 3: Kiểm tra Cách cục (Sát Phá Tham, Tử Phủ Vũ Tướng...).
    - **Bước 3b (Ranking)**: Xếp hạng cách cục chủ đạo dựa trên Lực, Vị trí và Tứ Hóa.

### 3. Verification Layer (Unit Tests)
- **Deep Verification Suite**: `Level5DeepVerificationTest.kt` kiểm tra tính chính xác của:
    - Can Chi 12 cung.
    - Vị trí Tiểu Hạn (Nam thuận, Nữ nghịch).
    - Logic hội hợp của `detectBoSao()`.
    - Các nhãn (labels) đặc biệt như `(Bình)`, `[Vô chính diệu]`, `ĐV. Hóa Lộc`.

---

## 📁 Project Structure Keywords
- **`core/TuViLogic.kt`**: Trái tim logic an sao.
- **`core/GeminiClient.kt`**: Cầu nối AI và Prompt Engineering.
- **`data/Constants.kt`**: Cơ sở dữ liệu tra cứu (Nạp Âm, Tứ Hóa, Cách cục).
- **`test/.../Level5DeepVerificationTest.kt`**: Bộ test "sống còn" của dự án.

---
*Updated: 2026-03-09 (Expert Feedback Refinement)*
