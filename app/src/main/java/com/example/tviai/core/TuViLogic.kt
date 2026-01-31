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
        val lunarResult = LunarConverter.convertSolarToLunar(solarDay, solarMonth, solarYear)
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
        
        // 5. An Phu Tinh Full
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
            var p = (menhPos - i) % 12
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
    
    private fun calculateScores(cungList: List<CungInfo>): List<Int> {
        return cungList.map { cung ->
            var score = 5
            cung.chinhTinh.forEach { score += STAR_SCORES[it] ?: 0 }
            cung.phuTinh.forEach { score += STAR_SCORES[it] ?: 0 }
            score
        }
    }
}
