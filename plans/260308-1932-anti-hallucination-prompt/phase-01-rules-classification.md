# Phase 01: Siết chặt Nguyên tắc & Phân loại sao
Status: ✅ Complete
Dependencies: Không

## Objective
Chuẩn hóa phân loại sao và ngăn AI tự phân loại lại, loại bỏ kẽ hở lớn nhất.

## Implementation Steps

### Task 1: Chuẩn hóa mục 4 (Phân loại sao)
- [x] Sửa mục `4. Phân biệt rõ:` trong `NGUYÊN TẮC TUYỆT ĐỐI`
- [x] Thay bằng danh sách cụ thể:
```text
4. Không tự phân loại lại sao. Chỉ dùng cách phân loại truyền thống:
   • 14 Chính tinh: Tử Vi, Thiên Cơ, Thái Dương, Vũ Khúc, Thiên Đồng, Liêm Trinh,
     Thiên Phủ, Thái Âm, Tham Lang, Cự Môn, Thiên Tướng, Thiên Lương, Thất Sát, Phá Quân.
   • Sát tinh chính: Kình Dương, Đà La, Hỏa Tinh, Linh Tinh, Địa Không, Địa Kiếp.
   • Cát tinh chính: Tả Phụ, Hữu Bật, Văn Xương, Văn Khúc, Lộc Tồn, Thiên Khôi, Thiên Việt.
   • Tứ hóa: Hóa Lộc, Hóa Quyền, Hóa Khoa, Hóa Kỵ (đã ký hiệu sẵn trong dữ liệu).
   • Các sao còn lại: Phụ tinh — không tự ý nâng cấp thành cát tinh hoặc sát tinh.
```

### Task 2: Thêm Rule mới (Rule #10) — Cấm tự suy diễn cách cục
- [x] Thêm rule mới sau rule #9 (hoặc cuối block rules):
```text
10. ⛔ KHÔNG ĐƯỢC tự suy diễn cách cục khi dữ liệu không đủ.
    Chỉ xác nhận một cách cục khi CÁC SAO tạo cách xuất hiện ĐÚNG CUNG theo điều kiện.
    Nếu không đủ điều kiện → ghi rõ: "Không đủ dữ kiện để xác nhận cách [tên cách]".
```

## Files to Modify
- `app/src/main/java/com/example/tviai/core/GeminiClient.kt` — hàm `constructPrompt`, phần NGUYÊN TẮC TUYỆT ĐỐI

## Test Criteria
- [ ] Build thành công (không lỗi compile)
- [ ] Prompt output chứa danh sách 14 chính tinh

---
Next Phase: phase-02-strengthen-steps.md
