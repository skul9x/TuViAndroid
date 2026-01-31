package com.example.tviai.core

object Constants {
    // Can
    val THIEN_CAN = listOf("Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý")
    
    // Chi
    val DIA_CHI = listOf("Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi")
    val CUNG_DIA_BAN = DIA_CHI

    // 12 Cung chức năng
    val CUNG_CHUC_NANG = listOf(
        "Mệnh", "Phụ Mẫu", "Phúc Đức", "Điền Trạch", "Quan Lộc", "Nô Bộc",
        "Thiên Di", "Tật Ách", "Tài Bạch", "Tử Tức", "Phu Thê", "Huynh Đệ"
    )

    // 14 Chính Tinh
    val CHINH_TINH = listOf(
        "Tử Vi", "Thiên Cơ", "Thái Dương", "Vũ Khúc", "Thiên Đồng", "Liêm Trinh",
        "Thiên Phủ", "Thái Âm", "Tham Lang", "Cự Môn", "Thiên Tướng", "Thiên Lương", "Thất Sát", "Phá Quân"
    )

    // Ngũ Hành Cục (Tên Cục -> Số cục)
    val CUC = mapOf(
        "Thủy" to 2, 
        "Mộc" to 3, 
        "Kim" to 4, 
        "Thổ" to 5, 
        "Hỏa" to 6
    )

    // Vòng Thái Tuế
    val VONG_THAI_TUE = listOf(
        "Thái Tuế", "Thiếu Dương", "Tang Môn", "Thiếu Âm", "Quan Phù", "Tử Phù",
        "Tuế Phá", "Long Đức", "Bạch Hổ", "Phúc Đức", "Điếu Khách", "Trực Phù"
    )

    // Vòng Bác Sỹ
    val VONG_BAC_SY = listOf(
        "Bác Sỹ", "Lực Sỹ", "Thanh Long", "Tiểu Hao", "Tướng Quân", "Tấu Thư",
        "Phi Liêm", "Hỷ Thần", "Bệnh Phù", "Đại Hao", "Phục Binh", "Quan Phủ"
    )

    // Vòng Tràng Sinh
    val VONG_TRANG_SINH = listOf(
        "Tràng Sinh", "Mộc Dục", "Quan Đới", "Lâm Quan", "Đế Vượng", "Suy",
        "Bệnh", "Tử", "Mộ", "Tuyệt", "Thai", "Dưỡng"
    )

    // Mapping Khôi Việt (Can -> (Khôi index, Việt index))
    // Index theo DIA_CHI: Tý=0, Sửu=1...
    val KHOI_VIET_POS = mapOf(
        "Giáp" to Pair(1, 7),  // Sửu, Mùi
        "Mậu" to Pair(1, 7),
        "Ất" to Pair(0, 8),    // Tý, Thân
        "Kỷ" to Pair(0, 8),
        "Bính" to Pair(11, 9), // Hợi, Dậu
        "Đinh" to Pair(11, 9),
        "Canh" to Pair(6, 2),  // Ngọ, Dần
        "Tân" to Pair(6, 2),
        "Nhâm" to Pair(3, 5),  // Mão, Tỵ (User Requested: Khôi Mão, Việt Tỵ)
        "Quý" to Pair(3, 5)    // Consistent with Nhâm for this school
    )



    // Mapping Lộc Tồn (Can -> Chi name)
    val LOC_TON_MAP = mapOf(
        "Giáp" to "Dần", "Ất" to "Mão", "Bính" to "Tỵ", "Đinh" to "Ngọ", "Mậu" to "Tỵ",
        "Kỷ" to "Ngọ", "Canh" to "Thân", "Tân" to "Dậu", "Nhâm" to "Hợi", "Quý" to "Tý"
    )
    
    // Mapping Tràng Sinh (Cục -> Khởi cung index)
    val TRANG_SINH_START = mapOf(
        4 to 5,  // Kim -> Tỵ (5)
        3 to 11, // Mộc -> Hợi (11)
        6 to 2,  // Hỏa -> Dần (2)
        2 to 8,  // Thủy -> Thân (8)
        5 to 8   // Thổ -> Thân (8)
    )

    // Scores
    val STAR_SCORES = mapOf(
        "Tử Vi" to 10, "Thiên Cơ" to 8, "Thái Dương" to 9, "Vũ Khúc" to 8, "Thiên Đồng" to 7, "Liêm Trinh" to 6,
        "Thiên Phủ" to 10, "Thái Âm" to 8, "Tham Lang" to 6, "Cự Môn" to 5, "Thiên Tướng" to 8, "Thiên Lương" to 9, "Thất Sát" to 7, "Phá Quân" to 5,
        "Văn Xương" to 2, "Văn Khúc" to 2, "Tả Phù" to 2, "Hữu Bật" to 2, "Thiên Khôi" to 3, "Thiên Việt" to 3,
        "Lộc Tồn" to 5, "Hóa Lộc" to 5, "Hóa Quyền" to 4, "Hóa Khoa" to 4, "Hóa Kỵ" to -5,
        "Kình Dương" to -3, "Đà La" to -3, "Hỏa Tinh" to -3, "Linh Tinh" to -3, "Địa Không" to -5, "Địa Kiếp" to -5,
        "Thái Tuế" to 2, "Thiên Mã" to 3, "Đào Hoa" to 2, "Hồng Loan" to 2, "Thiên Hỷ" to 2,
        "Cô Thần" to -2, "Quả Tú" to -2, "Đại Hao" to -2, "Tiểu Hao" to -1
    )

    // Triệt (Can Năm -> Set of Chi Index 0-11)
    // Giáp Kỷ: Thân(8) Dậu(9)
    // Ất Canh: Ngọ(6) Mùi(7)
    // Bính Tân: Thìn(4) Tỵ(5)
    // Đinh Nhâm: Dần(2) Mão(3)
    // Mậu Quý: Tý(0) Sửu(1)
    val TRIET_MAP = mapOf(
        0 to listOf(8, 9), // Giap
        5 to listOf(8, 9), // Ky
        1 to listOf(6, 7), // At
        6 to listOf(6, 7), // Canh
        2 to listOf(4, 5), // Binh
        7 to listOf(4, 5), // Tan
        3 to listOf(2, 3), // Dinh
        8 to listOf(2, 3), // Nham
        4 to listOf(0, 1), // Mau
        9 to listOf(0, 1)  // Quy
    )
    
    // Tứ Hóa (Can Năm -> Map<StarName, HoaType>)
    // Using simple mapping: Index of Can -> List of 4 stars [Loc, Quyen, Khoa, Ky]
    // 0 Giáp: Liêm, Phá, Vũ, Dương
    // 1 Ất: Cơ, Lương, Vi, Nguyệt
    // 2 Bính: Đồng, Cơ, Xương, Liêm
    // 3 Đinh: Nguyệt, Đồng, Cơ, Cự
    // 4 Mậu: Tham, Nguyệt, Bật, Cơ
    // 5 Kỷ: Vũ, Tham, Lương, Khúc
    // 6 Canh: Nhật, Vũ, Âm, Đồng
    // 7 Tân: Cự, Nhật, Khúc, Xương
    // 8 Nhâm: Lương, Vi, Phủ (or Tả), Vũ
    // 9 Quý: Phá, Cự, Âm, Tham
    
    // Note: Variations exist for Khoa/Ky in some stems (e.g., Nhâm: Tả vs Phủ).
    // Using standard Nam Phai (Southern School) usually: 
    // Nhâm: Lương (Lộc), Vi (Quyền), Phủ (Khoa), Vũ (Kỵ).
    // BUT User image shows Tật Ách (Tỵ) has Hóa Khoa. Tật Ách has Thiên Lương.
    // Tả Phù is also at Tỵ (Month 3).
    // If Rule is Phu (Thien Phu), then Hóa Khoa should be where Thien Phu is.
    // Thien Phu is at Tý (Phúc Đức).
    // So if User sees Hóa Khoa at Tật Ách, it MUST be attached to Tả Phù (since Tả is at Tỵ).
    // Therefore I will use: Nhâm -> Tả Phù = Hóa Khoa.
    
    val TU_HOA_MAP = mapOf(
        0 to listOf("Liêm Trinh", "Phá Quân", "Vũ Khúc", "Thái Dương"),
        1 to listOf("Thiên Cơ", "Thiên Lương", "Tử Vi", "Thái Âm"),
        2 to listOf("Thiên Đồng", "Thiên Cơ", "Văn Xương", "Liêm Trinh"),
        3 to listOf("Thái Âm", "Thiên Đồng", "Thiên Cơ", "Cự Môn"),
        4 to listOf("Tham Lang", "Thái Âm", "Hữu Bật", "Thiên Cơ"),
        5 to listOf("Vũ Khúc", "Tham Lang", "Thiên Lương", "Văn Khúc"),
        6 to listOf("Thái Dương", "Vũ Khúc", "Thái Âm", "Thiên Đồng"),
        7 to listOf("Cự Môn", "Thái Dương", "Văn Khúc", "Văn Xương"),
        8 to listOf("Thiên Lương", "Tử Vi", "Tả Phù", "Vũ Khúc"), // Changed Phu->Ta for Khoa based on user image evidence
        9 to listOf("Phá Quân", "Cự Môn", "Thái Âm", "Tham Lang")
    )

    // Hỏa Tinh Starts (Key: Remainder of ChiNam / 4 ?? No, needs grouping)
    // Chi Index:
    // Ty(0), Suu(1), Dan(2), Mao(3), Thin(4), Ty(5), Ngo(6), Mui(7), Than(8), Dau(9), Tuat(10), Hoi(11)
    
    // Groups:
    // Dan(2), Ngo(6), Tuat(10) -> Remainder 2 when mod 4?
    // 2%4=2, 6%4=2, 10%4=2. YES. Group Remainder = 2.
    
    // Than(8), Ty(0), Thin(4) ->
    // 8%4=0, 0%4=0, 4%4=0. YES. Group Remainder = 0.
    
    // Ty(5), Dau(9), Suu(1) ->
    // 5%4=1, 9%4=1, 1%4=1. YES. Group Remainder = 1.
    
    // Hoi(11), Mao(3), Mui(7) ->
    // 11%4=3, 3%4=3, 7%4=3. YES. Group Remainder = 3.
    
    // HOA_TINH_KHOI (Map<GroupMod4, StartIndex>)
    val HOA_TINH_KHOI = mapOf(
        2 to 1, // Dan Ngo Tuat -> Suu (1)
        0 to 2, // Than Ty Thin -> Dan (2)
        1 to 3, // Ty Dau Suu -> Mao (3)
        3 to 9  // Hoi Mao Mui -> Dau (9)
    )
    
    // LINH_TINH_KHOI (Map<GroupMod4, StartIndex>)
    val LINH_TINH_KHOI = mapOf(
        2 to 3,  // Dan Ngo Tuat -> Mao (3)
        0 to 10, // Than Ty Thin -> Tuat (10)
        1 to 10, // Ty Dau Suu -> Tuat (10)
        3 to 9   // Hoi Mao Mui -> Dau (9)
    )

    // Lưu Hà (Theo Can Năm)
    val LUU_HA_MAP = mapOf(
        0 to 9, 1 to 10, 2 to 7, 3 to 8, 4 to 5,
        5 to 6, 6 to 8, 7 to 3, 8 to 11, 9 to 2
    )

    // Thiên Trù (Theo Can Năm)
    val THIEN_TRU_MAP = mapOf(
        0 to 5, 1 to 6, 2 to 0, 3 to 5, 4 to 6,
        5 to 8, 6 to 2, 7 to 9, 8 to 9, 9 to 10
    )

    // Vòng Đào Hoa (Theo Chi Năm)
    val DAO_HOA_MAP = mapOf(
        5 to 6, 9 to 6, 1 to 6, // Ty Dau Suu -> Ngo
        11 to 0, 3 to 0, 7 to 0, // Hoi Mao Mui -> Ty
        2 to 3, 6 to 3, 10 to 3, // Dan Ngo Tuat -> Mao
        8 to 9, 0 to 9, 4 to 9   // Than Ty Thin -> Dau
    )

    // Sao Cô Thần, Quả Tú (Theo Chi Năm)
    val CO_QUA_MAP = mapOf(
        11 to Pair(2, 10), 0 to Pair(2, 10), 1 to Pair(2, 10),
        2 to Pair(5, 1), 3 to Pair(5, 1), 4 to Pair(5, 1),
        5 to Pair(8, 4), 6 to Pair(8, 4), 7 to Pair(8, 4),
        8 to Pair(11, 7), 9 to Pair(11, 7), 10 to Pair(11, 7)
    )
    
    // Sao Phá Toái (Theo Chi Năm)
    // Tỵ-Dậu-Sửu repeat grouping
    val PHA_TOAI_MAP = mapOf(
        0 to 5, 1 to 1, 2 to 9, 
        3 to 5, 4 to 1, 5 to 9,
        6 to 5, 7 to 1, 8 to 9,
        9 to 5, 10 to 1, 11 to 9
    )
    
    // Sao Thiên Đức (Theo Chi Năm)
    // Tý(Dậu), Sửu(Tuất), Dần(Hợi), Mão(Tý), Thìn(Sửu), Tỵ(Dần)
    // Ngọ(Mão), Mùi(Thìn), Thân(Tỵ), Dậu(Ngọ), Tuất(Mùi), Hợi(Thân) -- Offset +9 ?
    // 0(9?), 1(10), 2(11), 3(0), 4(1), 5(2), 6(3), 7(4), 8(5), 9(6), 10(7), 11(8)
    // Formula: (ChiNam + 9) % 12.
    // Verify Year Than (8) -> 8+9=17 -> 5 (Ty). User says Ty. Formula CORRECT.
    // However, I will use MAP for clarity and safety as requested.
    val THIEN_DUC_MAP = mapOf(
        0 to 9, 1 to 10, 2 to 11,
        3 to 0, 4 to 1, 5 to 2,
        6 to 3, 7 to 4, 8 to 5, // Year Thân -> Tỵ (5)
        9 to 6, 10 to 7, 11 to 8
    )

    // Sao Văn Tinh (Theo Can Năm)
    val VAN_TINH_MAP = mapOf(
        0 to 5, // Giap -> Ty
        1 to 6, // At -> Ngo
        2 to 8, // Binh -> Than
        3 to 9, // Dinh -> Dau
        4 to 8, // Mau -> Than
        5 to 9, // Ky -> Dau
        6 to 11, // Canh -> Hoi
        7 to 0, // Tan -> Ty
        8 to 9, // Nham -> Dau (Previously 2 Dan. User override: "Van Tinh at Dau for Nham")
        9 to 3  // Quy -> Mao
    )

    // Sao Thiên Quan, Thiên Phúc (Theo Can Năm)
    val THIEN_QUAN_PHUC_MAP = mapOf(
        0 to Pair(7, 9),  // Giap: Mui, Dau
        1 to Pair(4, 8),  // At: Thin, Than
        2 to Pair(5, 0),  // Binh: Ty, Ty
        3 to Pair(2, 11), // Dinh: Dan, Hoi
        4 to Pair(3, 3),  // Mau: Mao, Mao
        5 to Pair(9, 2),  // Ky: Dau, Dan
        6 to Pair(11, 6), // Canh: Hoi, Ngo
        7 to Pair(9, 5),  // Tan: Dau, Ty
        8 to Pair(10, 6), // Nham: Tuat, Ngo
        9 to Pair(6, 5)   // Quy: Ngo, Ty
    )

    // Thiên Mã (ReuseMap)
    val THIEN_MA_MAP = mapOf(
        2 to 8, 6 to 8, 10 to 8, 
        8 to 2, 0 to 2, 4 to 2,  
        5 to 11, 9 to 11, 1 to 11, 
        11 to 5, 3 to 5, 7 to 5   
    )

    // Độ sáng của 14 Chính Tinh (Miếu M, Vượng V, Đắc Đ, Hãm H, Bình B)
    // Key: Tên Sao. Value: List<String> 12 phần tử tương ứng 12 cung (Tý -> Hợi)
    val STAR_BRIGHTNESS = mapOf(
        "Tử Vi" to listOf("B", "Đ", "M", "B", "V", "M", "M", "Đ", "M", "B", "V", "B"), // Ty->Hoi
        "Thiên Cơ" to listOf("Đ", "Đ", "V", "M", "V", "B", "M", "Đ", "Đ", "M", "V", "H"), // Ty->Hoi
        "Thái Dương" to listOf("H", "Đ", "V", "V", "V", "V", "M", "Đ", "B", "H", "H", "H"),
        "Vũ Khúc" to listOf("V", "M", "V", "Đ", "M", "B", "B", "M", "V", "Đ", "M", "H"),
        "Thiên Đồng" to listOf("V", "H", "M", "Đ", "H", "Đ", "H", "H", "M", "Đ", "H", "Đ"),
        "Liêm Trinh" to listOf("V", "Đ", "M", "H", "V", "H", "V", "Đ", "V", "H", "V", "H"), // Than=V (Index 8) CORRECTED
        "Thiên Phủ" to listOf("M", "M", "M", "B", "M", "Đ", "V", "M", "M", "B", "M", "Đ"),
        "Thái Âm" to listOf("V", "Đ", "H", "H", "H", "H", "H", "B", "Đ", "V", "M", "M"), // Suu=Đ (Index 1) CORRECTED from M
        "Tham Lang" to listOf("H", "M", "Đ", "H", "V", "H", "H", "M", "Đ", "H", "V", "H"),
        "Cự Môn" to listOf("V", "H", "M", "M", "H", "B", "V", "H", "M", "M", "H", "V"),
        "Thiên Tướng" to listOf("V", "M", "M", "H", "V", "Đ", "V", "M", "M", "H", "V", "Đ"),
        "Thiên Lương" to listOf("V", "M", "V", "Đ", "M", "H", "M", "V", "V", "H", "M", "H"),
        "Thất Sát" to listOf("M", "Đ", "M", "H", "H", "V", "M", "Đ", "M", "H", "H", "V"),
        "Phá Quân" to listOf("M", "V", "H", "H", "Đ", "H", "M", "V", "H", "H", "Đ", "H")
    )
}
