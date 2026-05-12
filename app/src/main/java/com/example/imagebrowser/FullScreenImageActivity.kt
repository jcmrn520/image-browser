package com.example.imagebrowser

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.imagebrowser.databinding.ActivityFullScreenImageBinding

class FullScreenImageActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_IMAGE_URIS = "extra_image_uris"
        const val EXTRA_IMAGE_NAMES = "extra_image_names"
        const val EXTRA_START_POSITION = "extra_start_position"
        // 向后兼容
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val EXTRA_IMAGE_NAME = "extra_image_name"
    }

    private lateinit var binding: ActivityFullScreenImageBinding
    private var uiVisible = true
    private var currentPosition = 0
    private var uris: List<String> = emptyList()
    private var names: List<String> = emptyList()

    // 从 ImageLoader 加载的完整图片数据（用于显示 EXIF）
    private var allImages: List<ImageItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        binding = ActivityFullScreenImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取数据
        uris = intent.getStringArrayListExtra(EXTRA_IMAGE_URIS)?.toList()
            ?: intent.getStringExtra(EXTRA_IMAGE_URI)?.let { listOf(it) }
            ?: return finish()
        names = intent.getStringArrayListExtra(EXTRA_IMAGE_NAMES)?.toList()
            ?: intent.getStringExtra(EXTRA_IMAGE_NAME)?.let { listOf(it) }
            ?: uris.map { "" }
        val startPos = intent.getIntExtra(EXTRA_START_POSITION, 0)

        setupToolbar()
        setupViewPager(startPos)
        setupBottomBar()

        // 后台加载完整图片数据（用于信息面板）
        Thread {
            allImages = ImageLoader.loadAllImages(this)
        }.start()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        updateTitle(0)
    }

    private fun setupViewPager(startPos: Int) {
        val adapter = ImagePagerAdapter(uris) { toggleUi() }
        binding.viewPagerImages.adapter = adapter
        binding.viewPagerImages.setCurrentItem(startPos, false)
        currentPosition = startPos

        binding.viewPagerImages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentPosition = position
                updateTitle(position)
            }
        })
    }

    private fun setupBottomBar() {
        binding.btnInfo.setOnClickListener { showImageInfo() }
        binding.btnShare.setOnClickListener {
            Toast.makeText(this, "分享功能即将推出", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTitle(position: Int) {
        val name = names.getOrElse(position) { "" }
        val count = if (uris.size > 1) "${position + 1}/${uris.size}" else ""
        supportActionBar?.title = name.ifEmpty { "图片" }
        supportActionBar?.subtitle = count
    }

    private fun toggleUi() {
        uiVisible = !uiVisible
        val visibility = if (uiVisible) View.VISIBLE else View.GONE
        binding.layoutTopBar.visibility = visibility
        binding.layoutBottomBar.visibility = visibility
    }

    private fun showImageInfo() {
        val uri = uris.getOrElse(currentPosition) { return }
        val name = names.getOrElse(currentPosition) { "" }

        // 从已加载的图片列表中匹配完整数据
        val imageItem = allImages.firstOrNull { it.uri == uri }

        val sheet = ImageInfoBottomSheet.newInstance(
            uri = uri,
            name = name,
            size = imageItem?.size ?: 0L,
            width = imageItem?.width ?: 0,
            height = imageItem?.height ?: 0,
            dateModified = imageItem?.dateModified ?: 0L,
            mimeType = imageItem?.mimeType ?: "",
            relativePath = imageItem?.relativePath ?: ""
        )
        sheet.show(supportFragmentManager, "image_info")
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> { onBackPressedDispatcher.onBackPressed(); true }
        else -> super.onOptionsItemSelected(item)
    }
}
