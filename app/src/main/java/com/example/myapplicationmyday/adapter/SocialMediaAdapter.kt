package com.example.myapplicationmyday.adapter

import android.graphics.Color
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplicationmyday.R
import com.example.myapplicationmyday.data.SocialMediaLink
import com.example.myapplicationmyday.data.SocialPlatform
import com.example.myapplicationmyday.databinding.ItemSocialLinkBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class SocialMediaAdapter(
    private val onItemClick: (SocialMediaLink) -> Unit,
    private val onMenuClick: (SocialMediaLink, View) -> Unit
) : ListAdapter<SocialMediaLink, SocialMediaAdapter.SocialLinkViewHolder>(SocialLinkDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SocialLinkViewHolder {
        val binding = ItemSocialLinkBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SocialLinkViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SocialLinkViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SocialLinkViewHolder(
        private val binding: ItemSocialLinkBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(link: SocialMediaLink) {
            binding.apply {
                // Platform indicator color
                val color = try {
                    Color.parseColor(link.platform.color)
                } catch (e: Exception) {
                    Color.parseColor("#1DB954")
                }
                platformIndicator.setBackgroundColor(color)

                // Platform name
                tvPlatform.text = link.platform.displayName
                tvPlatform.setTextColor(color)

                // Title
                if (link.title.isNotEmpty()) {
                    tvTitle.text = link.title
                    tvTitle.visibility = View.VISIBLE
                } else {
                    tvTitle.text = when (link.platform) {
                        SocialPlatform.FACEBOOK -> "Contenido de Facebook"
                        SocialPlatform.INSTAGRAM -> "Contenido de Instagram"
                        SocialPlatform.TIKTOK -> "Contenido de TikTok"
                        SocialPlatform.TWITTER -> "Contenido de Twitter"
                        SocialPlatform.YOUTUBE -> "Contenido de YouTube"
                        SocialPlatform.OTHER -> "Contenido"
                    }
                }

                // Description
                if (link.description.isNotEmpty()) {
                    tvDescription.text = link.description
                    tvDescription.visibility = View.VISIBLE
                } else {
                    tvDescription.visibility = View.GONE
                }

                // Image preview
                if (link.imageUrl.isNotEmpty()) {
                    ivPreview.visibility = View.VISIBLE
                    Glide.with(ivPreview.context)
                        .load(link.imageUrl)
                        .transform(CenterCrop(), RoundedCorners(16))
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(ivPreview)
                } else {
                    ivPreview.visibility = View.GONE
                }

                // URL
                tvUrl.text = link.url

                // Date
                val timeAgo = DateUtils.getRelativeTimeSpanString(
                    link.createdAt,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
                )
                tvDate.text = timeAgo

                // Click listeners
                root.setOnClickListener { onItemClick(link) }
                btnMenu.setOnClickListener { onMenuClick(link, it) }
            }
        }
    }

    class SocialLinkDiffCallback : DiffUtil.ItemCallback<SocialMediaLink>() {
        override fun areItemsTheSame(oldItem: SocialMediaLink, newItem: SocialMediaLink): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SocialMediaLink, newItem: SocialMediaLink): Boolean {
            return oldItem == newItem
        }
    }
}
