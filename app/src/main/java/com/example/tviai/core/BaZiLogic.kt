package com.example.tviai.core

import com.example.tviai.data.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class BaZiLogic(private val solarTermsJson: String) {

    private val solarTermsData = JSONObject(solarTermsJson)
    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun calculateBaZi(input: UserInput): BaZiData {
        val longitude = input.longitude ?: 105.8
        
        // 1. Calculate True Solar Time (TST)
        // TST = LocalTime + (Longitude - TimezoneMeridian) * 4 minutes
        // For UTC+7, TimezoneMeridian = 7 * 15 = 105
        val longitudeCorrectionMinutes = ((longitude - 105.0) * 4).toInt()
        
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+7")).apply {
            set(input.solarYear, input.solarMonth - 1, input.solarDay, input.hour, 0)
            add(Calendar.MINUTE, longitudeCorrectionMinutes)
        }
        
        val tstYear = calendar.get(Calendar.YEAR)
        val tstMonth = calendar.get(Calendar.MONTH) + 1
        val tstDay = calendar.get(Calendar.DAY_OF_MONTH)
        val tstHour = calendar.get(Calendar.HOUR_OF_DAY)
        val tstMinute = calendar.get(Calendar.MINUTE)

        // 2. Day Boundary (23:00)
        var calcJdn = calculateJDN(tstYear, tstMonth, tstDay)
        if (input.dayBoundaryAt23 && tstHour >= 23) {
            calcJdn += 1
        }
        
        // 3. Year Pillar
        // Based on Lập Xuân (start_of_spring)
        val birthTimeUtc = calendar.timeInMillis
        val lapXuanThisYear = getSolarTerm(tstYear, "start_of_spring")
        
        val yearCalc = if (birthTimeUtc < lapXuanThisYear) tstYear - 1 else tstYear
        val yearCanIdx = (yearCalc - 4 + 100) % 10
        val yearChiIdx = (yearCalc - 4 + 120) % 12
        val yearPillar = createPillar(Constants.THIEN_CAN[yearCanIdx], Constants.DIA_CHI[yearChiIdx])

        // 4. Month Pillar
        val monthChiIdx = calculateMonthChiIndex(tstYear, birthTimeUtc)
        val monthCanIdx = calculateMonthCanIndex(yearCanIdx, monthChiIdx)
        val monthPillar = createPillar(Constants.THIEN_CAN[monthCanIdx], Constants.DIA_CHI[monthChiIdx])

        // 5. Day Pillar
        val dayCanIdx = ((calcJdn + 9) % 10).toInt()
        val dayChiIdx = ((calcJdn + 1) % 12).toInt()
        val dayPillar = createPillar(Constants.THIEN_CAN[dayCanIdx], Constants.DIA_CHI[dayChiIdx])
        val dayMaster = Constants.THIEN_CAN[dayCanIdx]

        // 6. Hour Pillar
        val hourChiIdx = (((tstHour + 1) / 2) % 12) 
        val hourCanIdx = calculateHourCanIndex(dayCanIdx, hourChiIdx)
        val hourPillar = createPillar(Constants.THIEN_CAN[hourCanIdx], Constants.DIA_CHI[hourChiIdx])

        // 7. Ten Gods
        val tenGods = TenGods(
            dayMaster = dayMaster,
            yearStemGod = Constants.calculateTenGod(dayMaster, yearPillar.stem),
            yearBranchGod = "Main: " + Constants.calculateTenGod(dayMaster, yearPillar.hiddenStems.first()),
            monthStemGod = Constants.calculateTenGod(dayMaster, monthPillar.stem),
            monthBranchGod = "Main: " + Constants.calculateTenGod(dayMaster, monthPillar.hiddenStems.first()),
            hourStemGod = Constants.calculateTenGod(dayMaster, hourPillar.stem),
            hourBranchGod = "Main: " + Constants.calculateTenGod(dayMaster, hourPillar.hiddenStems.first())
        )

        // 8. Element Balance
        val elementBalance = calculateElementBalance(listOf(yearPillar, monthPillar, dayPillar, hourPillar))

        val currentTermInfo = getCurrentAndNextTerm(tstYear, birthTimeUtc)

        return BaZiData(
            birthInfo = "TST: $tstYear-$tstMonth-$tstDay $tstHour:$tstMinute (Long: $longitude)",
            year = yearPillar,
            month = monthPillar,
            day = dayPillar,
            hour = hourPillar,
            tenGods = tenGods,
            currentTerm = currentTermInfo.first,
            nextTerm = currentTermInfo.second,
            nextTermTime = currentTermInfo.third,
            elementBalance = elementBalance
        )
    }

    private fun createPillar(can: String, chi: String): Pillar {
        return Pillar(
            stem = can,
            stemYinYang = Constants.CAN_YIN_YANG[can] ?: "",
            stemElement = Constants.NGU_HANH_CAN[can] ?: "",
            branch = chi,
            branchYinYang = Constants.CHI_YIN_YANG[chi] ?: "",
            branchElement = Constants.NGU_HANH_CUNG[chi] ?: "",
            hiddenStems = Constants.TANG_CAN[chi] ?: emptyList()
        )
    }

    private val solarTermNames = mapOf(
            "start_of_spring" to "Lập Xuân",
            "spring_showers" to "Vũ Thủy",
            "awakening_of_insects" to "Kinh Trập",
            "spring_equinox" to "Xuân Phân",
            "pure_brightness" to "Thanh Minh",
            "grain_rain" to "Cốc Vũ",
            "start_of_summer" to "Lập Hạ",
            "grain_buds" to "Tiểu Mãn",
            "grain_in_ear" to "Mang Chủng",
            "summer_solstice" to "Hạ Chí",
            "minor_heat" to "Tiểu Thử",
            "major_heat" to "Đại Thử",
            "start_of_autumn" to "Lập Thu",
            "end_of_heat" to "Xử Thử",
            "white_dew" to "Bạch Lộ",
            "autumn_equinox" to "Thu Phân",
            "cold_dew" to "Hàn Lộ",
            "frost" to "Sương Giáng",
            "start_of_winter" to "Lập Đông",
            "minor_snow" to "Tiểu Tuyết",
            "major_snow" to "Đại Tuyết",
            "winter_solstice" to "Đông Chí",
            "minor_cold" to "Tiểu Hàn",
            "major_cold" to "Đại Hàn"
    )

    private fun calculateMonthChiIndex(year: Int, birthTimeUtc: Long): Int {
        val monthStarts = listOf(
            "minor_cold" to 1,          // Jan (~Jan 5)
            "start_of_spring" to 2,     // Feb (Lập Xuân ~Feb 4)
            "awakening_of_insects" to 3, // Mar (Kinh Trập ~Mar 5)
            "pure_brightness" to 4,     // Apr (Thanh Minh ~Apr 5)
            "start_of_summer" to 5,     // May (Lập Hạ ~May 5)
            "grain_in_ear" to 6,       // Jun (Mang Chủng ~Jun 6)
            "minor_heat" to 7,          // Jul (Tiểu Thử ~Jul 7)
            "start_of_autumn" to 8,     // Aug (Lập Thu ~Aug 8)
            "white_dew" to 9,           // Sep (Bạch Lộ ~Sep 8)
            "cold_dew" to 10,           // Oct (Hàn Lộ ~Oct 8)
            "start_of_winter" to 11,    // Nov (Lập Đông ~Nov 7)
            "major_snow" to 0           // Dec (Đại Tuyết ~Dec 7)
        )
        
        var lastChi = 1
        val checkYears = listOf(year - 1, year)
        for (y in checkYears) {
            for ((term, chi) in monthStarts) {
                if (birthTimeUtc >= getSolarTerm(y, term)) {
                    lastChi = chi
                }
            }
        }
        return lastChi
    }

    private fun calculateMonthCanIndex(yearCanIdx: Int, monthChiIdx: Int): Int {
        val startCan = when (yearCanIdx % 5) {
            0 -> 2 // Giáp/Kỷ -> Bính Dần (2)
            1 -> 4 // Ất/Canh -> Mậu Dần (4)
            2 -> 6 // Bính/Tân -> Canh Dần (6)
            3 -> 8 // Đinh/Nhâm -> Nhâm Dần (8)
            4 -> 0 // Mậu/Quý -> Giáp Dần (0)
            else -> 0
        }
        val diff = (monthChiIdx - 2 + 12) % 12
        return (startCan + diff) % 10
    }

    private fun calculateHourCanIndex(dayCanIdx: Int, hourChiIdx: Int): Int {
        val startCan = when (dayCanIdx % 5) {
            0 -> 0 // Giáp/Kỷ -> Giáp Tý (0)
            1 -> 2 // Ất/Canh -> Bính Tý (2)
            2 -> 4 // Bính/Tân -> Mậu Tý (4)
            3 -> 6 // Đinh/Nhâm -> Canh Tý (6)
            4 -> 8 // Mậu/Quý -> Nhâm Tý (8)
            else -> 0
        }
        return (startCan + hourChiIdx) % 10
    }

    private fun getSolarTerm(year: Int, termKey: String): Long {
        return try {
            val yearStr = year.toString()
            if (solarTermsData.has(yearStr)) {
                val yearData = solarTermsData.getJSONObject(yearStr).getJSONObject("data")
                val dateStr = yearData.getString(termKey).replace("T", " ").take(19)
                sdf.parse(dateStr)?.time ?: 0L
            } else 0L
        } catch (e: Exception) {
            0L
        }
    }

    private fun getCurrentAndNextTerm(year: Int, birthTimeUtc: Long): Triple<String, String, String> {
        val allTerms = mutableListOf<Pair<Long, String>>()
        for (y in (year - 1)..(year + 1)) {
            solarTermNames.keys.forEach { key ->
                allTerms.add(getSolarTerm(y, key) to key)
            }
        }
        allTerms.sortBy { it.first }
        
        var current = "N/A"
        var next = "N/A"
        var nextTime = "N/A"
        
        for (i in 0 until allTerms.size - 1) {
            if (birthTimeUtc >= allTerms[i].first && birthTimeUtc < allTerms[i+1].first) {
                current = solarTermNames[allTerms[i].second] ?: "N/A"
                next = solarTermNames[allTerms[i+1].second] ?: "N/A"
                nextTime = sdf.format(Date(allTerms[i+1].first))
                break
            }
        }
        
        return Triple(current, next, nextTime)
    }

    private fun calculateElementBalance(pillars: List<Pillar>): Map<String, Int> {
        val scores = mutableMapOf("Kim" to 0, "Mộc" to 0, "Thủy" to 0, "Hỏa" to 0, "Thổ" to 0)
        for (p in pillars) {
            scores[p.stemElement] = scores.getOrDefault(p.stemElement, 0) + 40
            val weights = Constants.TANG_CAN_WEIGHT[p.branch] ?: emptyMap()
            for ((can, weight) in weights) {
                val hanh = Constants.NGU_HANH_CAN[can] ?: ""
                if (hanh.isNotEmpty()) {
                    scores[hanh] = (scores[hanh] ?: 0) + (weight * 60 / 100)
                }
            }
        }
        return scores
    }

    private fun calculateJDN(year: Int, month: Int, day: Int): Long {
        var yLong = year.toLong()
        var mLong = month.toLong()
        val dLong = day.toLong()
        val a = (14 - mLong) / 12
        yLong = yLong + 4800 - a
        mLong = mLong + 12 * a - 3
        return dLong + (153 * mLong + 2) / 5 + 365 * yLong + yLong / 4 - yLong / 100 + yLong / 400 - 32045
    }
}
