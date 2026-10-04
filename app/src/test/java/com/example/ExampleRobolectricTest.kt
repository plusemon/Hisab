package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.plusemon.hisab.R
import com.plusemon.hisab.domain.util.MarkdownUtils
import com.plusemon.hisab.domain.util.VersionUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Hisab", appName)
  }

  @Test
  fun `version comparison logic`() {
    assertTrue(VersionUtils.isVersionNewer("1.0.3", "1.0.5"))
    assertTrue(VersionUtils.isVersionNewer("1.0", "1.0.5"))
  }

  @Test
  fun `markdown parsing and deduplication`() {
    val notes = "**Full Changelog**: link\n**Full Changelog**: link"
    val deduped = MarkdownUtils.deduplicateReleaseNotes(notes)
    assertEquals("**Full Changelog**: link", deduped)

    val annotated = MarkdownUtils.parseMarkdown("**Bold Text**")
    assertEquals("Bold Text", annotated.text)
  }
}
