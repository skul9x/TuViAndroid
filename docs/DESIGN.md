# �� DESIGN: Xử lý Lá Số Trẻ Em (< 13 tuổi)

Ngày tạo: 2026-03-08
Dựa trên: [Plan - Xử lý lỗi Metadata Đại vặn trẻ em](file:///home/skul9x/.gemini/antigravity/brain/7b6edeca-d967-4afe-85fa-13609eea2a5a/implementation_plan.md)

---

## 1. Vấn đề cần giải quyết

Khi lấy lá số cho trẻ con (dưới 13 tuổi), AI hiện tại vẫn luận giải Cung Quan Lộc (sự nghiệp), Tài Bạch (tiền bạc) và áp đặt Đại Vận khi bé chưa thực sự bước vào Đại Vận đầu tiên. Điều này là sai phương pháp học thuật.

## 2. Giải pháp Kỹ thuật

**A. Sửa logic ở Tầng Dữ liệu (`TuViLogic.kt`)**
- Trẻ chưa vào Đại vận (Tuổi hiện tại < Cục) → Output text: *"Chưa vào đại vận (Đại vận đầu tiên bắt đầu từ tuổi X). Giai đoạn này chỉ xem Đồng Hạn (Tiểu vận trẻ em)."*

**B. Sửa logic ở Tầng AI Prompt (`GeminiClient.kt`)**
- Tính `currentAge = viewingYear - solarYear + 1`.
- Nếu `currentAge < 13` HOẶC chưa vào đại vận: Truyền thêm **Quy tắc #9** vào Prompt.
- Quy tắc #9: Bắt buộc AI xưng hô với phụ huynh, cấm phân tích sâu Quan/Tài/Phu thê, tập trung luận Sức khỏe, Tính cách, Môi trường.

## 3. Checklist Kiểm Tra (Acceptance Criteria)

### TC-01: Trẻ 5 tuổi, Hỏa Lục Cục (Cục = 6)
- [ ] Logic xuất ra: "Chưa vào đại vận..." thay vì "Đại vận thứ 1 (6-15 tuổi)"
- [ ] Prompt chứa Nguyên tắc số 9 (Luận lá số trẻ em)

### TC-02: Người lớn 30 tuổi
- [ ] Logic xuất ra Đại Vận chuẩn như bình thường
- [ ] Prompt KHÔNG chứa Nguyên tắc số 9

---

*Tạo bởi AWF 2.1 - Design Phase*
