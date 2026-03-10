package com.example.tviai.core

import com.example.tviai.data.Gender
import com.example.tviai.data.UserInput
import com.example.tviai.data.ViewingMode
import com.example.tviai.data.ReadingStyle
import org.junit.Test
import org.junit.Assert.*

/**
 * Deep verification test for Level 5 Logic.
 * This test PRINTS detailed output for manual review of correctness.
 * 
 * Test Subject: Nhâm Thân 1992 (Male, 5/3/1992, 10h sáng)
 * Lunar: 2/2/1992 (Nhâm Thân)
 * 
 * Known facts for manual cross-check:
 * - Can Nhâm = index 8 -> Ngũ Dần Độn: Đinh/Nhâm -> Nhâm Dần (startCan=8)
 * - Viewing Year 2026: Can Bính (index 2), Chi Ngọ (index 6)
 * - Tuổi Âm 2026: 2026 - 1992 + 1 = 35
 * - Chi năm sinh: Thân (index 8)
 * - Nhâm = Dương -> Nam + Dương = Thuận hành
 */
class Level5DeepVerificationTest {

    private val logic = TuViLogic()
    
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
    fun deepDump_CanChi12Cung() {
        val result = logic.anSao(testInput)
        
        println("=" .repeat(60))
        println("DEEP DUMP: CAN CHI 12 CUNG")
        println("Năm sinh: Nhâm Thân 1992 (Can Nhâm = index 8)")
        println("Ngũ Dần Độn: Đinh/Nhâm -> startCanDần = 8 (Nhâm)")
        println("=" .repeat(60))
        
        // Print expected vs actual for all 12 cung
        // Expected: startCanDan = 8 (Nhâm), so:
        // Index 0 (Tý): dist from Dần(2) = (0-2)%12 = 10 -> can = (8+10)%10 = 8 = Nhâm -> Nhâm Tý? 
        // Wait: (0-2) = -2, mod 12 = 10. (8+10)%10 = 18%10 = 8 = Nhâm. -> "Nhâm Tý"
        // Hmm, but that would be wrong. Let me think again...
        // Tý is index 0. Dần is index 2. Distance from Dần to Tý going forward = 10 steps.
        // Can of Tý = (startCanDan + 10) % 10 = (8 + 10) % 10 = 8 = Nhâm.
        // But Nhâm Tý doesn't exist in the cycle. Let me verify:
        // Actually the 10 Can cycle just repeats, so any Can + Chi is valid.
        // The rule: from Dần=Nhâm, +1 step for each cung:
        // Dần(idx2)=Nhâm, Mão(3)=Quý, Thìn(4)=Giáp, Tỵ(5)=Ất, Ngọ(6)=Bính, Mùi(7)=Đinh,
        // Thân(8)=Mậu, Dậu(9)=Kỷ, Tuất(10)=Canh, Hợi(11)=Tân, Tý(0)=Nhâm, Sửu(1)=Quý
        
        val expectedCanChi = mapOf(
            "Tý" to "Nhâm Tý",
            "Sửu" to "Quý Sửu", 
            "Dần" to "Nhâm Dần",   // startCan
            "Mão" to "Quý Mão",
            "Thìn" to "Giáp Thìn",
            "Tỵ" to "Ất Tỵ",
            "Ngọ" to "Bính Ngọ",
            "Mùi" to "Đinh Mùi",
            "Thân" to "Mậu Thân",
            "Dậu" to "Kỷ Dậu",
            "Tuất" to "Canh Tuất",
            "Hợi" to "Tân Hợi"
        )
        
        var allCorrect = true
        for (cung in result.cung) {
            val expected = expectedCanChi[cung.name] ?: "?"
            val actual = cung.canChi
            val match = if (expected == actual) "✅" else "❌"
            if (expected != actual) allCorrect = false
            println("Cung %-4s: Expected=%-12s Actual=%-12s %s".format(cung.name, expected, actual, match))
        }
        println()
        assertTrue("Can Chi 12 cung phải chính xác theo Ngũ Dần Độn", allCorrect)
    }

    @Test
    fun deepDump_TieuHan() {
        val result = logic.anSao(testInput)
        
        println("=" .repeat(60))
        println("DEEP DUMP: TIỂU HẠN")
        println("Chi năm sinh: Thân (index 8)")
        println("Tuổi Âm 2026: 35")
        println("Giới tính: Nam. Nhâm = Dương -> Thuận hành")
        println("=" .repeat(60))
        
        // Correct standard rule: Nam starts from Tam Hợp Tuổi (Tứ Mộ), count FORWARD
        // Chi sinh: Thân (8) -> Tam Hợp: Thân-Tý-Thìn -> Khởi: Tuất (10)
        // Age 1 = Tuất. Age 2 = Hợi. ...
        // steps = age - 1 = 34
        // pos = (10 + 34) % 12 = 44 % 12 = 8 = Thân
        val expectedTieuHanCung = "Thân"
        
        println("Công thức: (startPos + (age-1) * direction) %% 12")
        println("         = (10 + 34 * 1) %% 12")
        println("         = 44 %% 12")
        println("         = 8 = Thân")
        println()
        println("Expected Tiểu Hạn cung: $expectedTieuHanCung")
        println("Actual   Tiểu Hạn cung: ${result.info.tieuHanCung}")
        
        val match = expectedTieuHanCung == result.info.tieuHanCung
        println("Match: ${if (match) "✅" else "❌"}")
        println()
        
        assertEquals("Tiểu Hạn 2026 phải ở cung Thân", expectedTieuHanCung, result.info.tieuHanCung)
    }
    
    @Test
    fun deepDump_AmDuong() {
        val result = logic.anSao(testInput)
        
        println("=" .repeat(60))
        println("DEEP DUMP: ÂM DƯƠNG METADATA")
        println("Can Nhâm = index 8 -> 8 %% 2 = 0 -> DƯƠNG")
        println("Gender = Nam")
        println("Dương + Nam = Thuận hành (cùng dương)")
        println("=" .repeat(60))
        
        println("Expected: 'Dương Nam\\nÂm dương nghịch lý\\nMệnh đóng tại cung Âm'")
        println("Actual:   '${result.info.amDuong}'")
        
        assertTrue("Phải có Dương Nam", result.info.amDuong.contains("Dương Nam"))
        assertTrue("Phải có Âm dương nghịch lý", result.info.amDuong.contains("Âm dương nghịch lý"))
        println("Match: ✅")
        println()
    }

    @Test
    fun deepDump_PhiTinhTuHoa() {
        val result = logic.anSao(testInput)
        
        println("=" .repeat(60))
        println("DEEP DUMP: PHI TINH TỨ HÓA (Pre-computed)")
        println("=" .repeat(60))
        
        // Print the raw Phi Tinh data
        val phiTinh = result.info.phiTinhTuHoa
        if (phiTinh.isNotEmpty()) {
            println(phiTinh)
        } else {
            println("⚠️ EMPTY! Phi Tinh data is empty!")
        }
        
        assertTrue("Phi Tinh data phải có nội dung", phiTinh.isNotEmpty())
        
        // Verify specific cases:
        // Cung Tuất has Can Canh (from deepDump_CanChi12Cung above)
        // Canh (index 6): TU_HOA_MAP[6] = ["Thái Dương", "Vũ Khúc", "Thái Âm", "Thiên Đồng"]
        // So Canh Tuất: Hóa Lộc -> Thái Dương, Hóa Quyền -> Vũ Khúc, Hóa Khoa -> Thái Âm, Hóa Kỵ -> Thiên Đồng
        
        println()
        println("--- SPOT CHECK: Cung Tuất (Can Canh, index 6 trong TU_HOA_MAP) ---")
        println("TU_HOA_MAP[6] = Thái Dương, Vũ Khúc, Thái Âm, Thiên Đồng")
        
        // Find where these stars actually are in this chart
        val tuCung = result.cung
        val thaiDuongCung = tuCung.find { c -> c.chinhTinh.any { it.startsWith("Thái Dương") } }
        val vuKhucCung = tuCung.find { c -> c.chinhTinh.any { it.startsWith("Vũ Khúc") } }
        val thaiAmCung = tuCung.find { c -> c.chinhTinh.any { it.startsWith("Thái Âm") } }
        val thienDongCung = tuCung.find { c -> c.chinhTinh.any { it.startsWith("Thiên Đồng") } }
        
        println("  H.Lộc → Thái Dương ở cung: ${thaiDuongCung?.name ?: "NOT FOUND"}")
        println("  H.Quyền → Vũ Khúc ở cung: ${vuKhucCung?.name ?: "NOT FOUND"}")
        println("  H.Khoa → Thái Âm ở cung: ${thaiAmCung?.name ?: "NOT FOUND"}")
        println("  H.Kỵ → Thiên Đồng ở cung: ${thienDongCung?.name ?: "NOT FOUND"}")
        
        // Verify the Phi Tinh line for Tuất contains these targets
        val tuatLine = phiTinh.lines().find { it.startsWith("Tuất(Canh)") }
        println()
        println("Phi Tinh line for Tuất: $tuatLine")
        
        assertNotNull("Phải có dòng Phi Tinh cho cung Tuất", tuatLine)
        if (thaiDuongCung != null) {
            assertTrue("Tuất H.Lộc phải bay về cung ${thaiDuongCung.name}", 
                tuatLine!!.contains("H.Lộc→${thaiDuongCung.name}"))
        }
        if (vuKhucCung != null) {
            assertTrue("Tuất H.Quyền phải bay về cung ${vuKhucCung.name}", 
                tuatLine!!.contains("H.Quyền→${vuKhucCung.name}"))
        }
        
        println()
        
        // Now verify count: should have 12 lines (one for each cung)
        val lineCount = phiTinh.trim().lines().count { it.isNotBlank() }
        println("Tổng số dòng Phi Tinh: $lineCount (expected: 12 hoặc gần 12)")
        println()
    }
    
    @Test
    fun deepDump_TuHoaMapVerification() {
        println("=" .repeat(60))
        println("DEEP DUMP: BẢNG TRA TỨ HÓA 10 CAN (Constants check)")
        println("=" .repeat(60))
        
        // Standard Tứ Hóa Table (Nam Phái):
        val expectedTuHoa = mapOf(
            0 to listOf("Liêm Trinh", "Phá Quân", "Vũ Khúc", "Thái Dương"),     // Giáp
            1 to listOf("Thiên Cơ", "Thiên Lương", "Tử Vi", "Thái Âm"),           // Ất
            2 to listOf("Thiên Đồng", "Thiên Cơ", "Văn Xương", "Liêm Trinh"),     // Bính
            3 to listOf("Thái Âm", "Thiên Đồng", "Thiên Cơ", "Cự Môn"),           // Đinh
            4 to listOf("Tham Lang", "Thái Âm", "Hữu Bật", "Thiên Cơ"),           // Mậu
            5 to listOf("Vũ Khúc", "Tham Lang", "Thiên Lương", "Văn Khúc"),       // Kỷ
            6 to listOf("Thái Dương", "Vũ Khúc", "Thái Âm", "Thiên Đồng"),       // Canh
            7 to listOf("Cự Môn", "Thái Dương", "Văn Khúc", "Văn Xương"),         // Tân
            8 to listOf("Thiên Lương", "Tử Vi", "Tả Phù", "Vũ Khúc"),             // Nhâm
            9 to listOf("Phá Quân", "Cự Môn", "Thái Âm", "Tham Lang")             // Quý
        )
        
        var allCorrect = true
        val canNames = listOf("Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý")
        val hoaNames = listOf("Lộc", "Quyền", "Khoa", "Kỵ")
        
        for (i in 0..9) {
            val expected = expectedTuHoa[i]!!
            val actual = Constants.TU_HOA_MAP[i] ?: listOf()
            val matches = expected == actual
            if (!matches) allCorrect = false
            
            println("${canNames[i]}: ${if (matches) "✅" else "❌"}")
            for (j in 0..3) {
                val expStar = expected[j]
                val actStar = if (actual.size > j) actual[j] else "N/A"
                val starMatch = expStar == actStar
                println("  ${hoaNames[j]}: Expected=%-12s Actual=%-12s ${if (starMatch) "✅" else "❌ MISMATCH!"}".format(expStar, actStar))
            }
        }
        
        assertTrue("TU_HOA_MAP phải khớp với bảng chuẩn Nam Phái", allCorrect)
    }
    
    @Test
    fun deepDump_FullPromptMetadata() {
        val client = GeminiClient("fake-key")
        val result = logic.anSao(testInput)
        val prompt = client.getPromptForCopy(result)
        
        println("=" .repeat(60))
        println("DEEP DUMP: PROMPT METADATA SECTION (Section 0)")
        println("=" .repeat(60))
        
        // Extract Section 0 from the prompt
        val lines = prompt.lines()
        var inMetadata = false
        for (line in lines) {
            if (line.contains("METADATA LÁ SỐ")) {
                inMetadata = true
            }
            if (inMetadata) {
                println(line)
                if (line.contains("THÔNG TIN CƠ BẢN")) break
            }
        }
        
        println()
        println("=== KEY CHECKS ===")
        
        // 1. Check Am Duong is present
        val hasAmDuong = prompt.contains("Âm/Dương mệnh")
        println("Âm/Dương mệnh present: ${if (hasAmDuong) "✅" else "❌"}")
        
        // 2. Check Can Chi 12 cung
        val hasCanChi = prompt.contains("Can Chi 12 cung")
        println("Can Chi 12 cung present: ${if (hasCanChi) "✅" else "❌"}")
        
        // 3. Check Tieu Han
        val hasTieuHan = prompt.contains("Tiểu Hạn năm")
        println("Tiểu Hạn năm present: ${if (hasTieuHan) "✅" else "❌"}")
        
        // 4. Check Phi Tinh
        val hasPhiTinh = prompt.contains("Phi Tinh Tứ Hóa")
        println("Phi Tinh Tứ Hóa present: ${if (hasPhiTinh) "✅" else "❌"}")
        
        // 5. Check 10 Can table
        val has10Can = prompt.contains("BẢNG TRA TỨ HÓA 10 CAN")
        println("Bảng tra 10 Can present: ${if (has10Can) "✅" else "❌"}")
        
        assertTrue("All metadata sections must be present", 
            hasAmDuong && hasCanChi && hasTieuHan && hasPhiTinh && has10Can)
    }

    @Test
    fun deepDump_TieuHan_NuMenh() {
        // Test with a female subject to verify reverse counting
        val femaleInput = UserInput(
            name = "Test Female",
            solarDay = 5,
            solarMonth = 3,
            solarYear = 1992,
            hour = 10,
            gender = Gender.NU,
            viewingYear = 2026,
            lunarDayInput = 2,
            lunarMonthInput = 2,
            lunarYearInput = 1992
        )
        
        val result = logic.anSao(femaleInput)
        
        println("=" .repeat(60))
        println("DEEP DUMP: TIỂU HẠN (NỮ MỆNH)")
        println("Chi năm sinh: Thân (index 8)")
        println("Tuổi Âm 2026: 35")
        println("Giới tính: Nữ -> direction = -1 (đếm nghịch)")
        println("=" .repeat(60))
        
        // Nữ: count BACKWARD from startPos (Tuất 10)
        // pos = (10 + 34 * (-1)) % 12 = (10 - 34) % 12 = -24 % 12 = 0 = Tý
        val expectedTieuHanCung = "Tý"
        
        println("Công thức: (startPos + (age-1) * direction) %% 12")
        println("         = (10 + 34 * (-1)) %% 12")
        println("         = (10 - 34) %% 12") 
        println("         = -24 %% 12")
        println("         = 0 = Tý")
        println()
        println("Expected Tiểu Hạn cung: $expectedTieuHanCung")
        println("Actual   Tiểu Hạn cung: ${result.info.tieuHanCung}")
        
        val match = expectedTieuHanCung == result.info.tieuHanCung
        println("Match: ${if (match) "✅" else "❌ MISMATCH!"}")
        
        // Also verify Am Duong for female
        println()
        println("Âm Dương Nữ: '${result.info.amDuong}'")
        // Nhâm = Dương, Nữ -> Dương Nữ
        // Mệnh cũng tại Hợi (cung Âm) -> Dương gặp Âm -> Âm dương nghịch lý
        val expectedAmDuongMale = "Dương Nữ\nÂm dương nghịch lý\nMệnh đóng tại cung Âm"
        println("Expected to contain: Dương Nữ, Âm dương nghịch lý") 
        println("Match: ${if (result.info.amDuong.contains("Dương Nữ") && result.info.amDuong.contains("Âm dương nghịch lý")) "✅" else "❌"}")
        
        assertEquals(expectedTieuHanCung, result.info.tieuHanCung)
        assertTrue("Phải chứa Dương Nữ", result.info.amDuong.contains("Dương Nữ"))
        assertTrue("Phải chứa Âm dương nghịch lý", result.info.amDuong.contains("Âm dương nghịch lý"))
    }

    @Test
    fun deepDump_DetectBoSao_Labels() {
        val result = logic.anSao(testInput)
        val client = GeminiClient("fake-key")
        val prompt = client.getPromptForCopy(result)
        
        println("=" .repeat(60))
        println("DEEP DUMP: BỘ SAO LABELS (GEMINI CLIENT)")
        println("=" .repeat(60))
        
        // Find "Nhóm sao hội hợp" line in prompt
        val boSaoLine = prompt.lines().find { it.contains("Nhóm sao hội hợp:") }
        println("Line detected: $boSaoLine")
        
        assertNotNull("Phải có dòng 'Nhóm sao hội hợp' trong prompt", boSaoLine)
        
        val content = boSaoLine!!.substringAfter("Nhóm sao hội hợp:").trim()
        
        // Nhâm Thân 1992 case:
        // Mệnh ở Thân có Thất Sát (index 8)
        // Tài ở Thìn có Phá Quân (index 4)
        // Quan ở Tý có Tham Lang (index 0)
        // All in Tam Hợp Thân-Tý-Thìn (index 8, 4, 0)
        
        assertTrue("Sát Phá Tham phải có nhãn 'Tam hợp'", content.contains("Tam hợp Sát Phá Tham"))
        
        if (content.contains("Nhật Nguyệt")) {
            val hasDetail = content.contains("đồng cung") || content.contains("hội chiếu") || content.contains("đối chiếu")
            assertTrue("Nhật Nguyệt phải có nhãn chi tiết (đồng cung/hội chiếu/đối chiếu)", hasDetail)
        }
        
        if (content.contains("Tử Phủ Vũ Tướng")) {
            val hasDetail = content.contains("Nhóm") || content.contains("hội chiếu")
            assertTrue("Tử Phủ Vũ Tướng phải có nhãn chi tiết (Nhóm/hội chiếu)", hasDetail)
        }
        
        println("Bo Sao Verification: ✅")
    }

    @Test
    fun deepDump_DaiVan_HoaLabel() {
        val result = logic.anSao(testInput)
        
        println("=" .repeat(60))
        println("DEEP DUMP: ĐẠI VẬN TỨ HÓA LABELS")
        println("=" .repeat(60))
        
        var foundAny = false
        for (cung in result.cung) {
            for (sao in cung.phuTinh) {
                if (sao.contains("ĐV. H ")) {
                    fail("Phát hiện ký hiệu viết tắt 'ĐV. H ' tại cung ${cung.name}: $sao. Phải đổi thành 'ĐV. Hóa '")
                }
                if (sao.contains("ĐV. Hóa")) {
                    println("Cung ${cung.name}: $sao [CORRECT]")
                    foundAny = true
                }
            }
        }
        
        assertTrue("Phải tìm thấy ít nhất 1 sao Tứ Hóa Đại Vận (với ký hiệu chuẩn)", foundAny)
        println("Hoa Label Verification: ✅")
    }

    @Test
    fun deepDump_PromptCachCucRanking() {
        val client = GeminiClient("fake-key")
        val result = logic.anSao(testInput)
        val prompt = client.getPromptForCopy(result)
        
        println("=" .repeat(60))
        println("DEEP DUMP: PROMPT CACH CUC RANKING BLOCK")
        println("=" .repeat(60))
        
        val hasRankingBlock = prompt.contains("BƯỚC 3b – XẾP HẠNG CÁCH CỤC")
        val hasValidationWarning = prompt.contains("AI phải TỰ XÁC ĐỊNH đây có phải \"Cách cục\" thật sự hay không")
        val hasNewNotation = prompt.contains("Nhóm [Bộ sao]") && prompt.contains("Tam hợp [Bộ sao]")
        
        println("Ranking block present: ${if (hasRankingBlock) "✅" else "❌"}")
        println("Validation warning present: ${if (hasValidationWarning) "✅" else "❌"}")
        println("New notations present: ${if (hasNewNotation) "✅" else "❌"}")
        
        assertTrue("Prompt phải chứa block 'XẾP HẠNG CÁCH CỤC'", hasRankingBlock)
        assertTrue("Prompt phải chứa cảnh báo tự xác định cách cục", hasValidationWarning)
        assertTrue("Prompt phải chứa định nghĩa Tam hợp/Nhóm", hasNewNotation)
        
        println("Prompt Ranking Verification: ✅")
    }

    @Test
    fun deepDump_Level5PatchBlocks() {
        val client = GeminiClient("fake-key")
        val result = logic.anSao(testInput)
        val prompt = client.getPromptForCopy(result)
        
        println("=" .repeat(60))
        println("DEEP DUMP: LEVEL 5 PATCH BLOCKS PRESENCE")
        println("=" .repeat(60))
        
        val blocks = listOf(
            "QUY TẮC ƯU TIÊN KHI TÍN HIỆU MÂU THUẪN",
            "QUY TẮC TRỌNG SỐ TƯƠNG TÁC",
            "QUY TẮC VÔ CHÍNH DIỆU (4 bước)",
            "CÁC LỖI PHỔ BIẾN AI KHÔNG ĐƯỢC MẮC",
            "LỰC CUNG:",
            "TÓM TẮT TỨ HÓA"
        )
        
        for (block in blocks) {
            val present = prompt.contains(block)
            println("Block '$block' present: ${if (present) "✅" else "❌"}")
            assertTrue("Prompt phải chứa block: $block", present)
        }
        println("Level 5 Blocks Verification: ✅")
    }

    @Test
    fun deepDump_TuHoaSummaryAccuracy() {
        val result = logic.anSao(testInput)
        val client = GeminiClient("fake-key")
        val prompt = client.getPromptForCopy(result)
        
        println("=" .repeat(60))
        println("DEEP DUMP: TỨ HÓA SUMMARY ACCURACY")
        println("=" .repeat(60))
        
        // Find the summary section
        val summaryLine = prompt.lines().find { it.contains("TÓM TẮT TỨ HÓA") }
        assertNotNull("Phải có block TÓM TẮT TỨ HÓA", summaryLine)
        
        println("Tứ Hóa Summary Found: ✅")
        
        // Verify specific known Tứ Hóa for Nhâm Thân 1992
        // Bản mệnh Nhâm: Lộc-Lương(Tỵ), Quyền-Vi(Thìn), Khoa-Phù(Tỵ), Kỵ-Khúc(Tý)
        assertTrue("Summary phải có Hóa Lộc bản mệnh", prompt.contains("(Hóa Lộc) → Cung Tỵ"))
        assertTrue("Summary phải có Hóa Quyền bản mệnh", prompt.contains("(Hóa Quyền) → Cung Thìn"))
        assertTrue("Summary phải có Hóa Kỵ bản mệnh", prompt.contains("(Hóa Kỵ) → Cung Tý"))
        
        // 2026 Bính: Lộc-Đồng(Hợi), Quyền-Cơ(Mão), Khoa-Xương(Tỵ), Kỵ-Liêm(Thân)
        assertTrue("Summary phải có L.Hóa Lộc", prompt.contains("(L.Hóa Lộc) → Cung Hợi"))
        assertTrue("Summary phải có L.Hóa Kỵ", prompt.contains("(L.Hóa Kỵ) → Cung Thân"))
        
        println("Tứ Hóa Content Accuracy: ✅")
    }

    @Test
    fun deepDump_Level5FinalPolish_3Blocks() {
        val client = GeminiClient("fake-key")
        val result = logic.anSao(testInput)
        val prompt = client.getPromptForCopy(result)
        
        println("=" .repeat(60))
        println("DEEP DUMP: LEVEL 5 FINAL POLISH - 3 BLOCKS PRESENCE")
        println("=" .repeat(60))
        
        // Block 1: BƯỚC 4 – KIỂM TRA MÂU THUẪN
        val hasBuoc4 = prompt.contains("BƯỚC 4 – KIỂM TRA MÂU THUẪN")
        val hasMenhVsThan = prompt.contains("- Mệnh vs Thân:")
        
        println("BƯỚC 4 block present: ${if (hasBuoc4) "✅" else "❌"}")
        println("Mệnh vs Thân check present: ${if (hasMenhVsThan) "✅" else "❌"}")
        
        assertTrue("Prompt phải chứa BƯỚC 4 kiểm tra mâu thuẫn", hasBuoc4)
        assertTrue("Prompt phải chứa Mệnh vs Thân check", hasMenhVsThan)
        
        // Block 2: Khóa bảng tra Tứ Hóa
        val hasTuHoaLock = prompt.contains("Bảng tra Tứ Hóa 10 Can CHỈ dùng để GIẢI THÍCH")
        println("Tứ Hóa table lock present: ${if (hasTuHoaLock) "✅" else "❌"}")
        assertTrue("Prompt phải chứa cảnh báo khóa bảng tra Tứ Hóa", hasTuHoaLock)
        
        // Block 3: Ép format vận năm bắt buộc (E1)
        val hasE1Section = prompt.contains("E1. Vận năm ${testInput.viewingYear}")
        val hasTrungDiep = prompt.contains("(3) Trùng điệp tứ hóa")
        
        println("E1 section present: ${if (hasE1Section) "✅" else "❌"}")
        println("Trùng điệp tứ hóa check present: ${if (hasTrungDiep) "✅" else "❌"}")
        
        assertTrue("Prompt phải chứa mục E1 vận năm ${testInput.viewingYear}", hasE1Section)
        assertTrue("Prompt phải chứa check trùng điệp tứ hóa", hasTrungDiep)
        
        println("Level 5 Final Polish Verification: ✅")
    }
}
