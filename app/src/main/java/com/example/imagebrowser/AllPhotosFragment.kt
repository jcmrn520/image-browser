package com.example.imagebrowser

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.imagebrowser.databinding.FragmentAllPhotosBinding

class AllPhotosFragment : Fragment() {

    private var _binding: FragmentAllPhotosBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAllPhotosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvImages.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvImages.setHasFixedSize(true)
        loadImages()
    }

    private fun loadImages() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmptyState.visibility = View.GONE
        Thread {
            val images = ImageLoader.loadAllImages(requireContext())
            activity?.runOnUiThread {
                if (_binding == null) return@runOnUiThread
                binding.progressBar.visibility = View.GONE
                if (images.isEmpty()) {
                    binding.tvEmptyState.text = "未找到图片"
                    binding.tvEmptyState.visibility = View.VISIBLE
                } else {
                    binding.rvImages.adapter = ImageAdapter(images) { image, position ->
                        openFullScreen(images, position)
                    }
                }
            }
        }.start()
    }

    private fun openFullScreen(images: List<ImageItem>, position: Int) {
        val uris = ArrayList(images.map { it.uri })
        val names = ArrayList(images.map { it.displayName })
        val intent = Intent(requireContext(), FullScreenImageActivity::class.java).apply {
            putStringArrayListExtra(FullScreenImageActivity.EXTRA_IMAGE_URIS, uris)
            putStringArrayListExtra(FullScreenImageActivity.EXTRA_IMAGE_NAMES, names)
            putExtra(FullScreenImageActivity.EXTRA_START_POSITION, position)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
