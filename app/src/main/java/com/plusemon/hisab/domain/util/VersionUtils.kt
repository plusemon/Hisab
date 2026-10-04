package com.plusemon.hisab.domain.util

object VersionUtils {
    /**
     * Compares two semantic version strings.
     * Returns true if latestVersion is strictly newer than currentVersion.
     * e.g., "1.10.0" > "1.9.0" -> true, "1.0.3" > "1.0" -> true
     */
    fun isVersionNewer(currentVersion: String, latestVersion: String): Boolean {
        val cleanCurrent = currentVersion.trim().removePrefix("v").removePrefix("V").split("-")[0]
        val cleanLatest = latestVersion.trim().removePrefix("v").removePrefix("V").split("-")[0]

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
