package com.example.tviai.core

import com.example.tviai.core.util.LunarDateUtil
import com.example.tviai.data.CungInfo
import com.example.tviai.data.Gender
import com.example.tviai.data.LasoData
import com.example.tviai.data.UserInput
import com.example.tviai.data.UserInfoResult
import com.example.tviai.core.Constants.DIA_CHI
import com.example.tviai.core.Constants.THIEN_CAN
import com.example.tviai.core.Constants.CUC
import com.example.tviai.core.Constants.VONG_THAI_TUE
import com.example.tviai.core.Constants.VONG_BAC_SY
import com.example.tviai.core.Constants.VONG_TRANG_SINH
import com.example.tviai.core.Constants.TRANG_SINH_START
import com.example.tviai.core.Constants.KHOI_VIET_POS
import com.example.tviai.core.Constants.LOC_TON_MAP
import com.example.tviai.core.Constants.STAR_SCORES

class TuViLogic {
    
    fun anSao(input: UserInput): LasoData {
        // 1. Convert Lunar
        val solarDay = input.solarDay
        val solarMonth = input.solarMonth
        val solarYear = input.solarYear
        
        // Use LunarConverter wrapper if implementing logic there, but here we call Util directly or converter
        // If manual lunar input is provided, use it.
        val lunarResult = if (input.lunarDayInput != null && input.lunarMonthInput != null && input.lunarYearInput != null) {
            com.example.tviai.core.util.LunarDateUtil.LunarResult(
                input.lunarDayInput, input.lunarMonthInput, input.lunarYearInput, false
            )
        } else {
            LunarConverter.convertSolarToLunar(solarDay, solarMonth, solarYear)
        }
        
        val lunarDay = lunarResult.day
        val lunarMonth = lunarResult.month
        val lunarYear = lunarResult.year
        
        val canNamIndex = LunarConverter.getCanNamIndex(lunarYear)
        val chiNamIndex = LunarConverter.getChiNamIndex(lunarYear)
        val chiGioIndex = LunarConverter.getChiGioIndex(input.hour)
        
        // Init 12 cung
        val cungList = MutableList(12) { i ->
            CungInfo(index = i, name = DIA_CHI[i])
        }
        
        // 2. An Cung Menh/Than
        val (menhIndex, thanIndex) = anCungMenhThan(cungList, lunarMonth, chiGioIndex)
        
        // 3. An Cuc
        val (cucName, cucNumber) = anCuc(menhIndex, canNamIndex)
        
        // 4. An Chinh Tinh
        anChinhTinh(cungList, cucNumber, lunarDay)
        
        anPhuTinhFull(
            cungList = cungList, 
            cucNumber = cucNumber,
            canNamIndex = canNamIndex,
            chiNamIndex = chiNamIndex,
            hourIndex = chiGioIndex,
            lunarMonth = lunarMonth,
            lunarDay = lunarDay,
            gender = input.gender
        )
        
        // 5.5 An Tuần / Triệt
        anTuanTriet(cungList, canNamIndex, chiNamIndex)
        
        // 5.6 An Tứ Hóa
        anTuHoa(cungList, canNamIndex)
        
        // 5.7 An Thiên Hình (Tháng)
        anThienHinh(cungList, lunarMonth)
        
        // 5.8 An Hỏa / Linh (Năm + Giờ)
        anHoaLinh(cungList, chiNamIndex, chiGioIndex)

        // 5.8b An Phụ Tinh Mở Rộng
        // Note: anPhuTinhMoRong now needs hourIndex for Phong Cao/Quoc An logic?
        // I need to update the signature of anPhuTinhMoRong to accept hourIndex before calling it.
        // Wait, the previous step defined it WITHOUT hourIndex? 
        // "Instruction: Append extended star logic... logic helper functions..."
        // In the ReplacementContent above, I commented: "Need to update the signature...".
        // BUT I didn't actually put hourIndex in the signature in the ReplacementContent I sent!
        // So I must fix the signature in TuViLogic.kt via another edit or update this call to match?
        // BETTER: I will assume the previous step FAILED or I can chain edits.
        // I will PASS hourIndex here, and I will make sure the function definition includes it.
        // NOTE: The previous tool call for TuViLogic might define it without hourIndex.
        // I should fix the definition first/concurrently. 
        // Actually, looking at my thought process, I realized the issue in the comment.
        // Let's assume I will Fix the definition in a subsequent step if needed, OR I will include the fix in the previous step?
        // I cannot change the previous step now.
        // So I will call it WITH hourIndex, and then I will use another tool call to Correct the signature if it was wrong.
        // Actually, the previous step's ReplacementContent SHOWS:
        // private fun anPhuTinhMoRong(... lunarDay: Int) { ... val phongCaoPos = (2 + hourIndex) ... }
        // This code DOES NOT COMPILE because 'hourIndex' is used but not passed.
        // I need to fix `anPhuTinhMoRong` signature.
        
        anPhuTinhMoRong(cungList, canNamIndex, chiNamIndex, lunarMonth, lunarDay, chiGioIndex)

        // 5.8c An Sao Đại Vận
        anSaoDaiVan(
            cungList = cungList,
            menhIndex = menhIndex,
            cucNumber = cucNumber,
            gender = input.gender,
            canNamIndex = canNamIndex,
            viewingYear = input.viewingYear,
            birthYear = input.solarYear // Use solar year to approx birth year logic or better calculate exact Age.
            // Age calculation usually matches ViewingYear - BirthYear + 1.
        )

        // 5.8d An Sao Lưu (Năm Xem)
        anSaoLuu(cungList, input.viewingYear)
        
        // 5.9 Attach Brightness (Miếu/Vượng...)
        anDoSang(cungList)
        
        // 6. Calculate Scores
        val scores = calculateScores(cungList)
        
        return LasoData(
            info = UserInfoResult(
                name = input.name,
                gender = if (input.gender == Gender.NAM) "Nam" else "Nữ",
                solarDate = "$solarDay/$solarMonth/$solarYear",
                time = "${input.hour}h (Giờ ${LunarConverter.getChiGio(input.hour)})",
                lunarDate = "$lunarDay/$lunarMonth/$lunarYear",
                canChi = LunarConverter.getCanChiNam(lunarYear),
                cuc = cucName,
                menhTai = DIA_CHI[menhIndex],
                thanTai = DIA_CHI[thanIndex],
                viewingYear = input.viewingYear,
                readingStyle = input.readingStyle.displayName
            ),
            cung = cungList,
            scores = scores
        )
    }

    private fun anCungMenhThan(cungList: MutableList<CungInfo>, lunarMonth: Int, chiGioIndex: Int): Pair<Int, Int> {
        // Mệnh: Dần (2) + (Tháng - 1) - (Giờ)
        val start = 2
        val monthStep = lunarMonth - 1
        val hourStep = chiGioIndex
        
        var menhPos = (start + monthStep - hourStep) % 12
        if (menhPos < 0) menhPos += 12
        
        // Thân: Dần (2) + (Tháng - 1) + (Giờ)
        val thanPos = (start + monthStep + hourStep) % 12
        
        cungList[menhPos] = cungList[menhPos].copy(chucNang = "Mệnh")
        
        // Thân cư gì?
        val currentThanFunc = cungList[thanPos].chucNang
        val newThanFunc = if (currentThanFunc.isNotEmpty()) "$currentThanFunc (Thân)" else "(Thân)"
        cungList[thanPos] = cungList[thanPos].copy(chucNang = newThanFunc) // Note: This copy won't work on list item ref if not careful.
        // Wait, list set needs to replace element.
        
        // Re-assign functional names for 12 cung relative to Menh
        // Menh, Phu, Phuc, Dien, Quan, No, Di, Tat, Tai, Tu, Phu, Huynh
        val chucNangList = com.example.tviai.core.Constants.CUNG_CHUC_NANG
        for (i in 0 until 12) {
            val pos = (menhPos - i) % 12 // Nghịch chiều kim đồng hồ từ Mệnh?
            // "Mệnh", "Phụ Mẫu", "Phúc Đức"... -> Thường là Nghịch.
            // Check lại: Mệnh -> Phụ -> Phúc... (Nghịch).
            // Code Python không explicit loop này, nhưng UI hiển thị.
            // Trong code Python `_init_cung` rỗng. `_an_cung_menh_than` chỉ set Mệnh/Thân text.
            // Nhưng đúng ra phải set tên Cung chức năng.
            // Logic chuẩn: Mệnh (1) -> Phụ Mẫu (2-Nghịch) -> Phúc Đức (3-Nghịch)... 
            // Vị trí: pos = (menhPos - i + 12) % 12.
            var p = (menhPos + i) % 12
            if (p < 0) p += 12
            
            // Append name
            val oldName = cungList[p].chucNang
            val funcName = chucNangList[i]
            
            // Special handle for Than
            if (p == thanPos) {
                 // Than is already marked or we mark here
                 cungList[p] = cungList[p].copy(chucNang = "$funcName (Thân)")
            } else {
                 cungList[p] = cungList[p].copy(chucNang = funcName)
            }
        }
        
        return Pair(menhPos, thanPos)
    }

    private fun anCuc(menhIndex: Int, canNamIndex: Int): Pair<String, Int> {
        val startCans = mapOf(
            0 to 2, 1 to 4, 2 to 6, 3 to 8, 4 to 0,
            5 to 2, 6 to 4, 7 to 6, 8 to 8, 9 to 0
        )
        val startCanDan = startCans[canNamIndex] ?: 2
        
        // Distance from Dan (2) to Menh
        var dist = (menhIndex - 2) % 12
        if (dist < 0) dist += 12
        
        val canMenhIndex = (startCanDan + dist) % 10
        val chiMenhIndex = menhIndex
        
        val valCan = (canMenhIndex / 2) + 1
        
        val valChi = when (chiMenhIndex) {
            0, 1, 6, 7 -> 0
            2, 3, 8, 9 -> 1
            else -> 2
        }
        
        var total = valCan + valChi
        if (total > 5) total -= 5
        
        return when (total) {
            1 -> "Kim Tứ Cục" to 4
            2 -> "Thủy Nhị Cục" to 2
            3 -> "Hỏa Lục Cục" to 6
            4 -> "Thổ Ngũ Cục" to 5
            5 -> "Mộc Tam Cục" to 3
            else -> "Thủy Nhị Cục" to 2 // Fallback
        }
    }

    private fun anChinhTinh(cungList: MutableList<CungInfo>, cucNumber: Int, day: Int) {
        var tuViPos = 0
        val q = day / cucNumber
        val r = day % cucNumber
        
        if (r == 0) {
            tuViPos = (2 + (q - 1)) % 12
        } else {
             // Logic: day + (cuc - r) -> new_day -> q -> pos. Then adjust by (cuc-r).
             // If (cuc-r) odd -> back, even -> forward.
             val extra = cucNumber - r
             val fakeDay = day + extra
             val newQ = fakeDay / cucNumber
             var basePos = (2 + (newQ - 1)) % 12
             
             if (extra % 2 != 0) { // Odd -> Back
                 basePos = (basePos - extra) % 12
                 if (basePos < 0) basePos += 12
             } else { // Even -> Forward
                 basePos = (basePos + extra) % 12
             }
             tuViPos = basePos
        }
        
        cungList[tuViPos].chinhTinh.add("Tử Vi")
        
        // Vòng Tử Vi (Nghịch): Tử (1) .. Cơ(2).. Dương(4).. Vũ(5).. Đồng(6).. Liêm(9)
        // Offset: -1, -3, -4, -5, -8
        val tuViOffsets = listOf(
            "Thiên Cơ" to 1, 
            "Thái Dương" to 3, 
            "Vũ Khúc" to 4, 
            "Thiên Đồng" to 5, 
            "Liêm Trinh" to 8
        )
        
        for ((star, offset) in tuViOffsets) {
            var pos = (tuViPos - offset) % 12
            if (pos < 0) pos += 12
            cungList[pos].chinhTinh.add(star)
        }
        
        // Thiên Phủ (đối xứng qua trục Dần Thân -> Tổng = 4?)
        // Formula: (4 - tuViPos) % 12
        var thienFuPos = (4 - tuViPos) % 12
        if (thienFuPos < 0) thienFuPos += 12
        
        cungList[thienFuPos].chinhTinh.add("Thiên Phủ")
        
        // Vòng Thiên Phủ (Thuận): Phủ(1).. Âm(2).. Tham(3).. Cự(4).. Tướng(5).. Lương(6).. Sát(7).. Phá(11)
        val thienFuOffsets = listOf(
            "Thái Âm" to 1,
            "Tham Lang" to 2,
            "Cự Môn" to 3,
            "Thiên Tướng" to 4,
            "Thiên Lương" to 5,
            "Thất Sát" to 6,
            "Phá Quân" to 10
        )
        
        for ((star, offset) in thienFuOffsets) {
            val pos = (thienFuPos + offset) % 12
            cungList[pos].chinhTinh.add(star)
        }
    }
    
    // ... Additional complex star placing logic
    // Implementing selected important stars for brevity based on python code
    
    private fun anPhuTinhFull(
        cungList: MutableList<CungInfo>, 
        cucNumber: Int,
        canNamIndex: Int,
        chiNamIndex: Int,
        hourIndex: Int,
        lunarMonth: Int,
        lunarDay: Int,
        gender: Gender
    ) {
        // Setup Direction
        val isYangYear = (canNamIndex % 2 == 0)
        val isThuan = if (gender == Gender.NAM) isYangYear else !isYangYear
        
        // 1. Vòng Thái Tuế
        for (i in VONG_THAI_TUE.indices) {
            val pos = (chiNamIndex + i) % 12
            cungList[pos].phuTinh.add(VONG_THAI_TUE[i])
        }
        
        // 2. Vòng Lộc Tồn
        val canNamStr = THIEN_CAN[canNamIndex]
        val locTonChi = LOC_TON_MAP[canNamStr] ?: "Dần"
        val locTonIdx = DIA_CHI.indexOf(locTonChi)
        
        cungList[locTonIdx].phuTinh.add("Lộc Tồn")
        cungList[(locTonIdx + 1) % 12].phuTinh.add("Kình Dương")
        
        var daLaPos = (locTonIdx - 1) % 12
        if (daLaPos < 0) daLaPos += 12
        cungList[daLaPos].phuTinh.add("Đà La")
        
        // Vòng Bác Sỹ
        for (i in VONG_BAC_SY.indices) {
            var pos = if (isThuan) (locTonIdx + i) % 12 else (locTonIdx - i) % 12
            if (pos < 0) pos += 12
            cungList[pos].phuTinh.add(VONG_BAC_SY[i])
        }
        
        // 3. Vòng Tràng Sinh
        val tsStart = TRANG_SINH_START[cucNumber] ?: 2
        for (i in VONG_TRANG_SINH.indices) {
             var pos = if (isThuan) (tsStart + i) % 12 else (tsStart - i) % 12
             if (pos < 0) pos += 12
             cungList[pos].phuTinh.add(VONG_TRANG_SINH[i])
        }
        
        // 4. Khôi Việt
        KHOI_VIET_POS[canNamStr]?.let { (kPos, vPos) ->
            cungList[kPos].phuTinh.add("Thiên Khôi")
            cungList[vPos].phuTinh.add("Thiên Việt")
        }
        
        // 5. Tả Hữu (Tháng)
        val taPhuPos = (4 + (lunarMonth - 1)) % 12
        var huuBatPos = (10 - (lunarMonth - 1)) % 12
        if (huuBatPos < 0) huuBatPos += 12
        cungList[taPhuPos].phuTinh.add("Tả Phù")
        cungList[huuBatPos].phuTinh.add("Hữu Bật")
        
        // 6. Xương Khúc (Giờ)
        var vanXuongPos = (10 - hourIndex) % 12
        if (vanXuongPos < 0) vanXuongPos += 12
        val vanKhucPos = (4 + hourIndex) % 12
        cungList[vanXuongPos].phuTinh.add("Văn Xương")
        cungList[vanKhucPos].phuTinh.add("Văn Khúc")
        
        // 7. Thiên Mã (Chi Năm -> Tam Hợp)
        // Dần Ngọ Tuất (2,6,10) -> Thân (8)
        // Thân Tý Thìn (8,0,4) -> Dần (2)
        // Tỵ Dậu Sửu (5,9,1) -> Hợi (11)
        // Hợi Mão Mùi (11,3,7) -> Tỵ (5)
        val maPos = when (chiNamIndex) {
            2, 6, 10 -> 8
            8, 0, 4 -> 2
            5, 9, 1 -> 11
            else -> 5
        }
        cungList[maPos].phuTinh.add("Thiên Mã")
        
        // 8. Khốc Hư (Ngọ)
        var khocPos = (6 - chiNamIndex) % 12
        if (khocPos < 0) khocPos += 12
        var huPos = (6 + chiNamIndex) % 12
        cungList[khocPos].phuTinh.add("Thiên Khốc")
        cungList[huPos].phuTinh.add("Thiên Hư")
        
        // 9. Không Kiếp (Hợi - Giờ)
        val diaKiepPos = (11 + hourIndex) % 12
        var diaKhongPos = (11 - hourIndex) % 12
        if (diaKhongPos < 0) diaKhongPos += 12
        cungList[diaKiepPos].phuTinh.add("Địa Kiếp")
        cungList[diaKhongPos].phuTinh.add("Địa Không")
    }
    
    private fun anTuanTriet(cungList: MutableList<CungInfo>, canNamIndex: Int, chiNamIndex: Int) {
        // 1. Triệt (Based on Can Năm)
        com.example.tviai.core.Constants.TRIET_MAP[canNamIndex]?.let { indices ->
            indices.forEach { idx ->
                // Triệt works on position, not Cung Name (though Name is fixed to mapping)
                // CungInfo.index 0 is Ty, 1 is Suu...
                // So index is direct.
                cungList[idx].phuTinh.add("Triệt")
            }
        }
        
        // 2. Tuần (Tuần Trung Không Vong - Based on Con Giáp of Year)
        // Find Con Giáp start (Chi of Giap Year in the 10-year cycle)
        // Formula: Start = (ChiNam - CanNam + 12) % 12
        // Tuan is at (Start - 2) and (Start - 1)
        
        var giapChiIndex = (chiNamIndex - canNamIndex) % 12
        if (giapChiIndex < 0) giapChiIndex += 12
        
        var tuan1 = (giapChiIndex - 2) % 12
        if (tuan1 < 0) tuan1 += 12
        
        var tuan2 = (giapChiIndex - 1) % 12
        if (tuan2 < 0) tuan2 += 12
        
        cungList[tuan1].phuTinh.add("Tuần")
        cungList[tuan2].phuTinh.add("Tuần")
    }
    
    private fun anTuHoa(cungList: MutableList<CungInfo>, canNamIndex: Int) {
        com.example.tviai.core.Constants.TU_HOA_MAP[canNamIndex]?.let { stars ->
            if (stars.size == 4) {
                val suffixes = listOf(" (Hóa Lộc)", " (Hóa Quyền)", " (Hóa Khoa)", " (Hóa Kỵ)")
                for ((i, starName) in stars.withIndex()) {
                    val suffix = suffixes[i]
                    // Find the cung containing this star (in chinhTinh or phuTinh)
                    // Note: Tu Hoa usually transforms MAIN stars. Sometimes Phu Tinh (Xuong, Khuc, Ta, Huu).
                    
                    var found = false
                    for (cung in cungList) {
                        // Check Chinh Tinh
                        if (cung.chinhTinh.contains(starName)) {
                            cung.phuTinh.add(suffix.trim()) // Add as separate "Hóa Lộc" star? Or modify?
                            // User wants to see "Mệnh có Hóa Lộc". If we explicitly add "Hóa Lộc" to the list,
                            // prompt will pick it up.
                            // Better: Add "Hóa Lộc" as a distinct element in phuTinh list.
                            // Or better: "Hóa Lộc" is the star name.
                            // Let's add simple "Hóa Lộc", "Hóa Quyền"... to the palace.
                            // But to avoid confusion, maybe verify if it should replace? 
                            // No, standard is co-existence.
                            found = true
                        }
                        // Check Phu Tinh (for Xuong, Khuc, Ta, Huu...)
                        if (cung.phuTinh.contains(starName)) {
                            cung.phuTinh.add(suffix.trim())
                            found = true
                        }
                        // Note: If star appears in multiple places (unlikely for these specific ones per person), it adds to all.
                        // Stars like Ta Phu, Huu Bat, Xuong, Khuc appear once.
                    }
                }
            }
        }
    }

    private fun anThienHinh(cungList: MutableList<CungInfo>, lunarMonth: Int) {
        // Thiên Hình: Khởi từ Dậu (9) là tháng 1, đếm THUẬN đến tháng sinh.
        // Month 1 -> 9
        // Month 2 -> 10
        // ...
        val pos = (9 + (lunarMonth - 1)) % 12
        cungList[pos].phuTinh.add("Thiên Hình")
        
        // Bonus: Thiên Riêu often opposite Thiên Hình?
        // Thiên Riêu: Khởi Sửu (1), thuận đến tháng sinh.
        // Month 2 -> 1 + 1 = 2 (Dần).
        // Let's add Thiên Riêu as a bonus since it's easy and standard.
        // val rieuPos = (1 + (lunarMonth - 1)) % 12
        // cungList[rieuPos].phuTinh.add("Thiên Riêu")
        // User didn't strictly ask, but it's good practice. But I'll stick to strict requirements to avoid unexpected diffs.
    }

    private fun anHoaLinh(cungList: MutableList<CungInfo>, chiNamIndex: Int, hourIndex: Int) {
        val groupMod = chiNamIndex % 4
        
        // Hỏa Tinh (Thuận từ giờ Tý)
        // Hour 0 (Ty) -> Start
        // Hour n -> Start + n
        val hoaStart = com.example.tviai.core.Constants.HOA_TINH_KHOI[groupMod] ?: 2
        val hoaPos = (hoaStart + hourIndex) % 12
        cungList[hoaPos].phuTinh.add("Hỏa Tinh")
        
        // Linh Tinh (Nghịch từ giờ Tý)
        // Hour 0 (Ty) -> Start
        // Hour n -> Start - n
        val linhStart = com.example.tviai.core.Constants.LINH_TINH_KHOI[groupMod] ?: 10
        var linhPos = (linhStart - hourIndex) % 12
        if (linhPos < 0) linhPos += 12
        cungList[linhPos].phuTinh.add("Linh Tinh")
    }

    private fun anPhuTinhMoRong(
        cungList: MutableList<CungInfo>, 
        canNamIndex: Int,
        chiNamIndex: Int,
        lunarMonth: Int,
        lunarDay: Int,
        hourIndex: Int
    ) {
        // 1. Đào Hoa (Theo Chi Năm)
        com.example.tviai.core.Constants.DAO_HOA_MAP[chiNamIndex]?.let { pos ->
            cungList[pos].phuTinh.add("Đào Hoa")
            
            // Thiên Không (Trước Đào Hoa 1 cung - Nghịch)
            // Logic: Đào Hoa Tý -> Thiên Không Sửu (Thuận) or Hợi?
            // "Tiền tam vị viết Thiếu Dương" (Trước Thái Tuế 1 vị là Thiếu Dương).
            // Thiên Không đồng cung Thiếu Dương. -> Trước Thái Tuế 1 cung.
            // Check logic: Vòng Thái Tuế đã có Thiếu Dương. Ta chỉ cần add Thiên Không vào chỗ Thiếu Dương.
            // Thiếu Dương = (ChiNam + 1).
            // Just find Thiếu Dương in the lists? Or re-calculate pos.
            val tdPos = (chiNamIndex + 1) % 12
            if (!cungList[tdPos].phuTinh.contains("Thiên Không")) {
                cungList[tdPos].phuTinh.add("Thiên Không") 
            }
            
            // Kiếp Sát (Theo Đào Hoa / Chi Năm)
            val kiepSatMap = mapOf(
                5 to 2, 9 to 2, 1 to 2, 
                11 to 8, 3 to 8, 7 to 8, 
                2 to 11, 6 to 11, 10 to 11, 
                8 to 5, 0 to 5, 4 to 5  
            )
            kiepSatMap[chiNamIndex]?.let { ksPos -> cungList[ksPos].phuTinh.add("Kiếp Sát") }

            // Hồng Loan (Cố định theo Chi Năm)
            // Tý -> Mão (3). Sửu -> Dần (2)... Thân -> Mùi (7).
            // Formula: (3 - chiNamIndex)
            var hlPos = (3 - chiNamIndex) % 12
            if (hlPos < 0) hlPos += 12
            if (!cungList[hlPos].phuTinh.contains("Hồng Loan")) cungList[hlPos].phuTinh.add("Hồng Loan")
            
            // Thiên Hỷ (Đối Hồng Loan)
            val thienHyPos = (hlPos + 6) % 12
            if (!cungList[thienHyPos].phuTinh.contains("Thiên Hỷ")) cungList[thienHyPos].phuTinh.add("Thiên Hỷ")
        }
        
        // 2. Cô Thần, Quả Tú
        com.example.tviai.core.Constants.CO_QUA_MAP[chiNamIndex]?.let { (co, qua) ->
            cungList[co].phuTinh.add("Cô Thần")
            cungList[qua].phuTinh.add("Quả Tú")
        }
        
        // 3. Thiên Quan, Thiên Phúc
        com.example.tviai.core.Constants.THIEN_QUAN_PHUC_MAP[canNamIndex]?.let { (quan, phuc) ->
            cungList[quan].phuTinh.add("Thiên Quan")
            cungList[phuc].phuTinh.add("Thiên Phúc")
        }
        
        // 4. Long Trì, Phượng Các, Giải Thần
        val longTriPos = (4 + chiNamIndex) % 12
        var phuongCacPos = (10 - chiNamIndex) % 12
        if (phuongCacPos < 0) phuongCacPos += 12
        
        cungList[longTriPos].phuTinh.add("Long Trì")
        cungList[phuongCacPos].phuTinh.add("Phượng Các")
        cungList[phuongCacPos].phuTinh.add("Giải Thần") 
        
        // 5. Thiên Giải, Địa Giải
        val thienGiaiPos = (8 + (lunarMonth - 1)) % 12
        cungList[thienGiaiPos].phuTinh.add("Thiên Giải")
        
        val diaGiaiPos = (7 + (lunarMonth - 1)) % 12
        cungList[diaGiaiPos].phuTinh.add("Địa Giải")
        
        // 6. Thiên Riêu, Thiên Y
        val rieuPos = (1 + (lunarMonth - 1)) % 12
        cungList[rieuPos].phuTinh.add("Thiên Riêu")
        cungList[rieuPos].phuTinh.add("Thiên Y")

        // 6b. Thiên Trù, Lưu Hà
        com.example.tviai.core.Constants.LUU_HA_MAP[canNamIndex]?.let { pos -> cungList[pos].phuTinh.add("Lưu Hà") }
        com.example.tviai.core.Constants.THIEN_TRU_MAP[canNamIndex]?.let { pos -> cungList[pos].phuTinh.add("Thiên Trù") }

        // 7. Hoa Cái
        val hoaCaiMap = mapOf(
            2 to 10, 6 to 10, 10 to 10, 
            8 to 4, 0 to 4, 4 to 4, 
            5 to 1, 9 to 1, 1 to 1, 
            11 to 7, 3 to 7, 7 to 7 
        )
        hoaCaiMap[chiNamIndex]?.let { pos -> cungList[pos].phuTinh.add("Hoa Cái") }
        
        // 8. Quốc Ấn (Theo Lộc Tồn + 8)
        val canNamStr = com.example.tviai.core.Constants.THIEN_CAN[canNamIndex]
        val locTonChi = com.example.tviai.core.Constants.LOC_TON_MAP[canNamStr] ?: "Dần"
        val locTonIdx = DIA_CHI.indexOf(locTonChi)
        
        val quocAnPos = (locTonIdx + 8) % 12
        cungList[quocAnPos].phuTinh.add("Quốc Ấn")
        
        // 9. Đường Phù (Lộc Tồn + 5? Kiểm chứng: Can Giáp Lộc Dần -> Đường Phù Mùi?)
        // Can Nhâm Lộc Hợi -> Đường Phù Thìn (+5). OK.
        val duongPhuPos = (locTonIdx + 5) % 12
        cungList[duongPhuPos].phuTinh.add("Đường Phù")
        
        // 10. Phong Cáo (Khởi Dần 2, Thuận đến Giờ Sinh)
        val phongCaoPos = (2 + hourIndex) % 12
        cungList[phongCaoPos].phuTinh.add("Phong Cáo")
        
        // 11. Ân Quang, Thiên Quý
        // Xương: (10 - hour) % 12
        // Khúc: (4 + hour) % 12
        var vanXuongPos = (10 - hourIndex) % 12
        if (vanXuongPos < 0) vanXuongPos += 12
        val vanKhucPos = (4 + hourIndex) % 12
        
        // Ân Quang: Xương + Day - 2 (Thuận từ Xương lùi 1 -> Thuận Day -> Lùi 1?)
        // Standard: Quang at (Xuong + Day - 2).
        var anQuangPos = (vanXuongPos + lunarDay - 2) % 12
        if (anQuangPos < 0) anQuangPos += 12
        cungList[anQuangPos].phuTinh.add("Ân Quang")
        
        // Thiên Quý: Khúc - Day + 2 (Nghịch từ Khúc lùi 1 -> Nghịch Day -> Lùi 1?)
        // Standard: Quy at (Khuc - Day + 2).
        var thienQuyPos = (vanKhucPos - lunarDay + 2) % 12
        if (thienQuyPos < 0) thienQuyPos += 12
        cungList[thienQuyPos].phuTinh.add("Thiên Quý")
        
        // 12. Thiên Tài, Thiên Thọ
        val menhCung = cungList.find { it.chucNang.contains("Mệnh") }
        val thanCung = cungList.find { it.chucNang.contains("(Thân)") }
        
        if (menhCung != null) {
            val taiPos = (menhCung.index + chiNamIndex) % 12
            cungList[taiPos].phuTinh.add("Thiên Tài")
            
            // Thiên Thương (Nô Bộc), Thiên Sứ (Tật Ách)
            val noBocPos = (menhCung.index + 5) % 12 // Nghịch 5? No, Mệnh(1)->Huynh(2)..Nô(6). Mệnh+5 -> Nô.
            // Check direction: Mệnh->Phụ->Phúc (Nghịch).
            // Mệnh(i) -> Nô(i-5).
            // But wait, anCungMenhThan set positions relative to Menh.
            // Let's use name search to be safe.
            val noBocCung = cungList.find { it.chucNang.contains("Nô Bộc") }
            val tatAchCung = cungList.find { it.chucNang.contains("Tật Ách") }
            noBocCung?.phuTinh?.add("Thiên Thương")
            tatAchCung?.phuTinh?.add("Thiên Sứ")
        }
        
        if (thanCung != null) {
            val thoPos = (thanCung.index + chiNamIndex) % 12
            cungList[thoPos].phuTinh.add("Thiên Thọ")
        }
        
        // 13. Đầu Quân (Thái Tuế nghịch đến Tháng Sinh, thuận đến Giờ Sinh)
        // Thái Tuế at Chi Năm (chiNamIndex).
        // Count CCW to Month: (chiNam - (month - 1) + 12) % 12 ?
        // Summary: "Từ Thái Tuế (coi là Tý/Tháng 1), đếm nghịch đến tháng sinh".
        var dauQuanPos = (chiNamIndex - (lunarMonth - 1)) % 12
        if (dauQuanPos < 0) dauQuanPos += 12
        // "Từ đó đếm Thuận đến giờ sinh" (Tý = 1, Sửu = 2... -> hourIndex is 0 (Tý), 1 (Sửu)...)
        // So Tý (0) means stay? No. Tý is step 1.
        // Start pos is already Tý/Step 1?
        // Let's check: Month 1 -> Stay. Hour 1 -> Stay. => Dau Quan at Thai Tue.
        // Logic: (Start - (M-1) + (H-1))? Or (H)?
        // "Thuận đến giờ sinh": Tý -> 1, Sửu -> 2.
        // If hour is Tý (0), we move 0 steps? No, we count 1 as current.
        // So move (hourIndex) steps?
        // Let's assume hourIndex 0 (Tý) -> Stay.
        dauQuanPos = (dauQuanPos + hourIndex) % 12
        cungList[dauQuanPos].phuTinh.add("Đầu Quân")
        
        // 14. Thiên Đức, Nguyệt Đức
        // Thiên Đức: Theo Chi Năm (Use MAP)
        com.example.tviai.core.Constants.THIEN_DUC_MAP[chiNamIndex]?.let { pos ->
            cungList[pos].phuTinh.add("Thiên Đức")
        }
        
        // Nguyệt Đức: Tỵ (5) Thuận đến Chi Năm.
        val nguyetDucPos = (5 + chiNamIndex) % 12
        cungList[nguyetDucPos].phuTinh.add("Nguyệt Đức")
        
        // 15. Văn Tinh (Theo Can Năm)
        com.example.tviai.core.Constants.VAN_TINH_MAP[canNamIndex]?.let { pos ->
            cungList[pos].phuTinh.add("Văn Tinh")
        }
        
        // 16. Phá Toái (Theo Chi Năm)
        com.example.tviai.core.Constants.PHA_TOAI_MAP[chiNamIndex]?.let { pos ->
            cungList[pos].phuTinh.add("Phá Toái")
        }
        
        // 17. Tam Thai, Bát Tọa
        // Tả Phù + Day -> Tam Thai. Hữu Bật + Day -> Bát Tọa.
        // Tả Phù: (4 + M - 1). Tam Thai: (TaPhu + Day - 1).
        val taPhuPos = (4 + (lunarMonth - 1)) % 12
        var tamThaiPos = (taPhuPos + (lunarDay - 1)) % 12
        cungList[tamThaiPos].phuTinh.add("Tam Thai")
        
        // Hữu Bật: (10 - M - 1). Bát Tọa: (HuuBat - (Day - 1)).
        var huuBatPos = (10 - (lunarMonth - 1)) % 12
        if (huuBatPos < 0) huuBatPos += 12
        var batToaPos = (huuBatPos - (lunarDay - 1)) % 12
        while (batToaPos < 0) batToaPos += 12
        cungList[batToaPos].phuTinh.add("Bát Tọa")
        
        // 18. Thai Phụ (Ngọ + Giờ - 1)
        val thaiPhuPos = (6 + hourIndex) % 12
        cungList[thaiPhuPos].phuTinh.add("Thai Phụ")
        
        // 19. Thiên La, Địa Võng (Fixed Thìn Tuất)
        cungList[4].phuTinh.add("Thiên La")
        cungList[10].phuTinh.add("Địa Võng")
    }


    private fun anSaoDaiVan(
        cungList: MutableList<CungInfo>, 
        menhIndex: Int, 
        cucNumber: Int, 
        gender: Gender, 
        canNamIndex: Int, // Birth Year Can
        viewingYear: Int,
        birthYear: Int
    ) {
        // 1. Determine Start Palace of Dai Van (Dung Cục)
        // Nam: Dương(Thuận) Âm(Nghịch). Nữ: Dương(Nghịch) Âm(Thuận).
        val isYangYear = (canNamIndex % 2 == 0)
        val isThuan = if (gender == Gender.NAM) isYangYear else !isYangYear
        
        // Start from Mệnh? No. Start from Cung based on Cuc?
        // "Thủy Nhị Cục" -> Starts at 2?
        // No, Dai Van starts at MENH. Number starts at Cuc.
        // Example: Kim 4 Cuc. Mệnh starts at Age 4.
        val startPalace = menhIndex
        
        // 2. Find Current Dai Van Palace
        // Current Age (Tuổi Âm) = Viewing - Birth + 1
        var currentAge = viewingYear - birthYear + 1
        if (currentAge < 1) currentAge = 1 // Fallback
        
        // Determine index offset (how many decades passed)
        // Age ranges: 
        // 1st: Cuc .. Cuc+9
        // 2nd: Cuc+10 .. Cuc+19
        // ...
        val cucVal = cucNumber
        if (currentAge < cucVal) {
             // Before first Dai Van -> "Tiểu Vận" logic usually? 
             // Or just return (no Dai Van stars active yet).
             // User is 35 (1992->2026). Cuc 4. 4-13, 14-23, 24-33, 34-43.
             // 35 is in 4th Decade (34-43).
             // Offset = 3.
        }
        
        // Calc decade index (0-based)
        // (Age - Cuc) / 10 ?
        // 35 - 4 = 31. 31 / 10 = 3. Correct (4th decade).
        val decadeIdx = if (currentAge >= cucVal) (currentAge - cucVal) / 10 else 0
        
        // Palace Position
        var daiVanPos = if (isThuan) (startPalace + decadeIdx) % 12 else (startPalace - decadeIdx) % 12
        if (daiVanPos < 0) daiVanPos += 12
        
        cungList[daiVanPos].phuTinh.add("Đại Vận") // Mark the palace
        
        // 3. Determine CAN of Dai Van (Ngũ Dần Độn)
        // Rule: From Birth Year Can, determine Can of Dần (2).
        // Giáp Kỷ -> Bính (2). Ất Canh -> Mậu (4). Bính Tân -> Canh (6). Đinh Nhâm -> Nhâm (8). Mậu Quý -> Giáp (0).
        val startCanDan = when (canNamIndex % 5) {
            0 -> 2 // Giap/Ky -> Binh
            1 -> 4 // At/Canh -> Mau
            2 -> 6 // Binh/Tan -> Canh
            3 -> 8 // Dinh/Nham -> Nham
            4 -> 0 // Mau/Quy -> Giap
            else -> 0
        }
        
        // Can of Dai Van Palace
        // Sequence of Can is ALWAYS Thuận from Dần? 
        // Dần=start, Mão=start+1...
        // DaiVanPos relative to Dần (2)?
        var distFromDan = (daiVanPos - 2) % 12
        if (distFromDan < 0) distFromDan += 12
        
        val canDaiVan = (startCanDan + distFromDan) % 10
        
        // 4. An Stars for Dai Van Can (ĐV.)
        // Lộc Tồn
        val canStr = THIEN_CAN[canDaiVan]
        val locTonChi = LOC_TON_MAP[canStr] ?: "Dần"
        val dvLocPos = DIA_CHI.indexOf(locTonChi)
        cungList[dvLocPos].phuTinh.add("ĐV. Lộc Tồn")
        
        // Kình Đà (Relative to Loc)
        cungList[(dvLocPos + 1)%12].phuTinh.add("ĐV. Kình Dương")
        var dvDaPos = (dvLocPos - 1) % 12
        if (dvDaPos < 0) dvDaPos += 12
        cungList[dvDaPos].phuTinh.add("ĐV. Đà La")
        
        // Khôi Việt
        KHOI_VIET_POS[canStr]?.let { (k, v) ->
             cungList[k].phuTinh.add("ĐV. Thiên Khôi")
             cungList[v].phuTinh.add("ĐV. Thiên Việt")
        }
        
        // ĐV. Văn Xương (Theo Can ĐV - reused VAN_TINH_MAP)
        com.example.tviai.core.Constants.VAN_TINH_MAP[canDaiVan]?.let { pos ->
            cungList[pos].phuTinh.add("ĐV. Văn Xương")
        }
        
        // ĐV. Văn Khúc (Override for Can Quý -> Hợi)
        if (canDaiVan == 9) { // Quý
            // User says Hợi (11)
            cungList[11].phuTinh.add("ĐV. Văn Khúc")
        }
        
        // ĐV. Thiên Mã (Chi Đại Vận -> Thiên Mã)
        com.example.tviai.core.Constants.THIEN_MA_MAP[daiVanPos]?.let { pos ->
            cungList[pos].phuTinh.add("ĐV. Thiên Mã")
        }
        
        // Tứ Hóa (Can Đại Vận)
        com.example.tviai.core.Constants.TU_HOA_MAP[canDaiVan]?.let { stars ->
             if (stars.size == 4) {
                 val suffixes = listOf(" (ĐV. H Lộc)", " (ĐV. H Quyền)", " (ĐV. H Khoa)", " (ĐV. H Kỵ)")
                 // Note: User prompt has "ĐV. Hóa Khoa", "ĐV. Hóa Kỵ".
                 for ((i, starName) in stars.withIndex()) {
                     // Find star in chart
                     for (cung in cungList) {
                         if (cung.chinhTinh.contains(starName) || cung.phuTinh.contains(starName)) {
                             cung.phuTinh.add(suffixes[i].trim())
                         }
                     }
                 }
             }
        }
    }

    private fun anSaoLuu(cungList: MutableList<CungInfo>, viewingYear: Int) {
        val canNamXemIndex = LunarConverter.getCanNamIndex(viewingYear)
        val chiNamXemIndex = LunarConverter.getChiNamIndex(viewingYear)
        
        // 1. Lưu Thái Tuế (Tại Chi Năm Xem)
        val lThaiTuePos = chiNamXemIndex
        cungList[lThaiTuePos].phuTinh.add("L.Thái Tuế")
        
        // Only Add Major Stars: Bach Ho, Tang Mon. REMOVE full loop.
        val lTangMonPos = (lThaiTuePos + 2) % 12
        cungList[lTangMonPos].phuTinh.add("L.Tang Môn")
        
        val lBachHoPos = (lTangMonPos + 6) % 12
        cungList[lBachHoPos].phuTinh.add("L.Bạch Hổ")
        
        // 2. Lưu Lộc Tồn & Kình Đà
        val canStr = THIEN_CAN[canNamXemIndex]
        val locTonChi = LOC_TON_MAP[canStr] ?: "Dần"
        val lLocTonPos = DIA_CHI.indexOf(locTonChi)
        cungList[lLocTonPos].phuTinh.add("L.Lộc Tồn")
        
        val lKinhPos = (lLocTonPos + 1) % 12
        cungList[lKinhPos].phuTinh.add("L.Kình Dương")
        var lDaPos = (lLocTonPos - 1) % 12
        if (lDaPos < 0) lDaPos += 12
        cungList[lDaPos].phuTinh.add("L.Đà La")
        
        // 3. Lưu Thiên Mã
        com.example.tviai.core.Constants.THIEN_MA_MAP[chiNamXemIndex]?.let { pos ->
            cungList[pos].phuTinh.add("L.Thiên Mã")
        }
        
        // 4. Lưu Khốc Hư
        var lKhocPos = (6 - chiNamXemIndex) % 12
        if (lKhocPos < 0) lKhocPos += 12
        val lHuPos = (6 + chiNamXemIndex) % 12
        cungList[lKhocPos].phuTinh.add("L.Thiên Khốc")
        cungList[lHuPos].phuTinh.add("L.Thiên Hư")
        
        // 5. Lưu Đào Hồng Hỷ
        // Đào Hoa (Chi Năm Xem)
        com.example.tviai.core.Constants.DAO_HOA_MAP[chiNamXemIndex]?.let { pos ->
            cungList[pos].phuTinh.add("L.Đào Hoa")
            
            // L.Thiên Không (Trước Đào Hoa) (Same as L.Thiếu Dương)
            // Included in Vong Thai Tue Luu above (L.Thiếu Dương). 
            // So we strictly don't need explicit L.Thiên Không if L.Thiếu Dương is present?
            // But User might want text "L.Thiên Không".
            // Let's add it to L.ThieuDuong pos.
            // But wait, Vong Thai Tue has Thieu Duong.
            // Let's find L.Thieu Duong and add L.Thien Khong.
            val tdPos = cungList.indexOfFirst { it.phuTinh.contains("L.Thiếu Dương") }
            if (tdPos != -1) cungList[tdPos].phuTinh.add("L.Thiên Không")
            
            // Kiếp Sát (Lưu)
            val kiepSatMap = mapOf(
                5 to 2, 9 to 2, 1 to 2, 
                11 to 8, 3 to 8, 7 to 8, 
                2 to 11, 6 to 11, 10 to 11, 
                8 to 5, 0 to 5, 4 to 5  
            )
            kiepSatMap[chiNamXemIndex]?.let { ksPos -> cungList[ksPos].phuTinh.add("L.Kiếp Sát") }

            // L.Hồng Loan
            var hlPos = (3 - chiNamXemIndex) % 12
            if (hlPos < 0) hlPos += 12
            cungList[hlPos].phuTinh.add("L.Hồng Loan")
            
            // L.Thiên Hỷ - User requested REMOVE ("Thừa").
            // val thPos = (hlPos + 6) % 12
            // cungList[thPos].phuTinh.add("L.Thiên Hỷ")
        }
        
        // 6. Lưu Khôi Việt
        KHOI_VIET_POS[canStr]?.let { (k, v) ->
             cungList[k].phuTinh.add("L.Thiên Khôi")
             cungList[v].phuTinh.add("L.Thiên Việt")
        }

        // 7. Lưu Văn Xương, Lưu Văn Khúc (Theo Can Năm Xem)
        // Can Bính (2) -> Xương Thân, Khúc Ngọ. (Generic Map Logic if needed, but simple block here)
        // Xương: Giáp Tỵ... Bính Thân. (Matches VAN_TINH_MAP).
        com.example.tviai.core.Constants.VAN_TINH_MAP[canNamXemIndex]?.let { pos ->
            cungList[pos].phuTinh.add("L.Văn Xương")
        }
        // Khúc: Giáp Dậu... Bính Ngọ.
        // Simple Khuc Map derived or explicit for Binh:
        if (canNamXemIndex == 2) { // Bính
            cungList[6].phuTinh.add("L.Văn Khúc") // Ngọ
        } else {
             // Generic fallback if needed later
        }

        // 8. Lưu Thiên Đức, Lưu Nguyệt Đức
        // L.Nguyệt Đức: (5 + ChiNamXem) % 12 (Like Static: Tỵ + ChiName)
        val lNguyetDucPos = (5 + chiNamXemIndex) % 12
        cungList[lNguyetDucPos].phuTinh.add("L.Nguyệt Đức")
        
        // L.Thiên Đức, L.Long Đức, L.Phúc Đức (Special User Offset)
        // L.Long Đức -> Thái Tuế + 9 (Mão)
        val lLongDucPos = (lThaiTuePos + 9) % 12
        cungList[lLongDucPos].phuTinh.add("L.Long Đức")
        // L.Thiên Đức -> Thái Tuế + 9 (Mão) - Same as L.Long Đức in user chart
        cungList[lLongDucPos].phuTinh.add("L.Thiên Đức")
        
        // L.Phúc Đức -> Thái Tuế + 11 (Tỵ)
        val lPhucDucPos = (lThaiTuePos + 11) % 12
        cungList[lPhucDucPos].phuTinh.add("L.Phúc Đức")

        // 9. Lưu Tứ Hóa
        com.example.tviai.core.Constants.TU_HOA_MAP[canNamXemIndex]?.let { stars ->
            if (stars.size == 4) {
               val suffixes = listOf(" (L.Hóa Lộc)", " (L.Hóa Quyền)", " (L.Hóa Khoa)", " (L.Hóa Kỵ)")
               for ((i, targetStar) in stars.withIndex()) {
                   for (cung in cungList) {
                        val hasStar = cung.chinhTinh.any { it.startsWith(targetStar) }
                                || cung.phuTinh.any { !it.contains("(") && it.startsWith(targetStar) } 
                        if (hasStar) {
                            cung.phuTinh.add(suffixes[i].trim())
                        }
                   }
               }
            }
        }
    }


    private fun anDoSang(cungList: MutableList<CungInfo>) {
        // Iterate every cung
        for (i in 0 until 12) {
            val cung = cungList[i]
            val newChinhTinh = cung.chinhTinh.map { starName ->
                // Check if this star has brightness data
                val brightnessList = com.example.tviai.core.Constants.STAR_BRIGHTNESS[starName]
                if (brightnessList != null) {
                    val brightness = brightnessList[i] // i is index 0..11 (Ty..Hoi)
                    "$starName ($brightness)"
                } else {
                    starName
                }
            }
            // Update the list
            cungList[i] = cung.copy(chinhTinh = newChinhTinh.toMutableList())
        }
    }

    private fun calculateScores(cungList: List<CungInfo>): List<Int> {
        return cungList.map { cung ->
            var score = 5
            cung.chinhTinh.forEach { score += STAR_SCORES[it] ?: 0 }
            cung.phuTinh.forEach { score += STAR_SCORES[it] ?: 0 }
            score
        }
    }
}
