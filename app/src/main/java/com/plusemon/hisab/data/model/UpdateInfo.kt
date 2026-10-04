package com.plusemon.hisab.data.model

data class UpdateInfo(
    val version: String,
    val rawTagName: String,
    val downloadUrl: String,
    val releaseNotes: String = ""
)
