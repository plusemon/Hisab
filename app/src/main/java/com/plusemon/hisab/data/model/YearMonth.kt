package com.plusemon.hisab.data.model

import java.util.Calendar
import java.util.Locale

data class YearMonth(
    val year: Int,
    val month: Int // 1 to 12
) : Comparable<YearMonth> {

    init {
        require(month in 1..12) { "Month must be between 1 and 12, got $month" }
    }

    fun toMonthYearString(): String = String.format(Locale.US, "%04d-%02d", year, month)

    fun plusMonths(months: Long): YearMonth {
        val totalMonths = (year.toLong() * 12 + (month - 1)) + months
        val newYear = Math.floorDiv(totalMonths, 12).toInt()
        val newMonth = (Math.floorMod(totalMonths, 12) + 1).toInt()
        return YearMonth(newYear, newMonth)
    }

    fun minusMonths(months: Long): YearMonth = plusMonths(-months)

    fun isCurrent(): Boolean {
        val now = now()
        return year == now.year && month == now.month
    }

    override fun compareTo(other: YearMonth): Int {
        val yearDiff = year.compareTo(other.year)
        return if (yearDiff != 0) yearDiff else month.compareTo(other.month)
    }

    companion object {
        fun now(): YearMonth {
            val cal = Calendar.getInstance()
            return YearMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
        }

        fun fromString(monthYear: String): YearMonth {
            val parts = monthYear.split("-")
            require(parts.size == 2) { "Invalid month-year format: $monthYear" }
            return YearMonth(parts[0].toInt(), parts[1].toInt())
        }
    }
}
