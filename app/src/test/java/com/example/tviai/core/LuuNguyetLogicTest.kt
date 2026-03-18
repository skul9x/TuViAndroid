package com.example.tviai.core

import com.example.tviai.data.Gender
import com.example.tviai.data.UserInput
import com.example.tviai.data.ViewingMode
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

/**
 * Quick Check cho tính năng Lưu Nguyệt (Monthly Transit)
 */
class LuuNguyetLogicTest {

    private val logic = TuViLogic()

    @Test
    fun test_CanChiThang_NguHoDon() {
        println("=== Test Ngũ Hổ Độn ===")
        // Năm Giáp (index 0) -> Tháng 1 Bính Dần
        val canThang1_Giap = LunarConverter.getCanChiThang(1, 0)
        println("Năm Giáp - Tháng 1: $canThang1_Giap")
        assertEquals("Bính Dần", canThang1_Giap)

        // Năm Ất (index 1) -> Tháng 1 Mậu Dần
        val canThang1_At = LunarConverter.getCanChiThang(1, 1)
        println("Năm Ất - Tháng 1: $canThang1_At")
        assertEquals("Mậu Dần", canThang1_At)

        // Năm Bính (index 2) -> Tháng 1 Canh Dần
        val canThang1_Binh = LunarConverter.getCanChiThang(1, 2)
        println("Năm Bính - Tháng 1: $canThang1_Binh")
        assertEquals("Canh Dần", canThang1_Binh)
        
        // Năm Nhâm (index 8) -> Tháng 1 Nhâm Dần
        val canThang1_Nham = LunarConverter.getCanChiThang(1, 8)
        println("Năm Nhâm - Tháng 1: $canThang1_Nham")
        assertEquals("Nhâm Dần", canThang1_Nham)

        // Tháng 3 năm Nhâm -> Giáp Thìn
        // 1-Nhâm Dần, 2-Quý Mão, 3-Giáp Thìn
        val canThang3_Nham = LunarConverter.getCanChiThang(3, 8)
        println("Năm Nhâm - Tháng 3: $canThang3_Nham")
        assertEquals("Giáp Thìn", canThang3_Nham)
    }

    @Test
    fun test_AnSaoLuuNguyet_Logic() {
        println("\n=== Test An Sao Lưu Nguyệt ===")
        // Giả lập: Nam Nhâm Thân (1992), xem tháng 3/2026 (Bính Ngọ)
        val testInput = UserInput(
            name = "Test User",
            solarDay = 5,
            solarMonth = 3,
            solarYear = 1992,
            hour = 10,
            gender = Gender.NAM,
            viewingYear = 2026,
            viewingMonth = 3,
            viewingMode = ViewingMode.MONTH
        )

        val result = logic.anSao(testInput)
        
        // Năm 2026 là Bính Ngọ (Can index 2)
        // Tháng 3 năm Bính (2) -> 1-Canh Dần, 2-Tân Mão, 3-Nhâm Thìn
        // Can tháng 3 là Nhâm (index 8), Chi là Thìn (index 4)
        
        // Theo công thức mới: Tiểu Hạn tại Thân (8) -> Lùi tháng sinh (2-1=1) -> Mùi (7) -> Tiến giờ Tỵ (5 bước) -> 7+5=12(0) (Tý) = Tháng Giêng
        // Tháng 3 xem: Tý (Giêng) -> Sửu (2) -> Dần (3) -> Cung Lưu Nguyệt tháng 3 là Dần (index 2).
        
        println("Cung Lưu Nguyệt mong đợi: Dần")
        val cungLuuNguyet = result.cung.find { it.phuTinh.contains("[Cung Lưu Nguyệt]") }
        println("Cung Lưu Nguyệt thực tế: ${cungLuuNguyet?.name}")
        assertEquals("Dần", cungLuuNguyet?.name)
        val cungHoi = result.cung.find { it.name == "Hợi" }
        println("Cung Hợi: ${cungHoi?.phuTinh}")
        assertTrue("Cung Hợi phải có LN. Lộc Tồn", cungHoi?.phuTinh?.contains("LN. Lộc Tồn") == true)
        
        // 2. Kiểm tra LN. Thiên Mã (Chi Thìn -> Dần)
        // Thân Tý Thìn mã tại Dần
        val cungDan = result.cung.find { it.name == "Dần" }
        println("Cung Dần: ${cungDan?.phuTinh}")
        assertTrue("Cung Dần phải có LN. Thiên Mã", cungDan?.phuTinh?.contains("LN. Thiên Mã") == true)

        // 3. Kiểm tra LN. Tứ Hóa (Can Nhâm -> Lương Vi Tả Vũ)
        // Hóa Lộc (LN) -> Thiên Lương
        val cungThienLuong = result.cung.find { c -> 
            c.chinhTinh.any { it.startsWith("Thiên Lương") } 
        }
        println("Cung chứa Thiên Lương (${cungThienLuong?.name}): ${cungThienLuong?.phuTinh}")
        assertTrue("Cung Thiên Lương phải có LN. Hóa Lộc", cungThienLuong?.phuTinh?.contains("(LN. Hóa Lộc)") == true)
        
        // 4. Kiểm tra Phi Tinh Lưu Nguyệt string
        println("\nPhi Tinh Lưu Nguyệt Data:\n${result.info.phiTinhLuuNguyet}")
        assertTrue("Dữ liệu Phi Tinh Lưu Nguyệt không được trống", result.info.phiTinhLuuNguyet.isNotEmpty())
        assertTrue("Dữ liệu Phi Tinh Lưu Nguyệt phải chứa thông tin Can Nhâm", result.info.phiTinhLuuNguyet.contains("LN. Tứ Hóa (Nhâm Thìn)"))
    }
}
