package com.example.imagebrowser

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class ImageAdapter(
    private val images: List<ImageItem>,
    private val onImageClick: (ImageItem, Int) -> Unit
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.iv_thumbnail)

        fun bind(image: ImageItem, position: Int) {
            Glide.with(itemView.context)
                .load(Uri.parse(image.uri))
                .centerCrop()
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_broken_image)
                .transition(DrawableTransitionOptions.withCrossFade(150))
                .thumbnail(0.1f)
                .into(imageView)

            itemView.setOnClickListener {
                onImageClick(image, position)
            }

            // 无障碍描述
            imageView.contentDescription = image.displayName
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(images[position], position)
    }

    override fun getItemCount(): Int = images.size
}
