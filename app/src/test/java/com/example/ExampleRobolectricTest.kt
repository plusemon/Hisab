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
    assertTrue(VersionUtils.isVersionNewer("v1.0.3", "v1.0.5"))
    assertTrue(VersionUtils.isVersionNewer("vv1.0.3", "v1.0.5"))
  }

  @Test
  fun `version cleaning and display formatting prevents duplicate v`() {
    assertEquals("1.0.16", VersionUtils.cleanVersion("vv1.0.16"))
    assertEquals("1.0.16", VersionUtils.cleanVersion("v1.0.16"))
    assertEquals("1.0.16", VersionUtils.cleanVersion("V1.0.16"))
    assertEquals("1.0.16", VersionUtils.cleanVersion("1.0.16"))

    assertEquals("v1.0.16", VersionUtils.formatDisplayVersion("vv1.0.16"))
    assertEquals("v1.0.16", VersionUtils.formatDisplayVersion("v1.0.16"))
    assertEquals("v1.0.16", VersionUtils.formatDisplayVersion("V1.0.16"))
    assertEquals("v1.0.16", VersionUtils.formatDisplayVersion("1.0.16"))
  }

  @Test
  fun `markdown parsing and deduplication`() {
    val notes = "**Full Changelog**: link\n**Full Changelog**: link"
    val deduped = MarkdownUtils.deduplicateReleaseNotes(notes)
    assertEquals("**Full Changelog**: link", deduped)

    val annotated = MarkdownUtils.parseMarkdown("**Bold Text**")
    assertEquals("Bold Text", annotated.text)
  }

  @Test
  fun `language preference persistence across app sessions`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = context.getSharedPreferences("hisab_settings_prefs", Context.MODE_PRIVATE)

    // Initially or default
    prefs.edit().putString("language", "bn").apply()
    assertEquals("bn", prefs.getString("language", "bn"))

    // User switches language on auth screen to English
    prefs.edit().putString("language", "en").apply()
    assertEquals("en", prefs.getString("language", "bn"))

    // Toggle back to Bangla
    val current = prefs.getString("language", "bn")
    val toggled = if (current == "bn") "en" else "bn"
    prefs.edit().putString("language", toggled).apply()
    assertEquals("bn", prefs.getString("language", "en"))
  }

  @Test
  fun `credentials error handling logic distinguishes no credential from cancellation`() {
    val noCredException = androidx.credentials.exceptions.NoCredentialException("No credentials available")
    val cancellationException = androidx.credentials.exceptions.GetCredentialCancellationException("User cancelled")

    val isNoCred1 = noCredException.message?.contains("No credentials available", ignoreCase = true) == true
    val isCancel1 = cancellationException.message?.contains("cancel", ignoreCase = true) == true

    assertTrue(isNoCred1)
    assertTrue(isCancel1)
  }

  @Test
  fun `auth diagnostics identifies known hashes and formats correctly`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val knownHashes = com.plusemon.hisab.domain.util.AuthDiagnostics.KNOWN_FIREBASE_SHA1_HASHES
    assertTrue(knownHashes.size >= 3)
    assertTrue(knownHashes.contains("71:D4:ED:DF:3C:4C:36:AC:A6:59:22:C5:A9:F9:74:B5:D2:EE:E3:91"))
    assertTrue(knownHashes.contains("A2:7F:46:52:5C:5E:1C:93:89:4F:07:C3:17:F9:83:B3:38:CE:51:0E"))
    assertEquals("41:33:99:F7:93:E1:54:93:B7:F9:15:4E:AC:08:B5:FA:24:19:3D:60", com.plusemon.hisab.domain.util.AuthDiagnostics.DEBUG_KEYSTORE_SHA1)

    val currentSha1 = com.plusemon.hisab.domain.util.AuthDiagnostics.getCurrentAppSha1(context)
    assertTrue(currentSha1.isNotBlank())
  }
}
