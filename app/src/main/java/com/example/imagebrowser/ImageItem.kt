package com.example.imagebrowser

data class ImageItem(
    val id: Long,
    val uri: String,
    val displayName: String,
    val size: Long,
    val dateModified: Long
)
