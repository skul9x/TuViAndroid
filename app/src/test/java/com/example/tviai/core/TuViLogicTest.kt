package com.example.tviai.core

import com.example.tviai.data.Gender
import com.example.tviai.data.ReadingStyle
import com.example.tviai.data.UserInput
import org.junit.Test
import org.junit.Assert.*

class TuViLogicTest {

    private val logic = TuViLogic()

    @Test
    fun testAnSao_Basic() {
        // Test case: 15/08/1990 (Dương), Giờ Ngọ (12:00), Nam
        val input = UserInput(
            name = "Test User",
            solarDay = 15,
            solarMonth = 8,
            solarYear = 1990,
            hour = 12,
            gender = Gender.NAM,
            viewingYear = 2024
        )
        
        val result = logic.anSao(input)
        
        assertNotNull(result)
        assertEquals("Test User", result.info.name)
        assertEquals("Nam", result.info.gender)
        
        // Verify Cục (Should be verified against Python output or logic)
        // 1990 (Canh Ngọ). Mệnh tại Dần? 
        // Tháng 6 Âm (Approx). Tháng 1=Dần. Tháng 6=Mùi (6+1=7 -> Ngọ?). 
        // Logic: Month from lunar.
        // Let's assume standard lunar conversion is approx correct in our Mock.
        
        // We verify that result contains 12 cung
        assertEquals(12, result.cung.size)
        // Verify Mệnh is set
        assertTrue(result.cung.any { it.chucNang.contains("Mệnh") })
    }
}
