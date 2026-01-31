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
        
        // Better: Use assert with message containing debug info
        val huynhDe = getCung(9)
        assertTrue("Huynh Đệ must have Đào Hoa. $debugMsg", huynhDe.phuTinh.contains("Đào Hoa"))
        
        
        // 1. Verify Static Stars
        // Cung Huynh Đệ (Dậu - 9): Thiên Không, Đào Hoa
        // (New assert is above)
        assertTrue("Huynh Đệ must have Thiên Không", huynhDe.phuTinh.contains("Thiên Không"))
        
        // Cung Tử Tức (Mùi - 7): Hồng Loan, Quả Tú
        val tuTuc = getCung(7)
        assertTrue("Tử Tức must have Hồng Loan", tuTuc.phuTinh.contains("Hồng Loan"))
        assertTrue("Tử Tức must have Quả Tú", tuTuc.phuTinh.contains("Quả Tú"))
        
        // Cung Phụ Mẫu (Hợi - 11): Cô Thần, Lưu Hà
        val phuMau = getCung(11)
        assertTrue("Phụ Mẫu must have Cô Thần", phuMau.phuTinh.contains("Cô Thần"))
        assertTrue("Phụ Mẫu must have Lưu Hà", phuMau.phuTinh.contains("Lưu Hà"))
        
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
        assertTrue("Nô Bộc must have Thiên Thương", getCung(3).phuTinh.contains("Thiên Thương"))
        
        // Cung Tật Ách (Tỵ - 5): Thiên Sứ
        assertTrue("Tật Ách must have Thiên Sứ", getCung(5).phuTinh.contains("Thiên Sứ"))
        // Check Corrected Thiên Đức (Tỵ - 5)
        assertTrue("Tật Ách must have Thiên Đức", getCung(5).phuTinh.contains("Thiên Đức"))
        
        // Cung Phúc Đức (Tý - 0): Đầu Quân
        assertTrue("Phúc Đức (Tý) must have Đầu Quân", getCung(0).phuTinh.contains("Đầu Quân"))
        
        // 4. Verify Decade Stars (Đại Vận)
        // ĐV. Hóa Khoa -> Thái Âm (Sửu). Thái Âm (Đ).
        val dienTrach = getCung(1)
        assertTrue("Sửu (Điền Trạch) must have ĐV. Hóa Khoa", dienTrach.phuTinh.contains("(ĐV. H Khoa)"))
        assertTrue("Thái Âm at Sửu must be (Đ)", dienTrach.chinhTinh.any { it.contains("Thái Âm (Đ)") })

        // Liêm Trinh at Thân (8) must be (V)
        val phuTheCheck = getCung(8)
        assertTrue("Liêm Trinh at Thân must be (V)", phuTheCheck.chinhTinh.any { it.contains("Liêm Trinh (V)") })

        // 5. Verify Static Correction
        // Văn Tinh at Dậu (9) (Huynh Đệ)
        assertTrue("Dậu (Huynh Đệ) must have Văn Tinh", getCung(9).phuTinh.contains("Văn Tinh"))

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
