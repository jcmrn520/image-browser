package com.example.imagebrowser

data class ImageItem(
    val id: Long,
    val uri: String,
    val displayName: String,
    val size: Long,
    val dateModified: Long,
    val width: Int = 0,
    val height: Int = 0,
    val mimeType: String = "",
    val bucketName: String = "",
    val relativePath: String = ""
)
