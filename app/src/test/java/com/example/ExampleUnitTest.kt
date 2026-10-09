package com.example

import com.plusemon.hisab.data.model.AppSyncStatus
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testGreetingLogic() {
    fun getGreeting(hour: Int, isBn: Boolean): String {
      return when {
        hour in 5..11 -> if (isBn) "শুভ সকাল" else "Good Morning"
        hour in 12..16 -> if (isBn) "শুভ অপরাহ্ন" else "Good Afternoon"
        hour in 17..20 -> if (isBn) "শুভ সন্ধ্যা" else "Good Evening"
        else -> if (isBn) "শুভ রাত্রি" else "Good Night"
      }
    }

    assertEquals("Good Morning", getGreeting(8, false))
    assertEquals("শুভ সকাল", getGreeting(8, true))
    assertEquals("Good Afternoon", getGreeting(14, false))
    assertEquals("শুভ অপরাহ্ন", getGreeting(14, true))
    assertEquals("Good Evening", getGreeting(18, false))
    assertEquals("শুভ সন্ধ্যা", getGreeting(18, true))
    assertEquals("Good Night", getGreeting(23, false))
    assertEquals("শুভ রাত্রি", getGreeting(2, true))
  }

  @Test
  fun testAppSyncStatus() {
    val saving: AppSyncStatus = AppSyncStatus.Saving
    val saved: AppSyncStatus = AppSyncStatus.Saved()
    val syncing: AppSyncStatus = AppSyncStatus.Syncing
    val synced: AppSyncStatus = AppSyncStatus.Synced()
    val checking: AppSyncStatus = AppSyncStatus.CheckingUpdates

    assertTrue(saving is AppSyncStatus.Saving)
    assertTrue(saved is AppSyncStatus.Saved)
    assertTrue(syncing is AppSyncStatus.Syncing)
    assertTrue(synced is AppSyncStatus.Synced)
    assertTrue(checking is AppSyncStatus.CheckingUpdates)
  }

  @Test
  fun testYearMonthCreationAndFormatting() {
    val ym = com.plusemon.hisab.data.model.YearMonth(2026, 10)
    assertEquals("2026-10", ym.toMonthYearString())

    val ymMay = com.plusemon.hisab.data.model.YearMonth(2026, 5)
    assertEquals("2026-05", ymMay.toMonthYearString())

    val parsed = com.plusemon.hisab.data.model.YearMonth.fromString("2026-10")
    assertEquals(ym, parsed)
  }

  @Test
  fun testYearMonthMath() {
    val ymJan = com.plusemon.hisab.data.model.YearMonth(2026, 1)
    val prev = ymJan.minusMonths(1)
    assertEquals(2025, prev.year)
    assertEquals(12, prev.month)

    val ymDec = com.plusemon.hisab.data.model.YearMonth(2026, 12)
    val next = ymDec.plusMonths(1)
    assertEquals(2027, next.year)
    assertEquals(1, next.month)

    val ymOct = com.plusemon.hisab.data.model.YearMonth(2026, 10)
    val august = ymOct.minusMonths(2)
    assertEquals(2026, august.year)
    assertEquals(8, august.month)
  }

  @Test
  fun testYearMonthComparison() {
    val sep2026 = com.plusemon.hisab.data.model.YearMonth(2026, 9)
    val oct2026 = com.plusemon.hisab.data.model.YearMonth(2026, 10)
    val dec2025 = com.plusemon.hisab.data.model.YearMonth(2025, 12)

    assertTrue(sep2026 < oct2026)
    assertTrue(dec2025 < sep2026)
    assertEquals(0, oct2026.compareTo(com.plusemon.hisab.data.model.YearMonth(2026, 10)))
  }

  @Test
  fun testMonthHelperFormatters() {
    assertEquals("October", com.plusemon.hisab.domain.util.Formatters.getMonthName(10, false))
    assertEquals("অক্টোবর", com.plusemon.hisab.domain.util.Formatters.getMonthName(10, true))
    assertEquals("Oct", com.plusemon.hisab.domain.util.Formatters.getMonthShortName(10, false))
    assertEquals("অক্টো", com.plusemon.hisab.domain.util.Formatters.getMonthShortName(10, true))
  }
}

