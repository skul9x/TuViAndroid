-- 1. Xóa các quy tắc cũ nếu đã tồn tại
DROP POLICY IF EXISTS "Allow anyone to insert data" ON laso_sync;
DROP POLICY IF EXISTS "Allow authenticated users to view data" ON laso_sync;
DROP POLICY IF EXISTS "Enable insert for all users" ON laso_sync;
DROP POLICY IF EXISTS "Enable insert for authenticated users only" ON laso_sync;
DROP POLICY IF EXISTS "Enable insert for anon" ON laso_sync;

-- 2. Đảm bảo bảng tồn tại (Xóa nếu cần reset)
-- DROP TABLE IF EXISTS laso_sync;

CREATE TABLE IF NOT EXISTS laso_sync (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    phone_number TEXT,
    device_info JSONB,
    laso_data JSONB,
    ip_address TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 3. BẬT LẠI BẢO MẬT HÀNG (RLS) CHE CHẮN DATABASE
ALTER TABLE laso_sync ENABLE ROW LEVEL SECURITY;

-- 4. CẤP QUYỀN TRUY CẬP CƠ BẢN CHO CÁC ROLE
-- (Bắt buộc phải có để Policy bên dưới có tác dụng)
GRANT USAGE ON SCHEMA public TO anon, authenticated, service_role;
GRANT ALL PRIVILEGES ON TABLE public.laso_sync TO anon, authenticated, service_role;

-- 5. TẠO POLICY CHUẨN: CHỈ CHO PHÉP THÊM MỚI (INSERT), KHÔNG ĐƯỢC ĐỌC/SỬA/XÓA
CREATE POLICY "Enable insert for anon"
ON public.laso_sync
FOR INSERT
TO anon
WITH CHECK (true); -- Mọi dữ liệu insert đều được chấp nhận

-- 6. TẠO POLICY CHUẨN: CHỈ ADMIN (AUTHENTICATED/SERVICE_ROLE) MỚI ĐƯỢC XEM DỮ LIỆU
CREATE POLICY "Enable read for authenticated users" 
ON public.laso_sync 
FOR SELECT 
TO authenticated 
USING (true);
