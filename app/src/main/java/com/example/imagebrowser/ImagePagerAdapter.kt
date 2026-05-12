package com.example.imagebrowser

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.imagebrowser.databinding.ItemFullScreenImageBinding
import com.github.chrisbanes.photoview.PhotoView

class ImagePagerAdapter(
    private val uris: List<String>,
    private val onSingleClick: () -> Unit
) : RecyclerView.Adapter<ImagePagerAdapter.ImageVH>() {

    inner class ImageVH(val binding: ItemFullScreenImageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(uri: String) {
            Glide.with(binding.root.context)
                .load(Uri.parse(uri))
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_broken_image)
                .into(binding.photoView)

            binding.photoView.setOnPhotoTapListener { _, _, _ -> onSingleClick() }
            binding.photoView.setOnOutsidePhotoTapListener { onSingleClick() }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageVH {
        val binding = ItemFullScreenImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageVH(binding)
    }

    override fun onBindViewHolder(holder: ImageVH, position: Int) = holder.bind(uris[position])

    override fun getItemCount(): Int = uris.size
}
