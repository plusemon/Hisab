package com.plusemon.hisab.data.repository

import android.util.Log
import com.plusemon.hisab.data.model.UpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class UpdateRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun fetchLatestRelease(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://api.github.com/repos/plusemon/hisab/releases/latest")
                .header("User-Agent", "Hisab-App")
                .header("Accept", "application/vnd.github.v3+json")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val bodyString = response.body?.string() ?: return@withContext null
                val json = JSONObject(bodyString)

                val tagName = json.optString("tag_name", "")
                if (tagName.isBlank()) return@withContext null
                val version = tagName.trim().removePrefix("v").removePrefix("V")

                val releaseNotes = json.optString("body", "")

                val assets = json.optJSONArray("assets") ?: return@withContext null
                var downloadUrl: String? = null

                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val assetName = asset.optString("name", "")
                    if (assetName.endsWith(".apk", ignoreCase = true)) {
                        val url = asset.optString("browser_download_url", "")
                        if (url.isNotBlank()) {
                            downloadUrl = url
                            break
                        }
                    }
                }

                if (downloadUrl.isNullOrBlank()) return@withContext null

                UpdateInfo(
                    version = version,
                    rawTagName = tagName,
                    downloadUrl = downloadUrl,
                    releaseNotes = releaseNotes
                )
            }
        } catch (e: Exception) {
            Log.e("UpdateRepository", "Silent failure checking update", e)
            null
        }
    }
}
