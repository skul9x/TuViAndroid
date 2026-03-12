# System Overview

## Purpose
Tử Vi AI là ứng dụng luận giải lá số tử vi kết hợp trí tuệ nhân tạo (Gemini AI). Phiên bản hiện tại tích hợp thêm hệ thống đồng bộ dữ liệu người dùng (Telemetry) lên Supabase để quản lý và hỗ trợ khách hàng.

## Core Components

### 1. UI Layer (Jetpack Compose)
- **InputScreen**: Thu thập thông tin người dùng (Họ tên, SĐT, Ngày sinh...).
- **LasoScreen**: Hiển thị lá số chi tiết.
- **AnalysisScreen**: Hiển thị lời luận giải từ AI.

### 2. Logic Layer
- **TuViLogic**: Chứa toàn bộ thuật toán an sao, tính cục, mệnh, đại vận, tiểu vận chuẩn xác.
- **LunarConverter**: Chuyển đổi âm dương lịch.

### 3. Data & Sync Layer
- **TelemetryRepository**: Chịu trách nhiệm gom dữ liệu và gửi lên Supabase qua REST API.
- **DeviceInterceptor**: Thu thập thông tin phần cứng và SIM.
- **Supabase**: Backend dùng để lưu trữ log đồng bộ.

### 4. AI Integration
- **Gemini AI**: Nhận dữ liệu lá số thô và trả về nội dung luận giải theo phong cách người dùng chọn.

## Data Flow
1. User nhập thông tin -> `TuViViewModel`.
2. Bấm "Xem lá số" -> `TuViLogic` tính toán kết quả.
3. Đồng thời:
    - Lưu vào DB nội bộ (Room).
    - Trigger `TelemetryRepository` chạy ngầm.
    - Gọi Gemini AI để luận giải.
4. `TelemetryRepository` lấy IP, SĐT, Device Info -> Gửi lên Supabase.
