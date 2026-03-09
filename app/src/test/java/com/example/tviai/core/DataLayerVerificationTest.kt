package com.example.tviai.core

import com.example.tviai.data.Gender
import com.example.tviai.data.UserInput
import com.example.tviai.data.ViewingMode
import com.example.tviai.data.ReadingStyle
import org.junit.Test
import org.junit.Assert.*

/**
 * Verification test for the 6 new data layers:
 * 1. Ngũ hành cung
 * 2. Ngũ hành sao (static constant check)
 * 3. Nạp Âm mệnh ngũ hành
 * 4. Cục-Mệnh sinh khắc
 * 5. Full đại vận list
 * 6. Bộ sao detection (tested via GeminiClient indirectly)
 */
class DataLayerVerificationTest {

    private val logic = TuViLogic()

    // Use user's own birth data: Nhâm Thân 1992, 10h, Male
    private val testInput = UserInput(
        name = "Test User",
        solarDay = 5,
        solarMonth = 3,
        solarYear = 1992,
        hour = 10,
        gender = Gender.NAM,
        viewingYear = 2026,
        lunarDayInput = 2,
        lunarMonthInput = 2,
        lunarYearInput = 1992
    )

    @Test
    fun testLayer1_NguHanhCung() {
        val result = logic.anSao(testInput)
        // Each cung should have nguHanhCung populated
        for (cung in result.cung) {
            assertTrue("Cung ${cung.name} phải có ngũ hành", cung.nguHanhCung.isNotEmpty())
        }
        // Spot check known values
        val tuatCung = result.cung.find { it.name == "Tuất" }
        assertEquals("Tuất phải là Thổ", "Thổ", tuatCung?.nguHanhCung)

        val tyCung = result.cung.find { it.name == "Tý" }
        assertEquals("Tý phải là Thủy", "Thủy", tyCung?.nguHanhCung)

        val danCung = result.cung.find { it.name == "Dần" }
        assertEquals("Dần phải là Mộc", "Mộc", danCung?.nguHanhCung)

        val ngoCung = result.cung.find { it.name == "Ngọ" }
        assertEquals("Ngọ phải là Hỏa", "Hỏa", ngoCung?.nguHanhCung)

        val thanCung = result.cung.find { it.name == "Thân" }
        assertEquals("Thân phải là Kim", "Kim", thanCung?.nguHanhCung)
    }

    @Test
    fun testLayer2_NguHanhSao_Constants() {
        // Verify the static NGU_HANH_SAO mapping (standard tử vi knowledge)
        assertEquals("Tử Vi", "Thổ", Constants.NGU_HANH_SAO["Tử Vi"])
        assertEquals("Thiên Cơ", "Mộc", Constants.NGU_HANH_SAO["Thiên Cơ"])
        assertEquals("Thái Dương", "Hỏa", Constants.NGU_HANH_SAO["Thái Dương"])
        assertEquals("Vũ Khúc", "Kim", Constants.NGU_HANH_SAO["Vũ Khúc"])
        assertEquals("Thiên Đồng", "Thủy", Constants.NGU_HANH_SAO["Thiên Đồng"])
        assertEquals("Liêm Trinh", "Hỏa", Constants.NGU_HANH_SAO["Liêm Trinh"])
        assertEquals("Thiên Phủ", "Thổ", Constants.NGU_HANH_SAO["Thiên Phủ"])
        assertEquals("Thái Âm", "Thủy", Constants.NGU_HANH_SAO["Thái Âm"])
        assertEquals("Tham Lang", "Mộc", Constants.NGU_HANH_SAO["Tham Lang"])
        assertEquals("Cự Môn", "Thủy", Constants.NGU_HANH_SAO["Cự Môn"])
        assertEquals("Thiên Tướng", "Thủy", Constants.NGU_HANH_SAO["Thiên Tướng"])
        assertEquals("Thiên Lương", "Mộc", Constants.NGU_HANH_SAO["Thiên Lương"])
        assertEquals("Thất Sát", "Kim", Constants.NGU_HANH_SAO["Thất Sát"])
        assertEquals("Phá Quân", "Thủy", Constants.NGU_HANH_SAO["Phá Quân"])
        // Must have exactly 14 entries
        assertEquals("Phải có 14 chính tinh", 14, Constants.NGU_HANH_SAO.size)
    }

    @Test
    fun testLayer3_NapAm_MenhNguHanh() {
        val result = logic.anSao(testInput)
        // Nhâm Thân → Kiếm Phong Kim (Kim)
        assertTrue("Nạp Âm phải chứa 'Kiếm Phong Kim'", result.info.menhNguHanh.contains("Kiếm Phong Kim"))
        assertTrue("Nạp Âm phải chứa '(Kim)'", result.info.menhNguHanh.contains("(Kim)"))
    }

    @Test
    fun testLayer3_NapAm_Constants() {
        // Spot check some famous Nạp Âm entries
        assertEquals("Giáp Tý", "Hải Trung Kim", Constants.NAP_AM_MAP["Giáp Tý"])
        assertEquals("Nhâm Thân", "Kiếm Phong Kim", Constants.NAP_AM_MAP["Nhâm Thân"])
        assertEquals("Quý Hợi", "Đại Hải Thủy", Constants.NAP_AM_MAP["Quý Hợi"])
        // Must have exactly 60 entries (60 Giáp Tý)
        assertEquals("Phải có 60 Nạp Âm entries", 60, Constants.NAP_AM_MAP.size)
    }

    @Test
    fun testLayer3_NapAmToNguHanh() {
        assertEquals("Kim", Constants.napAmToNguHanh("Kiếm Phong Kim"))
        assertEquals("Kim", Constants.napAmToNguHanh("Hải Trung Kim"))
        assertEquals("Thủy", Constants.napAmToNguHanh("Đại Hải Thủy"))
        assertEquals("Hỏa", Constants.napAmToNguHanh("Lư Trung Hỏa"))
        assertEquals("Mộc", Constants.napAmToNguHanh("Đại Lâm Mộc"))
        assertEquals("Thổ", Constants.napAmToNguHanh("Lộ Bàng Thổ"))
    }

    @Test
    fun testLayer4_CucMenhRelation() {
        val result = logic.anSao(testInput)
        // Kim Tứ Cục + Kiếm Phong Kim (Kim) → đồng hành
        assertTrue("Cục-Mệnh phải chứa 'đồng hành'", result.info.cucMenhRelation.contains("đồng hành"))
        assertTrue("Phải ghi rõ Mệnh (Kim)", result.info.cucMenhRelation.contains("Mệnh (Kim)"))
        assertTrue("Phải ghi rõ Cục (Kim)", result.info.cucMenhRelation.contains("Cục (Kim)"))
    }

    @Test
    fun testLayer4_SinhKhac_Logic() {
        // Verify sinh khắc logic
        assertEquals("đồng hành", Constants.sinhKhac("Kim", "Kim"))
        assertEquals("sinh", Constants.sinhKhac("Kim", "Thủy"))     // Kim sinh Thủy
        assertEquals("được sinh", Constants.sinhKhac("Thủy", "Kim")) // Thủy được Kim sinh
        assertEquals("khắc", Constants.sinhKhac("Kim", "Mộc"))       // Kim khắc Mộc
        assertEquals("bị khắc", Constants.sinhKhac("Mộc", "Kim"))    // Mộc bị Kim khắc
        assertEquals("sinh", Constants.sinhKhac("Mộc", "Hỏa"))       // Mộc sinh Hỏa
        assertEquals("khắc", Constants.sinhKhac("Thổ", "Thủy"))      // Thổ khắc Thủy
    }

    @Test
    fun testLayer5_FullDaiVanList() {
        val result = logic.anSao(testInput)
        val dvList = result.info.daiVanFullList
        // Must not be empty
        assertTrue("Đại vận list không được rỗng", dvList.isNotEmpty())
        // Verify 4th decade: 34-43 at Sửu with Can Quý
        assertTrue("Phải chứa '34–43'", dvList.contains("34–43"))
        assertTrue("Decade 4 phải tại Sửu", dvList.contains("34–43: Quý Sửu"))
        // Verify first decade: 4-13 at Mệnh (Tuất)
        assertTrue("Decade 1 phải bắt đầu từ cục=4", dvList.contains("4–13"))
        assertTrue("Decade 1 phải tại Tuất", dvList.contains("4–13: Canh Tuất"))
        // Must have multiple entries separated by |
        val decadeCount = dvList.split("|").size
        assertEquals("Phải có 10 đại vận", 10, decadeCount)
    }

    @Test
    fun testLayer6_BoSao_SatPhaTham() {
        val result = logic.anSao(testInput)
        // In user's chart: Thất Sát at Ngọ(6), Phá Quân at Tuất(10), Tham Lang at Dần(2)
        // 6→10: diff=4 ✓ (tam hợp)
        // 10→2: diff=8 ✓ (tam hợp)
        // This is a REAL Sát Phá Tham group!
        val satCung = result.cung.find { c -> c.chinhTinh.any { it.startsWith("Thất Sát") } }
        val phaCung = result.cung.find { c -> c.chinhTinh.any { it.startsWith("Phá Quân") } }
        val thamCung = result.cung.find { c -> c.chinhTinh.any { it.startsWith("Tham Lang") } }
        assertNotNull("Thất Sát phải có mặt", satCung)
        assertNotNull("Phá Quân phải có mặt", phaCung)
        assertNotNull("Tham Lang phải có mặt", thamCung)
        // Verify tam hợp: indices should be 4 apart
        val satIdx = satCung!!.index
        val phaIdx = phaCung!!.index
        val thamIdx = thamCung!!.index
        println("Sát Phá Tham positions: Sát=$satIdx(${satCung.name}), Phá=$phaIdx(${phaCung.name}), Tham=$thamIdx(${thamCung.name})")
        val diff1 = Math.abs(satIdx - phaIdx)
        val diff2 = Math.abs(phaIdx - thamIdx)
        assertTrue("Sát-Phá phải cách 4 hoặc 8", diff1 == 4 || diff1 == 8)
        assertTrue("Phá-Tham phải cách 4 hoặc 8", diff2 == 4 || diff2 == 8)
    }

    @Test
    fun testBrightnessLabel_BinhExpansion() {
        val result = logic.anSao(testInput)
        // Check if any star has "(B)" - should be replaced with "(Bình)"
        for (cung in result.cung) {
            for (star in cung.chinhTinh) {
                assertFalse("Không được dùng ký hiệu '(B)', hãy dùng '(Bình)' trong $star", star.contains("(B)"))
            }
        }
        // Spot check Thiên Phủ at Mão for Nhâm Thân 1992
    }

    @Test
    fun testVoChinhDieu_Annotation() {
        val client = GeminiClient("fake-key")
        val result = logic.anSao(testInput)
        val prompt = client.getPromptForCopy(result)
        
        // Find palaces without main stars and check if they have [Vô chính diệu]
        for (cung in result.cung) {
            if (cung.chinhTinh.isEmpty()) {
                assertTrue("Cung ${cung.name} vô chính diệu phải có nhãn trong prompt", 
                    prompt.contains("Cung ${cung.name}") && prompt.contains("[Vô chính diệu]"))
            }
        }
    }
}
