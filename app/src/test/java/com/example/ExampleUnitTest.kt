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
}

