package com.example.imagebrowser

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.imagebrowser.databinding.ActivityFullScreenImageBinding

class FullScreenImageActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "FullScreenImageActivity"
    }

    private lateinit var binding: ActivityFullScreenImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 全屏显示
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        binding = ActivityFullScreenImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadImage()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

            val imageName = intent.getStringExtra(MainActivity.EXTRA_IMAGE_NAME) ?: ""
            title = imageName
        }
    }

    private fun loadImage() {
        val imageUri = intent.getStringExtra(MainActivity.EXTRA_IMAGE_URI)
        if (imageUri == null) {
            Log.w(TAG, "loadImage: imageUri is null, finishing activity")
            finish()
            return
        }

        Log.i(TAG, "loadImage: loading $imageUri")

        Glide.with(this)
            .load(Uri.parse(imageUri))
            .placeholder(R.drawable.ic_image_placeholder)
            .error(R.drawable.ic_broken_image)
            .into(binding.photoView)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
