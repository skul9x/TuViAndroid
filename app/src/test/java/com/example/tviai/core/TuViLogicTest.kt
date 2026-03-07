package com.example.tviai.core

import com.example.tviai.data.Gender
import com.example.tviai.data.ReadingStyle
import com.example.tviai.data.UserInput
import org.junit.Test
import org.junit.Assert.*

class TuViLogicTest {

    private val logic = TuViLogic()

    @Test
    fun testAnSao_Basic() {
        // Test case: 15/08/1990 (Dương), Giờ Ngọ (12:00), Nam
        val input = UserInput(
            name = "Test User",
            solarDay = 15,
            solarMonth = 8,
            solarYear = 1990,
            hour = 12,
            gender = Gender.NAM,
            viewingYear = 2024
        )
        
        val result = logic.anSao(input)
        
        assertNotNull(result)
        assertEquals("Test User", result.info.name)
        assertEquals("Nam", result.info.gender)
        
        // Verify Cục (Should be verified against Python output or logic)
        // 1990 (Canh Ngọ). Mệnh tại Dần? 
        // Tháng 6 Âm (Approx). Tháng 1=Dần. Tháng 6=Mùi (6+1=7 -> Ngọ?). 
        // Logic: Month from lunar.
        // Let's assume standard lunar conversion is approx correct in our Mock.
        
        // We verify that result contains 12 cung
        assertEquals(12, result.cung.size)
        // Verify Mệnh is set
        assertTrue(result.cung.any { it.chucNang.contains("Mệnh") })
    }
    @Test
    fun testNguyenDuyTruong() {
        // Reference: Nguyen Duy Truong (Nam)
        // DL: 5/3/1992, 9h30 (Tỵ)
        // AL: 2/2/1992 (Nhâm Thân)
        // Viewing Year: 2026 (Bính Ngọ)
        
        val input = UserInput(
            name = "Nguyen Duy Truong",
            solarDay = 5,
            solarMonth = 3,
            solarYear = 1992,
            hour = 9, // 9h30 (Gio Ty)
            gender = Gender.NAM,
            viewingYear = 2026,
            lunarDayInput = 2,
            lunarMonthInput = 2,
            lunarYearInput = 1992
        )
        
        val result = logic.anSao(input)
        
        // Helper to find cung by name or index
        fun getCung(index: Int) = result.cung.find { it.index == index }!!
        
        // println("DEBUG INFO: ...")
        
        val debugMsg = """
            DEBUG INFO:
            Can Chi: ${result.info.canChi}
            Lunar Date: ${result.info.lunarDate}
            Huynh De (9) Stars: ${getCung(9).phuTinh}
            Huynh De (9) Chinh Tinh: ${getCung(9).chinhTinh}
        """.trimIndent()
        
        // Fail with debug info to see it in logs
        // throw RuntimeException(debugMsg) // Uncomment to see debug info 
        
        // 1. Verify Static Stars
        // Cung Phụ Mẫu (Hợi - 11): Thiên Đồng (Đ)
        val phuMau = getCung(11)
        assertTrue("Phụ Mẫu (Hợi) must have Thiên Đồng (Đ)", phuMau.chinhTinh.any { it.contains("Thiên Đồng (Đ)") })
        assertTrue("Phụ Mẫu (Hợi) must have Lưu Hà", phuMau.phuTinh.contains("Lưu Hà"))
        assertTrue("Phụ Mẫu (Hợi) must have Cô Thần", phuMau.phuTinh.contains("Cô Thần"))

        // Cung Phúc Đức (Tý - 0): Vũ Khúc (V), Thiên Phủ (M)
        val phucDuc = getCung(0)
        assertTrue("Phúc Đức (Tý) must have Vũ Khúc (V)", phucDuc.chinhTinh.any { it.contains("Vũ Khúc (V)") })
        assertTrue("Phúc Đức (Tý) must have Thiên Phủ (M)", phucDuc.chinhTinh.any { it.contains("Thiên Phủ (M)") })
        assertTrue("Phúc Đức (Tý) must have Đầu Quân", phucDuc.phuTinh.contains("Đầu Quân"))
        
        // Cung Điền Trạch (Sửu - 1): Thái Dương (Đ), Thái Âm (Đ)
        val dienTrach = getCung(1)
        assertTrue("Điền Trạch must have Thái Dương (Đ)", dienTrach.chinhTinh.any { it.contains("Thái Dương (Đ)") })
        assertTrue("Điền Trạch must have Thái Âm (Đ)", dienTrach.chinhTinh.any { it.contains("Thái Âm (Đ)") })

        // Cung Huynh Đệ (Dậu - 9): Đào Hoa (Thân-Tý-Thìn -> Dậu), Thiên Không
        val huynhDe = getCung(9)
        assertTrue("Huynh Đệ (Dậu) must have Đào Hoa", huynhDe.phuTinh.contains("Đào Hoa"))
        assertTrue("Huynh Đệ (Dậu) must have Thiên Không", huynhDe.phuTinh.contains("Thiên Không"))
        assertTrue("Huynh Đệ (Dậu) must have Văn Tinh", huynhDe.phuTinh.contains("Văn Tinh"))
        
        // 2. Verify Dynamic Stars (2026 - Bính Ngọ)
        // L.Thái Tuế tại Ngọ (6) - Cung Tài Bạch (WAIT: Check logic. Thái Tuế is at Chi Year Name 2026 -> Ngọ (6). Correct.)
        val taiBach = getCung(6) // Ngọ
        val taiBachStars = taiBach.phuTinh.joinToString(", ")
        
        // Assert L.Thái Tuế
        assertTrue("Tài Bạch (Ngọ) must have L.Thái Tuế. Stars: $taiBachStars", taiBach.phuTinh.contains("L.Thái Tuế"))
        
        // L.Kình Dương tại Ngọ (6) (Can Bính -> Lộc Tỵ -> Kình Ngọ)
        assertTrue("Tài Bạch (Ngọ) must have L.Kình Dương. Stars: $taiBachStars (Can Chi Nam Xem: ${LunarConverter.getCanChiNam(2026)})", taiBach.phuTinh.contains("L.Kình Dương"))
        
        // L.Thiên Mã tại Thân (8) (Năm Ngọ -> Mã Thân) - Cung Phu Thê
        val phuThe = getCung(8) // Thân
        assertTrue("Phu Thê (Thân) must have L.Thiên Mã", phuThe.phuTinh.contains("L.Thiên Mã"))
        
        // 3. Verify Static Stars (New)
        // Cung Nô Bộc (Mão - 3): Thiên Thương
        assertTrue("Nô Bộc (3) must have Thiên Thương", getCung(3).phuTinh.contains("Thiên Thương"))
        
        // Cung Tật Ách (Tỵ - 5): Thiên Sứ
        assertTrue("Tật Ách (5) must have Thiên Sứ", getCung(5).phuTinh.contains("Thiên Sứ"))
        // Check Corrected Thiên Đức (Theo Chi Năm Thân (8) -> tại Tỵ 5)
        assertTrue("Tật Ách (5) must have Thiên Đức", getCung(5).phuTinh.contains("Thiên Đức"))
        
        // 4. Verify Decade Stars (Đại Vận)
        // ĐV. Hóa Khoa -> Thái Âm (Sửu).
        // Thái Âm (Đ) is at Sửu (1).
        val dienTrachCheck = getCung(1)
        assertTrue("Sửu must have Thái Âm", dienTrachCheck.chinhTinh.any { it.contains("Thái Âm") })

        // Liêm Trinh at Thân (8) (Phu Thê) must be (V)
        val phuTheCheck = getCung(8)
        assertTrue("Thân must be Phu Thê", phuTheCheck.chucNang.contains("Phu Thê"))
        assertTrue("Liêm Trinh at Thân must be (V)", phuTheCheck.chinhTinh.any { it.contains("Liêm Trinh (V)") })

        // 5. Verify Static Correction
        // Văn Tinh at Dậu (9) (Phụ Mẫu)
        assertTrue("Dậu (Phụ Mẫu) must have Văn Tinh", getCung(9).phuTinh.contains("Văn Tinh"))

        // 6. Verify Noise Removal
        // L.Tuế Phá should NOT be present (removed loop)
        val hasTuoiPha = result.cung.any { it.phuTinh.contains("L.Tuế Phá") }
        assertFalse("L.Tuế Phá should be removed as noise", hasTuoiPha)

        // 7. Verify Phase 9 (Round 2 Experts)
        // Cung Mão (3): L.Long Đức, L.Thiên Đức, ĐV. Văn Xương
        val mao = getCung(3)
        assertTrue("Mão must have L.Long Đức", mao.phuTinh.contains("L.Long Đức"))
        assertTrue("Mão must have L.Thiên Đức", mao.phuTinh.contains("L.Thiên Đức"))
        assertTrue("Mão must have ĐV. Văn Xương", mao.phuTinh.contains("ĐV. Văn Xương"))
        
        // Cung Tỵ (5): L.Phúc Đức
        val ty = getCung(5)
        assertTrue("Tỵ must have L.Phúc Đức", ty.phuTinh.contains("L.Phúc Đức"))
        
        // Cung Ngọ (6): L.Văn Khúc
        val ngo = getCung(6)
        assertTrue("Ngọ must have L.Văn Khúc", ngo.phuTinh.contains("L.Văn Khúc"))
        
        // Cung Thân (8): L.Văn Xương
        val thanCung = getCung(8)
        assertTrue("Thân must have L.Văn Xương", thanCung.phuTinh.contains("L.Văn Xương"))
        
        // Cung Hợi (11): L.Nguyệt Đức, ĐV. Văn Khúc, ĐV. Thiên Mã
        val hoi = getCung(11)
        assertTrue("Hợi must have L.Nguyệt Đức", hoi.phuTinh.contains("L.Nguyệt Đức"))
        assertTrue("Hợi must have ĐV. Văn Khúc", hoi.phuTinh.contains("ĐV. Văn Khúc"))
        assertTrue("Hợi must have ĐV. Thiên Mã", hoi.phuTinh.contains("ĐV. Thiên Mã"))

        // 3. Verify Basic Info
        assertEquals("Nhâm Thân", result.info.canChi) 
    }
}
