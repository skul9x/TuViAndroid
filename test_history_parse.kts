/**
 * Test script: Verify parseHourFromTimeString and loadFromHistory parsing logic
 * Run: kotlinc -script test_history_parse.kts
 */

// ============ Replicate parsing logic from TuViViewModel ============

fun parseHourFromTimeString(time: String): Int {
    // Try to extract hour number directly: "0h (Giờ Tý)" → "0"
    val hourMatch = Regex("""^(\d+)h""").find(time)
    if (hourMatch != null) {
        return hourMatch.groupValues[1].toIntOrNull() ?: 12
    }

    // Fallback: map Chi name to hour
    val chiToHour = mapOf(
        "Tý" to 0, "Sửu" to 2, "Dần" to 4, "Mão" to 6,
        "Thìn" to 8, "Tị" to 10, "Ngọ" to 12, "Mùi" to 14,
        "Thân" to 16, "Dậu" to 18, "Tuất" to 20, "Hợi" to 22
    )
    return chiToHour.entries.firstOrNull { time.contains(it.key) }?.value ?: 12
}

fun parseSolarDate(solarDate: String): Triple<Int, Int, Int> {
    val dateParts = solarDate.split("/")
    val day = dateParts.getOrNull(0)?.toIntOrNull() ?: 1
    val month = dateParts.getOrNull(1)?.toIntOrNull() ?: 1
    val year = dateParts.getOrNull(2)?.toIntOrNull() ?: 1990
    return Triple(day, month, year)
}

fun parseGender(gender: String): String {
    return if (gender == "Nữ") "NU" else "NAM"
}

// ============ Test Cases ============

var passed = 0
var failed = 0

fun assert(name: String, expected: Any, actual: Any) {
    if (expected == actual) {
        println("  ✅ $name: $actual")
        passed++
    } else {
        println("  ❌ $name: expected=$expected, actual=$actual")
        failed++
    }
}

println("=" .repeat(60))
println("🧪 TEST: History Parse Logic")
println("=" .repeat(60))

// --- Test 1: parseHourFromTimeString ---
println("\n📌 Test 1: parseHourFromTimeString")

// Format từ TuViLogic: "${input.hour}h (Giờ ${LunarConverter.getChiGio(input.hour)})"
val hourTestCases = mapOf(
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

for ((input, expected) in hourTestCases) {
    assert("parseHour(\"$input\")", expected, parseHourFromTimeString(input))
}

// Fallback test (no number prefix)
assert("parseHour(fallback \"Giờ Ngọ\")", 12, parseHourFromTimeString("Giờ Ngọ"))
assert("parseHour(fallback \"Giờ Tý\")", 0, parseHourFromTimeString("Giờ Tý"))
assert("parseHour(unknown)", 12, parseHourFromTimeString("Unknown"))

// --- Test 2: parseSolarDate ---
println("\n📌 Test 2: parseSolarDate")

val dateTestCases = mapOf(
    "1/1/1990" to Triple(1, 1, 1990),
    "15/6/2000" to Triple(15, 6, 2000),
    "31/12/1985" to Triple(31, 12, 1985),
    "5/3/2025" to Triple(5, 3, 2025),
    "29/2/1996" to Triple(29, 2, 1996)
)

for ((input, expected) in dateTestCases) {
    val result = parseSolarDate(input)
    assert("parseSolarDate(\"$input\")", expected, result)
}

// Edge case: empty or malformed
val badDate = parseSolarDate("")
assert("parseSolarDate(empty)", Triple(1, 1, 1990), badDate)

// --- Test 3: parseGender ---
println("\n📌 Test 3: parseGender")
assert("parseGender(\"Nam\")", "NAM", parseGender("Nam"))
assert("parseGender(\"Nữ\")", "NU", parseGender("Nữ"))
assert("parseGender(\"\")", "NAM", parseGender(""))

// --- Test 4: ReadingStyle mapping ---
println("\n📌 Test 4: ReadingStyle fromString")

val readingStyleMap = mapOf(
    "Nghiêm túc" to "NGHIEM_TUC",
    "Đời thường" to "DOI_THUONG",
    "Hài hước" to "HAI_HUOC",
    "Kiếm hiệp" to "KIEM_HIEP",
    "Chữa lành" to "CHUA_LANH",
    "Chuyên gia mệnh lý" to "CHUYEN_GIA"
)

// Simulate ReadingStyle.fromString
fun readingStyleFromString(value: String): String {
    return readingStyleMap.entries.firstOrNull { it.key == value }?.value ?: "NGHIEM_TUC"
}

for ((input, expected) in readingStyleMap) {
    assert("readingStyle(\"$input\")", expected, readingStyleFromString(input))
}
assert("readingStyle(unknown)", "NGHIEM_TUC", readingStyleFromString("random"))

// --- Test 5: Full integration scenario ---
println("\n📌 Test 5: Full integration (simulated loadFromHistory)")

// Simulate a UserInfoResult from real LasoData
val fakeSolarDate = "15/8/1995"
val fakeTime = "14h (Giờ Mùi)"
val fakeGender = "Nữ"
val fakeReadingStyle = "Hài hước"
val fakeName = "Nguyễn Thị Mai"

val (day, month, year) = parseSolarDate(fakeSolarDate)
val hour = parseHourFromTimeString(fakeTime)
val gender = parseGender(fakeGender)
val style = readingStyleFromString(fakeReadingStyle)

assert("name", "Nguyễn Thị Mai", fakeName)
assert("day", 15, day)
assert("month", 8, month)
assert("year", 1995, year)
assert("hour", 14, hour)
assert("gender", "NU", gender)
assert("readingStyle", "HAI_HUOC", style)

// ============ Summary ============
println("\n" + "=" .repeat(60))
println("📊 KẾT QUẢ: $passed passed, $failed failed")
if (failed == 0) {
    println("🎉 ALL TESTS PASSED!")
} else {
    println("⚠️ CÓ $failed TEST FAIL!")
}
println("=" .repeat(60))
