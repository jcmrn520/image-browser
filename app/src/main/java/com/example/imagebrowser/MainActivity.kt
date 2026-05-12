package com.example.imagebrowser

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.imagebrowser.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val EXTRA_IMAGE_NAME = "extra_image_name"
        const val EXTRA_IMAGE_POSITION = "extra_image_position"
        private const val GRID_SPAN_COUNT = 3
    }

    private lateinit var binding: ActivityMainBinding
    private var imageAdapter: ImageAdapter? = null

    // 权限请求 Launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        if (granted) {
            Log.i(TAG, "Storage permission granted, loading images")
            loadImages()
        } else {
            Log.w(TAG, "Storage permission denied")
            showPermissionDeniedMessage()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        checkPermissionsAndLoad()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)
    }

    private fun setupRecyclerView() {
        binding.rvImages.layoutManager = GridLayoutManager(this, GRID_SPAN_COUNT)
        binding.rvImages.setHasFixedSize(true)
    }

    private fun checkPermissionsAndLoad() {
        when {
            hasStoragePermission() -> {
                Log.i(TAG, "Permission already granted")
                loadImages()
            }
            shouldShowRationale() -> {
                showPermissionRationale()
            }
            else -> {
                requestStoragePermission()
            }
        }
    }

    private fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun shouldShowRationale(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun requestStoragePermission() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        requestPermissionLauncher.launch(permissions)
    }

    private fun showPermissionRationale() {
        Snackbar.make(
            binding.root,
            R.string.permission_rationale,
            Snackbar.LENGTH_INDEFINITE
        ).setAction(R.string.grant_permission) {
            requestStoragePermission()
        }.show()
    }

    private fun showPermissionDeniedMessage() {
        binding.tvEmptyState.text = getString(R.string.permission_denied_message)
        binding.tvEmptyState.visibility = View.VISIBLE
        binding.rvImages.visibility = View.GONE
        binding.tvImageCount.visibility = View.GONE
    }

    private fun loadImages() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmptyState.visibility = View.GONE

        // 在后台线程加载图片列表
        Thread {
            val images = ImageLoader.loadAllImages(this)
            Log.i(TAG, "loadImages: found ${images.size} images")

            // 回到主线程更新 UI
            runOnUiThread {
                binding.progressBar.visibility = View.GONE
                updateUI(images)
            }
        }.start()
    }

    private fun updateUI(images: List<ImageItem>) {
        if (images.isEmpty()) {
            binding.tvEmptyState.text = getString(R.string.no_images_found)
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.rvImages.visibility = View.GONE
            binding.tvImageCount.visibility = View.GONE
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.rvImages.visibility = View.VISIBLE
            binding.tvImageCount.visibility = View.VISIBLE
            binding.tvImageCount.text = getString(R.string.image_count, images.size)

            imageAdapter = ImageAdapter(images) { image, position ->
                openFullScreen(image, position)
            }
            binding.rvImages.adapter = imageAdapter
        }
    }

    private fun openFullScreen(image: ImageItem, position: Int) {
        val intent = Intent(this, FullScreenImageActivity::class.java).apply {
            putExtra(EXTRA_IMAGE_URI, image.uri)
            putExtra(EXTRA_IMAGE_NAME, image.displayName)
            putExtra(EXTRA_IMAGE_POSITION, position)
        }
        startActivity(intent)
    }
}
