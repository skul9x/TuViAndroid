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
    CHUA_LANH("Chữa lành"),
    CHUYEN_GIA("Chuyên gia mệnh lý");

    companion object {
        fun fromString(value: String): ReadingStyle {
            return entries.find { it.displayName == value } ?: NGHIEM_TUC
        }
    }
}

enum class ViewingMode(val displayName: String) {
    YEAR("Theo năm"),
    MONTH("Theo tháng")
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
    val viewingMonth: Int = 0,
    val viewingMode: ViewingMode = ViewingMode.YEAR,
    val readingStyle: ReadingStyle = ReadingStyle.NGHIEM_TUC,
    val lunarDayInput: Int? = null,
    val lunarMonthInput: Int? = null,
    val lunarYearInput: Int? = null,
    val longitude: Double? = 105.8, // Mặc định Hà Nội
    val dayBoundaryAt23: Boolean = true // Mặc định đổi ngày lúc 23h
)

data class CungInfo(
    val index: Int = 0,
    val name: String = "",         // Tý, Sửu...
    val chucNang: String = "",     // Mệnh, Phụ Mẫu...
    val nguHanhCung: String = "",  // Ngũ hành cung (Thủy, Thổ, Mộc...)
    val canChi: String = "",       // Can Chi của cung (VD: "Canh Tuất")
    val chinhTinh: MutableList<String> = mutableListOf(),
    val phuTinh: MutableList<String> = mutableListOf(),
    var score: Int = 0
)

data class Pillar(
    val stem: String,
    val stemYinYang: String,
    val stemElement: String,
    val branch: String,
    val branchYinYang: String,
    val branchElement: String,
    val hiddenStems: List<String>
)

data class TenGods(
    val dayMaster: String,
    val yearStemGod: String,
    val yearBranchGod: String,
    val monthStemGod: String,
    val monthBranchGod: String,
    val hourStemGod: String,
    val hourBranchGod: String
)

data class BaZiData(
    val birthInfo: String, // Detail about TST, Longitude
    val year: Pillar,
    val month: Pillar,
    val day: Pillar,
    val hour: Pillar,
    val tenGods: TenGods,
    val currentTerm: String,
    val nextTerm: String,
    val nextTermTime: String,
    val elementBalance: Map<String, Int>
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
    val viewingMonth: Int = 0,
    val viewingMode: String = "YEAR",
    val readingStyle: String,
    val daiVanInfo: String = "",
    val menhNguHanh: String = "",       // VD: "Kiếm Phong Kim"
    val cucMenhRelation: String = "",   // VD: "Mệnh đồng hành Cục"
    val daiVanFullList: String = "",    // Danh sách tất cả đại vận
    val amDuong: String = "",           // VD: "Dương Nam – Thuận hành"
    val tieuHanCung: String = "",       // Cung tiểu hạn năm xem
    val luuNguyetCung: String = "",     // Cung lưu nguyệt tháng xem
    val phiTinhTuHoa: String = "",      // Pre-computed data phi tinh 12 cung (Bản mệnh)
    val phiTinhLuuNguyet: String = "",  // Pre-computed phi tinh lưu nguyệt
    val luuNguyet12Months: String = "", // Chứa mảng JSON 12 tháng lưu nguyệt
    val baZiData: BaZiData? = null      // Dữ liệu Tứ Trụ
)

data class LasoData(
    val info: UserInfoResult,
    val cung: List<CungInfo>,
    val scores: List<Int>
)
