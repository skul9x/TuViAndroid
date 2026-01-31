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
        "Nhâm" to Pair(5, 3),  // Tỵ, Mão
        "Quý" to Pair(5, 3)
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
}
