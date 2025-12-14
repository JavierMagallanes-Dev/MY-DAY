package com.example.myapplicationmyday

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplicationmyday.adapter.DeletedEntryAdapter
import com.example.myapplicationmyday.data.DeletedEntry
import com.example.myapplicationmyday.data.DiaryEntry
import com.example.myapplicationmyday.databinding.ActivityDeletedEntriesBinding
import com.example.myapplicationmyday.viewmodel.DeletedEntryViewModel
import com.example.myapplicationmyday.viewmodel.DiaryViewModel
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class DeletedEntriesActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityDeletedEntriesBinding
    private val deletedViewModel: DeletedEntryViewModel by viewModels()
    private val diaryViewModel: DiaryViewModel by viewModels()
    private lateinit var adapter: DeletedEntryAdapter
    private var isSelectionMode = false
    private val selectedItems = mutableSetOf<Long>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeletedEntriesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        cleanOldEntries()
    }
    
    private fun setupRecyclerView() {
        adapter = DeletedEntryAdapter(
            onItemClick = { entry ->
                if (isSelectionMode) {
                    toggleSelection(entry.id)
                } else {
                    showEntryPreview(entry)
                }
            },
            onItemLongClick = { entry ->
                if (!isSelectionMode) {
                    enterSelectionMode()
                    toggleSelection(entry.id)
                }
            },
            isItemSelected = { id -> selectedItems.contains(id) }
        )
        
        binding.rvDeletedEntries.layoutManager = LinearLayoutManager(this)
        binding.rvDeletedEntries.adapter = adapter
    }
    
    private fun setupObservers() {
        deletedViewModel.allDeletedEntries.observe(this) { entries ->
            adapter.submitList(entries)
            updateDaysRemaining(entries)
            
            if (entries.isEmpty()) {
                binding.emptyStateLayout.visibility = View.VISIBLE
                binding.rvDeletedEntries.visibility = View.GONE
                binding.btnSelect.visibility = View.GONE
            } else {
                binding.emptyStateLayout.visibility = View.GONE
                binding.rvDeletedEntries.visibility = View.VISIBLE
                binding.btnSelect.visibility = View.VISIBLE
            }
        }
    }
    
    private fun updateDaysRemaining(entries: List<DeletedEntry>) {
        if (entries.isEmpty()) return
        
        val oldestEntry = entries.maxByOrNull { it.deletedAt } ?: return
        val daysRemaining = 30 - TimeUnit.MILLISECONDS.toDays(
            System.currentTimeMillis() - oldestEntry.deletedAt
        )
        
        binding.tvDaysRemaining.text = "Quedan $daysRemaining días"
    }
    
    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            if (isSelectionMode) {
                exitSelectionMode()
            } else {
                finish()
            }
        }
        
        binding.btnSearch.setOnClickListener {
            // TODO: Implementar búsqueda
        }
        
        binding.btnSelect.setOnClickListener {
            if (isSelectionMode) {
                exitSelectionMode()
            } else {
                enterSelectionMode()
            }
        }
        
        binding.btnCloseSelection.setOnClickListener {
            exitSelectionMode()
        }
        
        binding.btnRestore.setOnClickListener {
            if (selectedItems.isNotEmpty()) {
                restoreSelected()
            }
        }
        
        binding.btnDeletePermanent.setOnClickListener {
            if (selectedItems.isNotEmpty()) {
                deleteSelectedPermanently()
            }
        }
    }
    
    private fun enterSelectionMode() {
        isSelectionMode = true
        binding.btnSelect.text = "Seleccionar"
        binding.selectionToolbar.visibility = View.VISIBLE
        binding.appBarLayout.visibility = View.GONE
        updateSelectionCount()
        adapter.notifyDataSetChanged()
    }
    
    private fun exitSelectionMode() {
        isSelectionMode = false
        selectedItems.clear()
        binding.btnSelect.text = "Seleccionar"
        binding.selectionToolbar.visibility = View.GONE
        binding.appBarLayout.visibility = View.VISIBLE
        adapter.notifyDataSetChanged()
    }
    
    private fun toggleSelection(id: Long) {
        if (selectedItems.contains(id)) {
            selectedItems.remove(id)
        } else {
            selectedItems.add(id)
        }
        updateSelectionCount()
        adapter.notifyDataSetChanged()
        
        if (selectedItems.isEmpty() && isSelectionMode) {
            exitSelectionMode()
        }
    }
    
    private fun updateSelectionCount() {
        binding.tvSelectionCount.text = "${selectedItems.size}"
    }
    
    private fun showEntryPreview(entry: DeletedEntry) {
        AlertDialog.Builder(this)
            .setTitle(entry.title.ifEmpty { "Sin título" })
            .setMessage(entry.content)
            .setPositiveButton("Restaurar") { _, _ ->
                restoreEntry(entry)
            }
            .setNegativeButton("Cerrar", null)
            .setNeutralButton("Eliminar permanentemente") { _, _ ->
                deletePermanently(entry)
            }
            .show()
    }
    
    private fun restoreEntry(entry: DeletedEntry) {
        lifecycleScope.launch {
            val restoredEntry = DiaryEntry(
                id = 0,
                title = entry.title,
                content = entry.content,
                date = entry.date,
                createdAt = entry.createdAt
            )
            diaryViewModel.insert(restoredEntry)
            deletedViewModel.delete(entry)
        }
    }
    
    private fun restoreSelected() {
        lifecycleScope.launch {
            val allEntries = deletedViewModel.allDeletedEntries.value ?: emptyList()
            val entriesToRestore = allEntries.filter { selectedItems.contains(it.id) }
            
            entriesToRestore.forEach { entry ->
                val restoredEntry = DiaryEntry(
                    id = 0,
                    title = entry.title,
                    content = entry.content,
                    date = entry.date,
                    createdAt = entry.createdAt
                )
                diaryViewModel.insert(restoredEntry)
                deletedViewModel.delete(entry)
            }
            
            exitSelectionMode()
        }
    }
    
    private fun deletePermanently(entry: DeletedEntry) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar permanentemente")
            .setMessage("Esta entrada se eliminará permanentemente y no se podrá recuperar.")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    deletedViewModel.delete(entry)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun deleteSelectedPermanently() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar permanentemente")
            .setMessage("Las ${selectedItems.size} entradas seleccionadas se eliminarán permanentemente y no se podrán recuperar.")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    val allEntries = deletedViewModel.allDeletedEntries.value ?: emptyList()
                    val entriesToDelete = allEntries.filter { selectedItems.contains(it.id) }
                    
                    entriesToDelete.forEach { entry ->
                        deletedViewModel.delete(entry)
                    }
                    
                    exitSelectionMode()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun cleanOldEntries() {
        lifecycleScope.launch {
            val thirtyDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)
            deletedViewModel.deleteOldEntries(thirtyDaysAgo)
        }
    }
}
