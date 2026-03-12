# API Documentation

## Supabase REST API (laso_sync)

Dùng để đồng bộ dữ liệu lá số từ App lên Server.

- **URL**: `{{SUPABASE_URL}}/rest/v1/laso_sync`
- **Method**: `POST`
- **Headers**:
    - `apikey`: `{{ANON_KEY}}`
    - `Authorization`: `Bearer {{ANON_KEY}}`
    - `Content-Type`: `application/json`

### Request Body
```json
{
  "phone_number": "0988123456",
  "device_info": {
    "brand": "Samsung",
    "model": "SM-S918B",
    "os_version": "14",
    "sdk_int": 34
  },
  "laso_data": { ... toàn bộ object LasoData ... },
  "ip_address": "1.2.3.4"
}
```

### Response
- `201 Created`: Thành công.
- `401 Unauthorized`: Sai API Key.
- `400 Bad Request`: Sai định dạng dữ liệu hoặc thiếu cột.
