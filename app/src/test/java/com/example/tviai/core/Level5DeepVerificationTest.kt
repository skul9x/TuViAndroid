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
        hour = 9,
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
        assertTrue("Phải có thuận lý hoặc nghịch lý", result.info.amDuong.contains("thuận lý") || result.info.amDuong.contains("nghịch lý"))
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
        println("DEEP DUMP: PROMPT METADATA (JSON Format)")
        println("=" .repeat(60))
        
        println("=== KEY CHECKS (JSON keys) ===")
        
        val hasAmDuong = prompt.contains("am_duong")
        println("am_duong present: ${if (hasAmDuong) "✅" else "❌"}")
        
        val hasCanChi = prompt.contains("can_chi_12_cung")
        println("can_chi_12_cung present: ${if (hasCanChi) "✅" else "❌"}")
        
        val hasTieuHan = prompt.contains("tieu_han_cung")
        println("tieu_han_cung present: ${if (hasTieuHan) "✅" else "❌"}")
        
        val hasPhiTinh = prompt.contains("phi_tinh_tu_hoa")
        println("phi_tinh_tu_hoa present: ${if (hasPhiTinh) "✅" else "❌"}")
        
        val has10Can = prompt.contains("tu_hoa_10_can")
        println("tu_hoa_10_can present: ${if (has10Can) "✅" else "❌"}")
        
        assertTrue("All metadata sections must be present", 
            hasAmDuong && hasCanChi && hasTieuHan && hasPhiTinh && has10Can)
    }

    @Test
    fun deepDump_TieuHan_NuMenh() {
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
        
        println("=".repeat(60))
        println("DEEP DUMP: TIỂU HẠN (NỮ MỆNH)")
        println("=".repeat(60))
        
        val expectedTieuHanCung = "Tý"
        
        println("Expected Tiểu Hạn cung: $expectedTieuHanCung")
        println("Actual   Tiểu Hạn cung: ${result.info.tieuHanCung}")
        
        val match = expectedTieuHanCung == result.info.tieuHanCung
        println("Match: ${if (match) "✅" else "❌ MISMATCH!"}")
        
        // Also verify Am Duong for female
        println()
        println("Âm Dương Nữ: '${result.info.amDuong}'")
        println("Match: ${if (result.info.amDuong.contains("Dương Nữ") && (result.info.amDuong.contains("thuận lý") || result.info.amDuong.contains("nghịch lý"))) "✅" else "❌"}")
        
        assertEquals(expectedTieuHanCung, result.info.tieuHanCung)
        assertTrue("Phải chứa Dương Nữ", result.info.amDuong.contains("Dương Nữ"))
        assertTrue("Phải chứa thuận lý hoặc nghịch lý", result.info.amDuong.contains("thuận lý") || result.info.amDuong.contains("nghịch lý"))
    }

    @Test
    fun deepDump_DetectBoSao_Labels() {
        val result = logic.anSao(testInput)
        val client = GeminiClient("fake-key")
        val prompt = client.getPromptForCopy(result)
        
        println("=" .repeat(60))
        println("DEEP DUMP: BỘ SAO LABELS (JSON Format)")
        println("=" .repeat(60))
        
        // In JSON format, nhom_sao is a key in metadata
        assertTrue("Prompt phải có key nhom_sao", prompt.contains("nhom_sao"))
        assertTrue("Sát Phá Tham phải có nhãn 'Tam hợp'", prompt.contains("Tam hợp Sát Phá Tham"))
        
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
        println("DEEP DUMP: PROMPT CACH CUC RANKING (JSON)")
        println("=" .repeat(60))
        
        val hasRankingBlock = prompt.contains("step_3b_ranking")
        val hasValidationWarning = prompt.contains("AI TỰ XÁC ĐỊNH")
        val hasNewNotation = prompt.contains("tam_hop") && prompt.contains("nhom")
        
        println("Ranking block present: ${if (hasRankingBlock) "✅" else "❌"}")
        println("Validation warning present: ${if (hasValidationWarning) "✅" else "❌"}")
        println("New notations present: ${if (hasNewNotation) "✅" else "❌"}")
        
        assertTrue("Prompt phải chứa ranking block", hasRankingBlock)
        assertTrue("Prompt phải chứa cảnh báo tự xác định cách cục", hasValidationWarning)
        assertTrue("Prompt phải chứa notation keys", hasNewNotation)
        
        println("Prompt Ranking Verification: ✅")
    }

    @Test
    fun deepDump_Level5PatchBlocks() {
        val client = GeminiClient("fake-key")
        val result = logic.anSao(testInput)
        val prompt = client.getPromptForCopy(result)
        
        println("=" .repeat(60))
        println("DEEP DUMP: LEVEL 5 PATCH BLOCKS (JSON)")
        println("=" .repeat(60))
        
        val blocks = listOf(
            "priority_rules",
            "interaction_weights",
            "vo_chinh_dieu_rules",
            "common_mistakes",
            "reasoning_rules",
            "force_score",
            "tu_hoa_summary"
        )
        
        for (block in blocks) {
            val present = prompt.contains(block)
            println("Block '$block' present: ${if (present) "✅" else "❌"}")
            assertTrue("Prompt phải chứa JSON key: $block", present)
        }
        println("Level 5 Blocks Verification: ✅")
    }

    @Test
    fun deepDump_TuHoaSummaryAccuracy() {
        val result = logic.anSao(testInput)
        val client = GeminiClient("fake-key")
        val prompt = client.getPromptForCopy(result)
        
        println("=" .repeat(60))
        println("DEEP DUMP: TỨ HÓA SUMMARY ACCURACY (JSON)")
        println("=" .repeat(60))
        
        assertTrue("Phải có tu_hoa_summary key", prompt.contains("tu_hoa_summary"))
        assertTrue("Phải có ban_menh key", prompt.contains("ban_menh"))
        
        println("Tứ Hóa Summary Found: ✅")
        
        // Verify Tứ Hóa entries exist in JSON
        assertTrue("Summary phải có Hóa Lộc", prompt.contains("(Hóa Lộc)"))
        assertTrue("Summary phải có Hóa Quyền", prompt.contains("(Hóa Quyền)"))
        assertTrue("Summary phải có Hóa Kỵ", prompt.contains("(Hóa Kỵ)"))
        assertTrue("Summary phải có luu_nien key", prompt.contains("luu_nien"))
        
        println("Tứ Hóa Content Accuracy: ✅")
    }

    @Test
    fun deepDump_Level5FinalPolish_3Blocks() {
        val client = GeminiClient("fake-key")
        val result = logic.anSao(testInput)
        val prompt = client.getPromptForCopy(result)
        
        println("=" .repeat(60))
        println("DEEP DUMP: LEVEL 5 FINAL POLISH (JSON)")
        println("=" .repeat(60))
        
        // Block 1: contradiction check
        val hasBuoc4 = prompt.contains("step_4_contradiction_check")
        val hasMenhVsThan = prompt.contains("Mệnh vs Thân")
        
        println("Contradiction check present: ${if (hasBuoc4) "✅" else "❌"}")
        println("Mệnh vs Thân present: ${if (hasMenhVsThan) "✅" else "❌"}")
        
        assertTrue("Prompt phải chứa contradiction check", hasBuoc4)
        assertTrue("Prompt phải chứa Mệnh vs Thân", hasMenhVsThan)
        
        // Block 2: Tu Hoa table lock
        val hasTuHoaLock = prompt.contains("GIẢI THÍCH cơ chế phi tinh")
        println("Tứ Hóa table lock present: ${if (hasTuHoaLock) "✅" else "❌"}")
        assertTrue("Prompt phải chứa cảnh báo khóa bảng tra Tứ Hóa", hasTuHoaLock)
        
        // Block 3: Fortune year E1
        val hasE1Section = prompt.contains("BẮT BUỘC")
        val hasTrungDiep = prompt.contains("Trùng điệp")
        
        println("E1 section present: ${if (hasE1Section) "✅" else "❌"}")
        println("Trùng điệp present: ${if (hasTrungDiep) "✅" else "❌"}")
        
        assertTrue("Prompt phải chứa E1 BẮT BUỘC", hasE1Section)
        assertTrue("Prompt phải chứa trùng điệp tứ hóa", hasTrungDiep)
        
        println("Level 5 Final Polish Verification: ✅")
    }

    // === EXPERT REVIEW FIX TESTS (4 high-priority issues) ===

    @Test
    fun expertFix_PriorityRulesNoAmbiguousBrightness() {
        val client = GeminiClient("fake-key")
        val result = logic.anSao(testInput)
        val prompt = client.getPromptForCopy(result)
        
        println("=" .repeat(60))
        println("EXPERT FIX #11: priority_rules — no ambiguous 'sáng'")
        println("=" .repeat(60))
        
        // Must NOT contain old ambiguous text
        assertFalse("KHÔNG được chứa 'sáng quyết định lực' (mơ hồ)", 
            prompt.contains("sáng quyết định lực"))
        
        // Must contain new explicit text
        assertTrue("Phải chứa 'trạng thái quyết định lực'", 
            prompt.contains("trạng thái quyết định lực"))
        assertTrue("Phải nhắc dùng ký hiệu M/V/Đ/Bình/H có sẵn", 
            prompt.contains("M/V/Đ/Bình/H có sẵn"))
        assertTrue("Phải cấm tự đánh giá sáng/tối", 
            prompt.contains("KHÔNG tự đánh giá sáng/tối"))
        
        println("Old ambiguous 'sáng quyết định lực': ❌ (removed)")
        println("New explicit 'trạng thái quyết định lực': ✅")
        println("M/V/Đ/Bình/H constraint: ✅")
        println("Expert Fix #11: ✅")
    }

    @Test 
    fun expertFix_ValidationWarningNoContradiction() {
        val client = GeminiClient("fake-key")
        val result = logic.anSao(testInput)
        val prompt = client.getPromptForCopy(result)
        
        println("=" .repeat(60))
        println("EXPERT FIX #12: validation_warning — no contradiction")
        println("=" .repeat(60))
        
        // Must NOT contain old contradictory text telling AI to check brightness itself
        assertFalse("KHÔNG được chứa 'kiểm tra độ sáng (Miếu/Vượng hay Hãm)' (mâu thuẫn với forbidden)", 
            prompt.contains("kiểm tra độ sáng (Miếu/Vượng hay Hãm)"))
        
        // Must contain new non-contradictory text referencing existing data
        assertTrue("Phải chứa 'CÓ SẴN trong dữ liệu sao'", 
            prompt.contains("CÓ SẴN trong dữ liệu sao"))
        assertTrue("Phải chứa fallback 'không đủ căn cứ'", 
            prompt.contains("không đủ căn cứ"))
        
        // Also verify data_integrity.forbidden still present (no accidental removal)
        assertTrue("data_integrity.forbidden vẫn phải cấm tự tính miếu/vượng", 
            prompt.contains("Tự tính miếu/vượng/đắc/bình/hãm"))
        
        println("Old contradictory 'kiểm tra độ sáng': ❌ (removed)")
        println("New 'CÓ SẴN trong dữ liệu sao': ✅")
        println("Fallback 'không đủ căn cứ': ✅")
        println("data_integrity.forbidden still intact: ✅")
        println("Expert Fix #12: ✅")
    }
    
    @Test
    fun expertFix_TuanTrietCheckRule12Cung() {
        val client = GeminiClient("fake-key")
        val result = logic.anSao(testInput)
        val prompt = client.getPromptForCopy(result)
        
        println("=" .repeat(60))
        println("EXPERT FIX #6: tuan_triet_check_rule — 12-cung cross-check")
        println("=" .repeat(60))
        
        // Must contain new key
        assertTrue("Phải có key 'tuan_triet_check_rule'", 
            prompt.contains("tuan_triet_check_rule"))
        
        // Must require full 12-cung scan
        assertTrue("Phải yêu cầu quét toàn bộ 12 cung", 
            prompt.contains("quét toàn bộ 12 cung"))
        
        // Must reference existing data only
        assertTrue("Phải chỉ dùng flags và palace data có sẵn", 
            prompt.contains("palace data"))
        
        // Must not allow self-calculation
        assertTrue("Phải cấm tự tính vị trí Tuần/Triệt",
            prompt.contains("KHÔNG tự tính vị trí Tuần/Triệt"))
        
        println("Key 'tuan_triet_check_rule' present: ✅")
        println("Full 12-cung scan required: ✅")
        println("Palace data reference: ✅")
        println("Self-calc forbidden: ✅")
        println("Expert Fix #6: ✅")
    }

    @Test
    fun expertFix_FortuneContextMapping() {
        val client = GeminiClient("fake-key")
        val result = logic.anSao(testInput)
        val prompt = client.getPromptForCopy(result)
        
        println("=" .repeat(60))
        println("EXPERT FIX #9: fortune_context — year-to-đại-vận mapping")
        println("=" .repeat(60))
        
        // Must contain fortune_context key
        assertTrue("Phải có key 'fortune_context'", 
            prompt.contains("fortune_context"))
        
        // Must contain the viewing year
        assertTrue("Phải chứa năm xem (2026)", 
            prompt.contains("\"year\""))
            
        // Must contain đại vận info
        assertTrue("Phải chứa 'dai_van_current'", 
            prompt.contains("dai_van_current"))
        
        // Must contain overlap guide
        assertTrue("Phải chứa 'tu_hoa_overlap_guide'", 
            prompt.contains("tu_hoa_overlap_guide"))
        assertTrue("Phải nhắc Song Kỵ/Song Lộc", 
            prompt.contains("Song Kỵ") && prompt.contains("Song Lộc"))
        assertTrue("Phải nhắc ưu tiên Mệnh/Quan/Tài/Phu Thê",
            prompt.contains("Mệnh/Quan/Tài/Phu Thê"))
        
        println("Key 'fortune_context' present: ✅")
        println("Year mapping: ✅")
        println("Đại vận current: ✅")
        println("Tứ hóa overlap guide: ✅")
        println("Song Kỵ/Song Lộc reference: ✅")
        println("Mệnh/Quan/Tài/Phu Thê priority: ✅")
        println("Expert Fix #9: ✅")
    }

    @Test
    fun verifyPillarsForTruong() {
        val jsonPath = "/home/skul9x/Desktop/Test_code/TuViAndroid-main/solar-term.json"
        val jsonContent = java.io.File(jsonPath).readText()
        val tuviLogic = TuViLogic(jsonContent)
        
        val birthDateInput = testInput.copy(
            solarDay = 5,
            solarMonth = 3,
            solarYear = 1992,
            hour = 9,
            gender = Gender.NAM
        )
        
        val result = tuviLogic.anSao(birthDateInput)
        val baziData = result.info.baZiData
        
        println("============================================================")
        println("DETAILED VERIFICATION FOR 1992-03-05 09:00:00")
        println("Lunar Date: ${result.info.lunarDate} (Giờ ${LunarConverter.getChiGio(birthDateInput.hour)})")
        println("Mệnh: ${result.info.menhNguHanh} at ${result.info.menhTai}")
        println("Cục: ${result.info.cuc}")
        println("Âm Dương: ${result.info.amDuong}")
        
        if (baziData != null) {
            println("\nBAZI PILLARS:")
            println("Year Pillar:  ${baziData.year.stem}${baziData.year.branch}")
            println("Month Pillar: ${baziData.month.stem}${baziData.month.branch}")
            println("Day Pillar:   ${baziData.day.stem}${baziData.day.branch}")
            println("Hour Pillar:  ${baziData.hour.stem}${baziData.hour.branch}")
        } else {
            println("\nBaZiData is NULL!")
        }
        
        println("\nSTAR GROUPS (Mệnh, Tài, Quan, Di):")
        result.cung.filter { it.chucNang in listOf("Mệnh", "Tài Bạch", "Quan Lộc", "Thiên Di") }.forEach { cung ->
            println("${cung.chucNang} (${cung.name}): ${cung.chinhTinh.joinToString(", ")}")
            if (cung.phuTinh.isNotEmpty()) {
                println("  Phụ tinh: ${cung.phuTinh.joinToString(", ")}")
            }
        }
        println("============================================================")
    }
}
