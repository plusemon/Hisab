package com.plusemon.hisab.domain.util

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Formatters {

    private val banglaDigits = mapOf(
        '0' to '০', '1' to '১', '2' to '২', '3' to '৩', '4' to '৪',
        '5' to '৫', '6' to '৬', '7' to '৭', '8' to '৮', '9' to '৯',
        '.' to '.'
    )

    fun toBanglaDigits(numberStr: String): String {
        val sb = StringBuilder()
        for (ch in numberStr) {
            sb.append(banglaDigits[ch] ?: ch)
        }
        return sb.toString()
    }

    fun formatAmount(
        amount: Double,
        currencySymbol: String = "৳",
        useBanglaDigits: Boolean = false,
        hideBalances: Boolean = false
    ): String {
        if (hideBalances) {
            return "$currencySymbol ••••••"
        }
        val decimalFormat = DecimalFormat("#,##0.##")
        val formattedNumber = decimalFormat.format(amount)
        val finalNumber = if (useBanglaDigits) toBanglaDigits(formattedNumber) else formattedNumber
        return "$currencySymbol $finalNumber"
    }

    fun formatNumber(
        number: Number,
        useBanglaDigits: Boolean = false
    ): String {
        val decimalFormat = DecimalFormat("#,##0.##")
        val formattedNumber = decimalFormat.format(number)
        return if (useBanglaDigits) toBanglaDigits(formattedNumber) else formattedNumber
    }

    fun formatDate(timestamp: Long, isBangla: Boolean = false): String {
        val date = Date(timestamp)
        val formatPattern = if (isBangla) "dd MMM yyyy" else "dd MMM yyyy"
        val locale = if (isBangla) Locale("bn", "BD") else Locale.ENGLISH
        val sdf = SimpleDateFormat(formatPattern, locale)
        val formatted = sdf.format(date)
        return if (isBangla) toBanglaDigits(formatted) else formatted
    }

    fun formatTime(timestamp: Long, isBangla: Boolean = false): String {
        val date = Date(timestamp)
        val locale = if (isBangla) Locale("bn", "BD") else Locale.ENGLISH
        val sdf = SimpleDateFormat("hh:mm a", locale)
        val formatted = sdf.format(date)
        return if (isBangla) toBanglaDigits(formatted) else formatted
    }

    fun formatDateTime(timestamp: Long, isBangla: Boolean = false): String {
        return "${formatDate(timestamp, isBangla)}, ${formatTime(timestamp, isBangla)}"
    }

    fun getCurrentMonthYear(): String {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        return String.format(Locale.US, "%04d-%02d", year, month)
    }

    fun formatMonthYear(monthYear: String, isBangla: Boolean = false): String {
        try {
            val parts = monthYear.split("-")
            if (parts.size == 2) {
                val year = parts[0].toInt()
                val month = parts[1].toInt()
                val cal = Calendar.getInstance()
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month - 1)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val locale = if (isBangla) Locale("bn", "BD") else Locale.ENGLISH
                val sdf = SimpleDateFormat("MMMM yyyy", locale)
                val formatted = sdf.format(cal.time)
                return if (isBangla) toBanglaDigits(formatted) else formatted
            }
        } catch (e: Exception) {
            // fallback
        }
        return monthYear
    }

    fun getStartAndEndOfMonth(monthYear: String): Pair<Long, Long> {
        val parts = monthYear.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt()
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis
        return Pair(start, end)
    }

    fun getStartAndEndOfToday(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis
        return Pair(start, end)
    }

    fun getStartAndEndForPeriod(period: String): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        return when (period) {
            "THIS_MONTH" -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            "LAST_MONTH" -> {
                cal.add(Calendar.MONTH, -1)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            "THIS_YEAR" -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.set(Calendar.DAY_OF_YEAR, cal.getActualMaximum(Calendar.DAY_OF_YEAR))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            else -> {
                // Default to last 30 days
                val end = System.currentTimeMillis()
                cal.add(Calendar.DAY_OF_YEAR, -30)
                val start = cal.timeInMillis
                Pair(start, end)
            }
        }
    }
}
