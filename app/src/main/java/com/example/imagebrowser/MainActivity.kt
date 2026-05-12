package com.example.imagebrowser

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.imagebrowser.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val EXTRA_IMAGE_NAME = "extra_image_name"
        const val EXTRA_IMAGE_POSITION = "extra_image_position"
    }

    private lateinit var binding: ActivityMainBinding

    private val permLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms.values.any { it }) setupViewPager()
        else showPermissionDenied()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        if (hasPermission()) setupViewPager()
        else if (shouldShow()) showRationale()
        else requestPerm()
    }

    private fun setupViewPager() {
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
    else ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun shouldShow() = if (Build.VERSION.SDK_INT >= 33)
        shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES)
    else shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)

    private fun requestPerm() = permLauncher.launch(
        if (Build.VERSION.SDK_INT >= 33) arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        else arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    )

    private fun showRationale() {
        Snackbar.make(binding.root, "需要读取存储权限才能浏览图片", Snackbar.LENGTH_INDEFINITE)
            .setAction("授予权限") { requestPerm() }.show()
    }

    private fun showPermissionDenied() {
        binding.tabLayout.visibility = View.GONE
        binding.viewPager.visibility = View.GONE
        Snackbar.make(binding.root, "存储权限被拒绝，请在设置中开启", Snackbar.LENGTH_LONG).show()
    }
}
