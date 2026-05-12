package com.example.imagebrowser

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.imagebrowser.databinding.ActivityAlbumDetailBinding

class AlbumDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ALBUM_NAME = "extra_album_name"
        const val EXTRA_ALBUM_COUNT = "extra_album_count"
        const val EXTRA_IMAGE_URIS = "extra_image_uris"
        const val EXTRA_IMAGE_NAMES = "extra_image_names"
    }

    private lateinit var binding: ActivityAlbumDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlbumDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val albumName = intent.getStringExtra(EXTRA_ALBUM_NAME) ?: "相册"
        val albumCount = intent.getIntExtra(EXTRA_ALBUM_COUNT, 0)
        val uris = intent.getStringArrayListExtra(EXTRA_IMAGE_URIS) ?: arrayListOf()
        val names = intent.getStringArrayListExtra(EXTRA_IMAGE_NAMES) ?: arrayListOf()

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = albumName
        }

        binding.tvImageCount.text = "$albumCount 张照片"
        binding.tvImageCount.visibility = View.VISIBLE

        val images = uris.mapIndexed { i, uri ->
            ImageItem(id = i.toLong(), uri = uri, displayName = names.getOrElse(i) { "" },
                size = 0, dateModified = 0)
        }

        binding.rvImages.layoutManager = GridLayoutManager(this, 3)
        binding.rvImages.setHasFixedSize(true)
        binding.rvImages.adapter = ImageAdapter(images) { _, position ->
            val intent = Intent(this, FullScreenImageActivity::class.java).apply {
                putStringArrayListExtra(FullScreenImageActivity.EXTRA_IMAGE_URIS, uris)
                putStringArrayListExtra(FullScreenImageActivity.EXTRA_IMAGE_NAMES, names)
                putExtra(FullScreenImageActivity.EXTRA_START_POSITION, position)
            }
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> { onBackPressedDispatcher.onBackPressed(); true }
        else -> super.onOptionsItemSelected(item)
    }
}
