# Database Schema

## 1. Local Database (Room)
Lưu trữ lịch sử các lá số đã xem trên máy người dùng.

### Table: `laso_history`
- `id`: Primary Key (Long)
- `name`: Tên người xem
- `birthDate`: Ngày sinh
- `gender`: Giới tính
- `resultJson`: Toàn bộ dữ liệu lá số (JSON)
- `timestamp`: Thời gian xem

## 2. Remote Database (Supabase)
Lưu trữ log đồng bộ để Admin quản lý.

### Table: `laso_sync`
- `id`: UUID (Primary Key)
- `phone_number`: SĐT (Ưu tiên người dùng nhập, fallback là SIM)
- `device_info`: Thông tin máy (JSONB)
- `laso_data`: Toàn bộ kết quả lá số (JSONB)
- `ip_address`: Địa chỉ IP của người dùng
- `created_at`: Thời gian đồng bộ
