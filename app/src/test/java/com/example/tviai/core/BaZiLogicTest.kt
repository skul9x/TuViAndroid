package com.example.tviai.core

import com.example.tviai.data.Gender
import com.example.tviai.data.UserInput
import org.junit.Test
import org.junit.Assert.*
import java.io.File

class BaZiLogicTest {

    private val solarTermsJson = File("/home/skul9x/Desktop/Test_code/TuViAndroid-main/solar-term.json").readText()
    private val baziLogic = BaZiLogic(solarTermsJson)

    @Test
    fun testBaZi_18Mar2026() {
        val input = UserInput(
            name = "Test 2026",
            solarDay = 18,
            solarMonth = 3,
            solarYear = 2026,
            hour = 12,
            gender = Gender.NAM,
            viewingYear = 2026
        )
        
        val result = baziLogic.calculateBaZi(input)
        
        assertNotNull(result)
        
        // Output for debugging
        println("Result Year: ${result.year.stem} ${result.year.branch}")
        println("Result Month: ${result.month.stem} ${result.month.branch}")
        println("Result Day: ${result.day.stem} ${result.day.branch}")
        println("Result Hour: ${result.hour.stem} ${result.hour.branch}")
        println("Ten Gods Year Stem: ${result.tenGods.yearStemGod}")
        
        // Year: 2026 is Bính Ngọ
        assertEquals("Bính", result.year.stem)
        assertEquals("Ngọ", result.year.branch)
        
        // Month: 18/03/2026 is after Kinh Trập (05/03) -> Mão
        assertEquals("Mão", result.month.branch)
        
        // Day: 18/03/2026 is Tân Mão
        assertEquals("Tân", result.day.stem)
        assertEquals("Mão", result.day.branch)
        
        // Hour: 12:00 is Giờ Ngọ
        assertEquals("Ngọ", result.hour.branch)
        
        // Ten Gods for Tân Day Master
        // Bính (Year) is Chính Quan of Tân
        assertEquals("Chính Quan", result.tenGods.yearStemGod)
    }

    @Test
    fun testBaZi_05Feb1984() {
        // Reference: Kỷ Tỵ Day (Verified)
        val input = UserInput(
            name = "Test 1984",
            solarDay = 5,
            solarMonth = 2,
            solarYear = 1984,
            hour = 12,
            gender = Gender.NAM,
            viewingYear = 1984
        )
        
        val result = baziLogic.calculateBaZi(input)
        
        assertEquals("Kỷ", result.day.stem)
        assertEquals("Tỵ", result.day.branch)
        
        // Year: 05/02/1984 is after Lập Xuân (04/02) -> Giáp Tý
        assertEquals("Giáp", result.year.stem)
        assertEquals("Tý", result.year.branch)
    }
}
