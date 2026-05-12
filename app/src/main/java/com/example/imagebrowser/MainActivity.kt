package com.example.imagebrowser

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.imagebrowser.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val EXTRA_IMAGE_NAME = "extra_image_name"
        const val EXTRA_IMAGE_POSITION = "extra_image_position"
    }

    private lateinit var binding: ActivityMainBinding
    private var viewPagerSetup = false

    private val permLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms.values.any { it }) {
            setupViewPager()
        } else {
            showPermissionDenied()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        checkAndRequestPermission()
    }

    override fun onResume() {
        super.onResume()
        // 从设置页面回来后，重新检查权限
        if (!viewPagerSetup && hasPermission()) {
            setupViewPager()
        }
    }

    private fun checkAndRequestPermission() {
        when {
            hasPermission() -> setupViewPager()
            else -> requestPerm()
        }
    }

    private fun setupViewPager() {
        if (viewPagerSetup) return
        viewPagerSetup = true

        binding.tabLayout.visibility = View.VISIBLE
        binding.viewPager.visibility = View.VISIBLE
        binding.layoutPermissionDenied.visibility = View.GONE

        val adapter = MainPagerAdapter(this)
        binding.viewPager.adapter = adapter
        binding.viewPager.offscreenPageLimit = 2

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, pos ->
            when (pos) {
                0 -> {
                    tab.text = "相册"
                    tab.setIcon(R.drawable.ic_photo_album)
                }
                1 -> {
                    tab.text = "全部"
                    tab.setIcon(R.drawable.ic_photo_grid)
                }
            }
        }.attach()
    }

    private fun hasPermission() = if (Build.VERSION.SDK_INT >= 33)
        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
    else
        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun requestPerm() {
        if (Build.VERSION.SDK_INT >= 33) {
            permLauncher.launch(arrayOf(Manifest.permission.READ_MEDIA_IMAGES))
        } else {
            permLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        }
    }

    private fun showPermissionDenied() {
        binding.tabLayout.visibility = View.GONE
        binding.viewPager.visibility = View.GONE
        binding.layoutPermissionDenied.visibility = View.VISIBLE

        binding.btnGoToSettings.setOnClickListener {
            // 引导用户去系统设置手动开启权限
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
        }

        binding.btnRetryPermission.setOnClickListener {
            viewPagerSetup = false
            checkAndRequestPermission()
        }
    }
}
