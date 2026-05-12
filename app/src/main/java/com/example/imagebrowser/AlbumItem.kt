package com.example.imagebrowser

data class AlbumItem(
    val name: String,
    val coverUri: String,
    val count: Int,
    val images: List<ImageItem>
)
