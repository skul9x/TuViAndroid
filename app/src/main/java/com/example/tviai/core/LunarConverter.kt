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
                0 to 2, // Giáp -> Bính
                1 to 4, // Ất -> Mậu
                2 to 6, // Bính -> Canh
                3 to 8, // Đinh -> Nhâm
                4 to 0, // Mậu -> Giáp
                5 to 2, // Kỷ -> Bính
                6 to 4, // Canh -> Mậu
                7 to 6, // Tân -> Canh
                8 to 8, // Nhâm -> Nhâm
                9 to 0  // Quý -> Giáp
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
    }
}
