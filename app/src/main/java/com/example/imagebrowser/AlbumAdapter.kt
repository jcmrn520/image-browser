package com.example.imagebrowser

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.imagebrowser.databinding.ItemAlbumBinding

class AlbumAdapter(
    private val albums: List<AlbumItem>,
    private val onAlbumClick: (AlbumItem) -> Unit
) : RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    inner class AlbumViewHolder(private val binding: ItemAlbumBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(album: AlbumItem) {
            binding.tvAlbumName.text = album.name
            binding.tvAlbumCount.text = "${album.count} 张"

            Glide.with(binding.root.context)
                .load(Uri.parse(album.coverUri))
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade(200))
                .placeholder(R.drawable.ic_image_placeholder)
                .into(binding.ivCover)

            binding.root.setOnClickListener { onAlbumClick(album) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val binding = ItemAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlbumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bind(albums[position])
    }

    override fun getItemCount(): Int = albums.size
}
