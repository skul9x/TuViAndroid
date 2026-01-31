package com.example.tviai.data

import com.example.tviai.core.Constants

enum class Gender {
    NAM, NU
}

enum class ReadingStyle(val displayName: String) {
    NGHIEM_TUC("Nghiêm túc"),
    DOI_THUONG("Đời thường"),
    HAI_HUOC("Hài hước"),
    KIEM_HIEP("Kiếm hiệp"),
    CHUA_LANH("Chữa lành");

    companion object {
        fun fromString(value: String): ReadingStyle {
            return entries.find { it.displayName == value } ?: NGHIEM_TUC
        }
    }
}

data class UserInput(
    val name: String,
    val solarDay: Int,
    val solarMonth: Int,
    val solarYear: Int,
    val hour: Int, // 0-23
    val gender: Gender,
    val isLunar: Boolean = false,
    val viewingYear: Int,
    val readingStyle: ReadingStyle = ReadingStyle.NGHIEM_TUC
)

data class CungInfo(
    val index: Int = 0,
    val name: String = "",         // Tý, Sửu...
    val chucNang: String = "",     // Mệnh, Phụ Mẫu...
    val chinhTinh: MutableList<String> = mutableListOf(),
    val phuTinh: MutableList<String> = mutableListOf(),
    var score: Int = 0
)

data class UserInfoResult(
    val name: String,
    val gender: String,
    val solarDate: String,
    val time: String,
    val lunarDate: String,
    val canChi: String,
    val cuc: String,
    val menhTai: String,
    val thanTai: String,
    val viewingYear: Int,
    val readingStyle: String
)

data class LasoData(
    val info: UserInfoResult,
    val cung: List<CungInfo>,
    val scores: List<Int>
)
