package com.example.myapplicationmyday.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplicationmyday.R
import com.example.myapplicationmyday.data.DiaryEntry
import com.example.myapplicationmyday.databinding.ItemDateHeaderBinding
import com.example.myapplicationmyday.databinding.ItemDiaryEntryBinding
import java.text.SimpleDateFormat
import java.util.*

sealed class DiaryItem {
    data class Header(val title: String) : DiaryItem()
    data class Entry(val entry: DiaryEntry) : DiaryItem()
}

class DiaryAdapter(
    private val onItemClick: (DiaryEntry) -> Unit,
    private val onMenuClick: (DiaryEntry, View) -> Unit
) : ListAdapter<DiaryItem, RecyclerView.ViewHolder>(DiaryItemDiffCallback()) {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ENTRY = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DiaryItem.Header -> TYPE_HEADER
            is DiaryItem.Entry -> TYPE_ENTRY
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val binding = ItemDateHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                HeaderViewHolder(binding)
            }
            else -> {
                val binding = ItemDiaryEntryBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                EntryViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is DiaryItem.Header -> (holder as HeaderViewHolder).bind(item.title)
            is DiaryItem.Entry -> (holder as EntryViewHolder).bind(item.entry)
        }
    }

    inner class HeaderViewHolder(
        private val binding: ItemDateHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String) {
            binding.tvDateHeader.text = title
        }
    }

    inner class EntryViewHolder(
        private val binding: ItemDiaryEntryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    if (item is DiaryItem.Entry) {
                        onItemClick(item.entry)
                    }
                }
            }

            binding.btnEntryMenu.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    if (item is DiaryItem.Entry) {
                        onMenuClick(item.entry, it)
                    }
                }
            }
        }

        fun bind(entry: DiaryEntry) {
            binding.tvEntryTitle.text = entry.title.ifEmpty { "Sin título" }
            binding.tvEntryContent.text = entry.content
            binding.tvEntryDate.text = formatDetailedDate(entry.date)
        }

        private fun formatDetailedDate(timestamp: Long): String {
            val date = Date(timestamp)
            val dateFormat = SimpleDateFormat("EEEE, d MMM.", Locale.forLanguageTag("es-ES"))
            return dateFormat.format(date).replaceFirstChar { it.uppercase() }
        }
    }

    class DiaryItemDiffCallback : DiffUtil.ItemCallback<DiaryItem>() {
        override fun areItemsTheSame(oldItem: DiaryItem, newItem: DiaryItem): Boolean {
            return when {
                oldItem is DiaryItem.Header && newItem is DiaryItem.Header -> 
                    oldItem.title == newItem.title
                oldItem is DiaryItem.Entry && newItem is DiaryItem.Entry -> 
                    oldItem.entry.id == newItem.entry.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: DiaryItem, newItem: DiaryItem): Boolean {
            return oldItem == newItem
        }
    }
}

// Función de extensión para convertir entradas a items con headers
fun List<DiaryEntry>.toItemsWithHeaders(): List<DiaryItem> {
    if (isEmpty()) return emptyList()
    
    val items = mutableListOf<DiaryItem>()
    val grouped = this.groupBy { entry ->
        val calendar = Calendar.getInstance().apply { timeInMillis = entry.date }
        val now = Calendar.getInstance()
        
        when {
            isSameDay(now, calendar) -> "Hoy"
            isYesterday(now, calendar) -> "Ayer"
            else -> {
                val dateFormat = SimpleDateFormat("EEEE, d 'de' MMMM", Locale.forLanguageTag("es-ES"))
                dateFormat.format(Date(entry.date)).replaceFirstChar { it.uppercase() }
            }
        }
    }
    
    grouped.forEach { (header, entries) ->
        items.add(DiaryItem.Header(header))
        entries.forEach { entry ->
            items.add(DiaryItem.Entry(entry))
        }
    }
    
    return items
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun isYesterday(now: Calendar, date: Calendar): Boolean {
    val yesterday = now.clone() as Calendar
    yesterday.add(Calendar.DAY_OF_YEAR, -1)
    return isSameDay(yesterday, date)
}
