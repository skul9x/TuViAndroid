package com.example.tviai.core

import com.example.tviai.core.util.LunarDateUtil
import com.example.tviai.core.Constants.THIEN_CAN
import com.example.tviai.core.Constants.DIA_CHI

class LunarConverter {
    companion object {
        fun convertSolarToLunar(day: Int, month: Int, year: Int): LunarDateUtil.LunarResult {
            return LunarDateUtil.convertSolarToLunar(day, month, year)
        }

        fun getCanChiNam(year: Int): String {
            val can = THIEN_CAN[(year - 4) % 10]
            val chi = DIA_CHI[(year - 4) % 12]
            return "$can $chi"
        }

        fun getChiNamIndex(year: Int): Int {
            return (year - 4) % 12
        }

        fun getCanNamIndex(year: Int): Int {
            return (year - 4) % 10
        }

        fun getCanChiThang(lunarMonth: Int, yearCanIndex: Int): String {
            // Công thức tính Can tháng:
            // Giáp, Kỷ -> Bính Dần (Tháng 1 là Bính)
            // Ất, Canh -> Mậu Dần
            // Bính, Tân -> Canh Dần
            // Đinh, Nhâm -> Nhâm Dần
            // Mậu, Quý -> Giáp Dần
            val startCans = mapOf(
                0 to 2, 5 to 2, // Giáp/Kỷ -> Bính
                1 to 4, 6 to 4, // Ất/Canh -> Mậu
                2 to 6, 7 to 6, // Bính/Tân -> Canh
                3 to 8, 8 to 8, // Đinh/Nhâm -> Nhâm
                4 to 0, 9 to 0  // Mậu/Quý -> Giáp
            )
            
            val startCan = startCans[yearCanIndex] ?: 2
            val currentCanIndex = (startCan + (lunarMonth - 1)) % 10
            val currentChiIndex = (2 + (lunarMonth - 1)) % 12 // Tháng 1 luôn là Dần (index 2)
            
            return "${THIEN_CAN[currentCanIndex]} ${DIA_CHI[currentChiIndex]}"
        }

        fun getChiGio(hour: Int): String {
            return if (hour >= 23 || hour < 1) "Tý"
            else if (hour < 3) "Sửu"
            else if (hour < 5) "Dần"
            else if (hour < 7) "Mão"
            else if (hour < 9) "Thìn"
            else if (hour < 11) "Tỵ"
            else if (hour < 13) "Ngọ"
            else if (hour < 15) "Mùi"
            else if (hour < 17) "Thân"
            else if (hour < 19) "Dậu"
            else if (hour < 21) "Tuất"
            else "Hợi"
        }

        fun getChiGioIndex(hour: Int): Int {
            return if (hour >= 23 || hour < 1) 0
            else if (hour < 3) 1
            else if (hour < 5) 2
            else if (hour < 7) 3
            else if (hour < 9) 4
            else if (hour < 11) 5
            else if (hour < 13) 6
            else if (hour < 15) 7
            else if (hour < 17) 8
            else if (hour < 19) 9
            else if (hour < 21) 10
            else 11
        }

        /**
         * Calculate Julian Day Number from a solar (Gregorian) date.
         * Used as the basis for computing Can Chi of any day.
         */
        private fun julianDayNumber(day: Int, month: Int, year: Int): Long {
            val a = (14 - month) / 12
            val y = year + 4800 - a
            val m = month + 12 * a - 3
            return day.toLong() + (153L * m + 2) / 5 + 365L * y + y / 4 - y / 100 + y / 400 - 32045
        }

        /**
         * Get Can index of a solar date (0=Giáp, 1=Ất, ..., 9=Quý)
         * Based on the 60-day cycle: Can = (JDN + 9) % 10
         */
        fun getCanNgayIndex(day: Int, month: Int, year: Int): Int {
            val jdn = julianDayNumber(day, month, year)
            return ((jdn + 9) % 10).toInt()
        }

        /**
         * Get Chi index of a solar date (0=Tý, 1=Sửu, ..., 11=Hợi)
         * Based on the 60-day cycle: Chi = (JDN + 1) % 12
         */
        fun getChiNgayIndex(day: Int, month: Int, year: Int): Int {
            val jdn = julianDayNumber(day, month, year)
            return ((jdn + 1) % 12).toInt()
        }

        /**
         * Get full Can Chi string of a solar date (e.g. "Giáp Tý")
         */
        fun getCanChiNgay(day: Int, month: Int, year: Int): String {
            return "${THIEN_CAN[getCanNgayIndex(day, month, year)]} ${DIA_CHI[getChiNgayIndex(day, month, year)]}"
        }
    }
}
