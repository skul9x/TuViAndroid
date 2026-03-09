package com.example.tviai.core.util

import java.util.Calendar

/**
 * Utility to convert Solar date to Lunar date.
 * Uses Table Lookup method (standard for Asian Lunar Calendar).
 * Supports range 1900 - 2049.
 */
object LunarDateUtil {

    // Data table for Lunar Calendar from 1900 to 2049
    // Format:
    // - 4 high bits (optional): Leap month days (often not used if fixed rule, but here seems unused or implicit)
    // - Next bits: Month sizes (1=30, 0=29)
    // - Low 4 bits: Leap month index (0 if none)
    private val LUNAR_INFO = longArrayOf(
        0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,
        0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977,
        0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,
        0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950,
        0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557,
        0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5d0, 0x14573, 0x052d0, 0x0a9a8, 0x0e950, 0x06aa0,
        0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0,
        0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b5a0, 0x195a6,
        0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570,
        0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0,
        0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5,
        0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930,
        0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530,
        0x05aa0, 0x076a3, 0x096d0, 0x04bd7, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45,
        0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0
    )

    private const val MIN_YEAR = 1900
    private const val MAX_YEAR = 2049
    
    // Base date: Jan 31, 1900 (Lunar 1/1/1900)
    // We calculate offsets relative to this date manually to avoid java.time requirement on older Android versions.

    /**
     * Convert solar date to lunar date.
     */
    fun convertSolarToLunar(day: Int, month: Int, year: Int): LunarResult {
        if (year < MIN_YEAR || year > MAX_YEAR) {
            // Fallback or error: Return solar as lunar for now, or throw
            return LunarResult(day, month, year, false)
        }

        // Calculate offset days from Base Date (1900-01-31)
        var offset = calculateOffsetFromBase(day, month, year)
        
        var iYear = MIN_YEAR
        var daysInYear = 0

        // 1. Find Lunar Year
        while (iYear <= MAX_YEAR) {
            daysInYear = getYearDays(iYear)
            if (offset < daysInYear) break
            offset -= daysInYear
            iYear++
        }
        val lunarYear = iYear

        // 2. Find Lunar Month
        val leapMonth = getLeapMonth(lunarYear)
        var iMonth = 1
        var isLeapMonth = false
        
        while (iMonth <= 12) {
            var daysInMonth = getMonthDays(lunarYear, iMonth)

            if (offset < daysInMonth) break 
            
            offset -= daysInMonth

            if (leapMonth == iMonth) {
                if (isLeapMonth) {
                    isLeapMonth = false
                    iMonth++
                } else {
                    daysInMonth = getLeapMonthDays(lunarYear)
                    if (offset < daysInMonth) {
                        isLeapMonth = true
                        break
                    }
                    offset -= daysInMonth
                    isLeapMonth = false
                    iMonth++
                }
            } else {
                iMonth++
            }
        }

        val lunarDay = offset + 1
        
        return LunarResult(lunarDay, iMonth, lunarYear, isLeapMonth)
    }

    private fun getYearDays(year: Int): Int {
        var sum = 0
        for (i in 1..12) {
            sum += getMonthDays(year, i)
        }
        return sum + getLeapMonthDays(year)
    }

    private fun getLeapMonthDays(year: Int): Int {
        if (getLeapMonth(year) == 0) return 0
        // Check bit 16 (0x10000) for leap month size
        return if ((LUNAR_INFO[year - MIN_YEAR] and 0x10000) != 0L) 30 else 29
    }

    private fun getLeapMonth(year: Int): Int {
        return (LUNAR_INFO[year - MIN_YEAR] and 0xf).toInt()
    }

    private fun getMonthDays(year: Int, month: Int): Int {
        // Bit mask from 0x8000 (month 1) down to 0x8 (month 12)
        // month 1 -> shift 16-1 = 15? No, user code:
        // LUNAR_INFO[..] & (0x10000 shr month)
        // month=1 -> 0x10000 >> 1 = 0x8000. Correct.
        return if ((LUNAR_INFO[year - MIN_YEAR] and (0x10000 shr month).toLong()) == 0L) 29 else 30
    }

    private fun calculateOffsetFromBase(day: Int, month: Int, year: Int): Int {
        // Base: 1900-01-31
        // Count total days from base to current date
        
        // Simple iteration to avoid Calendar complexity
        var days = 0
        
        // Add days for full years
        for (y in MIN_YEAR until year) {
            days += if (isLeapYear(y)) 366 else 365
        }
        
        // Add days for current year months
        val daysInMonths = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        for (m in 1 until month) {
            days += daysInMonths[m - 1]
            if (m == 2 && isLeapYear(year)) days++
        }
        
        // Add days in current month
        days += day
        
        // Subtract base offset (Jan 31 days)
        // Base is Jan 31. So if we are at Feb 1 1900:
        // year=1900, days = 31 + 1 = 32.
        // Offset should be 1. 32 - 31 = 1. Correct.
        days -= 31 
        
        return days
    }
    
    private fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }

    data class LunarResult(
        val day: Int,
        val month: Int, 
        val year: Int,
        val isLeap: Boolean
    )
}
