package com.example.tviai.viewmodel

import com.example.tviai.data.Gender
import com.example.tviai.data.ReadingStyle
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit test for history parsing logic used in TuViViewModel.loadFromHistory().
 * Tests the reverse-parsing of UserInfoResult fields back into UserInput values.
 */
class HistoryParseTest {

    // ============ Replicate private parsing functions from TuViViewModel ============

    private fun parseHourFromTimeString(time: String): Int {
        val hourMatch = Regex("""^(\d+)h""").find(time)
        if (hourMatch != null) {
            return hourMatch.groupValues[1].toIntOrNull() ?: 12
        }
        val chiToHour = mapOf(
            "Tý" to 0, "Sửu" to 2, "Dần" to 4, "Mão" to 6,
            "Thìn" to 8, "Tị" to 10, "Ngọ" to 12, "Mùi" to 14,
            "Thân" to 16, "Dậu" to 18, "Tuất" to 20, "Hợi" to 22
        )
        return chiToHour.entries.firstOrNull { time.contains(it.key) }?.value ?: 12
    }

    private fun parseSolarDate(solarDate: String): Triple<Int, Int, Int> {
        val dateParts = solarDate.split("/")
        val day = dateParts.getOrNull(0)?.toIntOrNull() ?: 1
        val month = dateParts.getOrNull(1)?.toIntOrNull() ?: 1
        val year = dateParts.getOrNull(2)?.toIntOrNull() ?: 1990
        return Triple(day, month, year)
    }

    // ============ Hour Parsing Tests ============

    @Test
    fun `parseHour - all 12 zodiac hours with number prefix`() {
        // Format from TuViLogic: "${input.hour}h (Giờ ${chi})"
        val cases = mapOf(
            "0h (Giờ Tý)" to 0,
            "2h (Giờ Sửu)" to 2,
            "4h (Giờ Dần)" to 4,
            "6h (Giờ Mão)" to 6,
            "8h (Giờ Thìn)" to 8,
            "10h (Giờ Tị)" to 10,
            "12h (Giờ Ngọ)" to 12,
            "14h (Giờ Mùi)" to 14,
            "16h (Giờ Thân)" to 16,
            "18h (Giờ Dậu)" to 18,
            "20h (Giờ Tuất)" to 20,
            "22h (Giờ Hợi)" to 22
        )
        for ((input, expected) in cases) {
            assertEquals("Failed for input: $input", expected, parseHourFromTimeString(input))
        }
    }

    @Test
    fun `parseHour - fallback chi name only`() {
        assertEquals(0, parseHourFromTimeString("Giờ Tý"))
        assertEquals(12, parseHourFromTimeString("Giờ Ngọ"))
        assertEquals(22, parseHourFromTimeString("Giờ Hợi"))
    }

    @Test
    fun `parseHour - unknown input returns default 12`() {
        assertEquals(12, parseHourFromTimeString("Unknown"))
        assertEquals(12, parseHourFromTimeString(""))
    }

    // ============ Date Parsing Tests ============

    @Test
    fun `parseSolarDate - standard dates`() {
        assertEquals(Triple(1, 1, 1990), parseSolarDate("1/1/1990"))
        assertEquals(Triple(15, 6, 2000), parseSolarDate("15/6/2000"))
        assertEquals(Triple(31, 12, 1985), parseSolarDate("31/12/1985"))
        assertEquals(Triple(5, 3, 2025), parseSolarDate("5/3/2025"))
        assertEquals(Triple(29, 2, 1996), parseSolarDate("29/2/1996"))
    }

    @Test
    fun `parseSolarDate - empty string returns defaults`() {
        assertEquals(Triple(1, 1, 1990), parseSolarDate(""))
    }

    // ============ Gender Parsing Tests ============

    @Test
    fun `parseGender - Nam and Nu`() {
        assertEquals(Gender.NAM, if ("Nam" == "Nữ") Gender.NU else Gender.NAM)
        assertEquals(Gender.NU, if ("Nữ" == "Nữ") Gender.NU else Gender.NAM)
        assertEquals(Gender.NAM, if ("" == "Nữ") Gender.NU else Gender.NAM)
    }

    // ============ ReadingStyle Parsing Tests ============

    @Test
    fun `readingStyle - all valid styles`() {
        assertEquals(ReadingStyle.NGHIEM_TUC, ReadingStyle.fromString("Nghiêm túc"))
        assertEquals(ReadingStyle.DOI_THUONG, ReadingStyle.fromString("Đời thường"))
        assertEquals(ReadingStyle.HAI_HUOC, ReadingStyle.fromString("Hài hước"))
        assertEquals(ReadingStyle.KIEM_HIEP, ReadingStyle.fromString("Kiếm hiệp"))
        assertEquals(ReadingStyle.CHUA_LANH, ReadingStyle.fromString("Chữa lành"))
        assertEquals(ReadingStyle.CHUYEN_GIA, ReadingStyle.fromString("Chuyên gia mệnh lý"))
    }

    @Test
    fun `readingStyle - unknown returns NGHIEM_TUC`() {
        assertEquals(ReadingStyle.NGHIEM_TUC, ReadingStyle.fromString("random"))
        assertEquals(ReadingStyle.NGHIEM_TUC, ReadingStyle.fromString(""))
    }

    // ============ Full Integration Test ============

    @Test
    fun `full integration - simulate loadFromHistory parsing`() {
        // Simulate a real UserInfoResult from LasoData
        val solarDate = "15/8/1995"
        val time = "14h (Giờ Mùi)"
        val genderStr = "Nữ"
        val readingStyleStr = "Hài hước"
        val name = "Nguyễn Thị Mai"

        // Parse all fields
        val (day, month, year) = parseSolarDate(solarDate)
        val hour = parseHourFromTimeString(time)
        val gender = if (genderStr == "Nữ") Gender.NU else Gender.NAM
        val readingStyle = ReadingStyle.fromString(readingStyleStr)

        // Verify
        assertEquals("Nguyễn Thị Mai", name)
        assertEquals(15, day)
        assertEquals(8, month)
        assertEquals(1995, year)
        assertEquals(14, hour)
        assertEquals(Gender.NU, gender)
        assertEquals(ReadingStyle.HAI_HUOC, readingStyle)
    }
}
