package com.example.imagebrowser

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore

object ImageLoader {

    fun loadAllImages(context: Context): List<ImageItem> {
        val images = mutableListOf<ImageItem>()

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val projection = mutableListOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(MediaStore.Images.Media.RELATIVE_PATH)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                add(MediaStore.Images.Media.WIDTH)
                add(MediaStore.Images.Media.HEIGHT)
            }
        }.toTypedArray()

        val selection = "${MediaStore.Images.Media.MIME_TYPE} IN (?, ?, ?, ?)"
        val selectionArgs = arrayOf("image/jpeg", "image/png", "image/gif", "image/webp")
        val sortOrder = "${MediaStore.Images.Media.DATE_MODIFIED} DESC"

        context.contentResolver.query(
            collection, projection, selection, selectionArgs, sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
            val bucketCol = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val relPathCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                cursor.getColumnIndex(MediaStore.Images.Media.RELATIVE_PATH) else -1
            val widthCol = cursor.getColumnIndex(MediaStore.Images.Media.WIDTH)
            val heightCol = cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                )
                images.add(
                    ImageItem(
                        id = id,
                        uri = uri.toString(),
                        displayName = cursor.getString(nameCol) ?: "Unknown",
                        size = cursor.getLong(sizeCol),
                        dateModified = cursor.getLong(dateCol),
                        width = if (widthCol >= 0) cursor.getInt(widthCol) else 0,
                        height = if (heightCol >= 0) cursor.getInt(heightCol) else 0,
                        mimeType = cursor.getString(mimeCol) ?: "",
                        bucketName = if (bucketCol >= 0) cursor.getString(bucketCol) ?: "" else "",
                        relativePath = if (relPathCol >= 0) cursor.getString(relPathCol) ?: "" else ""
                    )
                )
            }
        }

        return images
    }

    fun loadAlbums(context: Context): List<AlbumItem> {
        val allImages = loadAllImages(context)
        val grouped = allImages.groupBy { it.bucketName.ifEmpty { "其他" } }
        return grouped.map { (name, images) ->
            AlbumItem(
                name = name,
                coverUri = images.first().uri,
                count = images.size,
                images = images
            )
        }.sortedByDescending { it.count }
    }
}
