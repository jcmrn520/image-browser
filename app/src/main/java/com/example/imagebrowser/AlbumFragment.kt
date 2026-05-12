package com.example.imagebrowser

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.imagebrowser.databinding.FragmentAlbumBinding

class AlbumFragment : Fragment() {

    private var _binding: FragmentAlbumBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAlbumBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvAlbums.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvAlbums.setHasFixedSize(true)
        loadAlbums()
    }

    private fun loadAlbums() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmptyState.visibility = View.GONE
        Thread {
            val albums = ImageLoader.loadAlbums(requireContext())
            activity?.runOnUiThread {
                if (_binding == null) return@runOnUiThread
                binding.progressBar.visibility = View.GONE
                if (albums.isEmpty()) {
                    binding.tvEmptyState.text = "未找到相册"
                    binding.tvEmptyState.visibility = View.VISIBLE
                } else {
                    binding.rvAlbums.adapter = AlbumAdapter(albums) { album ->
                        val intent = Intent(requireContext(), AlbumDetailActivity::class.java).apply {
                            putExtra(AlbumDetailActivity.EXTRA_ALBUM_NAME, album.name)
                            putExtra(AlbumDetailActivity.EXTRA_ALBUM_COUNT, album.count)
                            val uris = ArrayList(album.images.map { it.uri })
                            val names = ArrayList(album.images.map { it.displayName })
                            putStringArrayListExtra(AlbumDetailActivity.EXTRA_IMAGE_URIS, uris)
                            putStringArrayListExtra(AlbumDetailActivity.EXTRA_IMAGE_NAMES, names)
                        }
                        startActivity(intent)
                    }
                }
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
