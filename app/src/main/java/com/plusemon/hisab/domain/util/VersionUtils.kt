package com.plusemon.hisab.domain.util

object VersionUtils {
    /**
     * Cleans leading 'v' or 'V' prefixes, trailing builds, and trims whitespace.
     * e.g. "vv1.0.16" -> "1.0.16", "v1.0.16" -> "1.0.16", "1.0.16" -> "1.0.16"
     */
    fun cleanVersion(version: String): String {
        return version.trim()
            .dropWhile { it == 'v' || it == 'V' }
            .trim()
    }

    /**
     * Formats a version string for display with exactly one leading 'v'.
     * e.g. "vv1.0.16" -> "v1.0.16", "v1.0.16" -> "v1.0.16", "1.0.16" -> "v1.0.16"
     */
    fun formatDisplayVersion(version: String): String {
        val clean = cleanVersion(version)
        return if (clean.isNotBlank()) "v$clean" else ""
    }

    /**
     * Compares two semantic version strings.
     * Returns true if latestVersion is strictly newer than currentVersion.
     * e.g., "1.10.0" > "1.9.0" -> true, "1.0.3" > "1.0" -> true
     */
    fun isVersionNewer(currentVersion: String, latestVersion: String): Boolean {
        val cleanCurrent = cleanVersion(currentVersion).split("-")[0]
        val cleanLatest = cleanVersion(latestVersion).split("-")[0]

        val currentParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }
        val latestParts = cleanLatest.split(".").mapNotNull { it.toIntOrNull() }

        val maxLength = maxOf(currentParts.size, latestParts.size)
        for (i in 0 until maxLength) {
            val curr = currentParts.getOrElse(i) { 0 }
            val lat = latestParts.getOrElse(i) { 0 }
            if (lat > curr) return true
            if (lat < curr) return false
        }
        return false
    }
}
