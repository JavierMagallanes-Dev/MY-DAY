package com.example.myapplicationmyday.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplicationmyday.R
import com.example.myapplicationmyday.data.DeletedEntry
import com.example.myapplicationmyday.databinding.ItemDiaryEntryBinding
import java.text.SimpleDateFormat
import java.util.*

class DeletedEntryAdapter(
    private val onItemClick: (DeletedEntry) -> Unit,
    private val onItemLongClick: (DeletedEntry) -> Unit,
    private val isItemSelected: (Long) -> Boolean
) : ListAdapter<DeletedEntry, DeletedEntryAdapter.DeletedEntryViewHolder>(DeletedEntryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeletedEntryViewHolder {
        val binding = ItemDiaryEntryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DeletedEntryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeletedEntryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DeletedEntryViewHolder(
        private val binding: ItemDiaryEntryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
            
            binding.root.setOnLongClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemLongClick(getItem(position))
                }
                true
            }
            
            binding.btnEntryMenu.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(entry: DeletedEntry) {
            binding.tvEntryTitle.text = entry.title.ifEmpty { "Sin título" }
            binding.tvEntryContent.text = entry.content
            binding.tvEntryDate.text = formatDetailedDate(entry.date)
            
            // Actualizar apariencia según selección
            val isSelected = isItemSelected(entry.id)
            if (isSelected) {
                binding.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.primary_light)
                )
                binding.cardView.strokeWidth = 2
                binding.cardView.strokeColor = 
                    ContextCompat.getColor(binding.root.context, R.color.primary)
            } else {
                binding.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(binding.root.context, android.R.color.white)
                )
                binding.cardView.strokeWidth = 0
            }
        }

        private fun formatDetailedDate(timestamp: Long): String {
            val date = Date(timestamp)
            val dateFormat = SimpleDateFormat("EEEE, d MMM.", Locale.forLanguageTag("es-ES"))
            return dateFormat.format(date).replaceFirstChar { it.uppercase() }
        }
    }

    class DeletedEntryDiffCallback : DiffUtil.ItemCallback<DeletedEntry>() {
        override fun areItemsTheSame(oldItem: DeletedEntry, newItem: DeletedEntry): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DeletedEntry, newItem: DeletedEntry): Boolean {
            return oldItem == newItem
        }
    }
}
