package com.example.myapplicationmyday

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplicationmyday.adapter.DiaryAdapter
import com.example.myapplicationmyday.adapter.toItemsWithHeaders
import com.example.myapplicationmyday.data.DeletedEntry
import com.example.myapplicationmyday.data.DiaryEntry
import com.example.myapplicationmyday.databinding.ActivityMainBinding
import com.example.myapplicationmyday.viewmodel.DeletedEntryViewModel
import com.example.myapplicationmyday.viewmodel.DiaryViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val viewModel: DiaryViewModel by viewModels()
    private val deletedViewModel: DeletedEntryViewModel by viewModels()
    private lateinit var adapter: DiaryAdapter
    private lateinit var auth: FirebaseAuth
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        auth = Firebase.auth
        
        // Sync from Firestore
        auth.currentUser?.uid?.let { userId ->
            viewModel.syncFromFirestore(userId)
        }
        
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }
    
    private fun setupRecyclerView() {
        adapter = DiaryAdapter(
            onItemClick = { entry ->
                // Abrir la pantalla de edición/vista de entrada
                val intent = Intent(this, AddEditEntryActivity::class.java).apply {
                    putExtra("ENTRY_ID", entry.id)
                }
                startActivity(intent)
            },
            onMenuClick = { entry, view ->
                showEntryMenu(entry, view)
            }
        )
        
        binding.rvDiaryEntries.layoutManager = LinearLayoutManager(this)
        binding.rvDiaryEntries.adapter = adapter
    }
    
    private fun setupObservers() {
        viewModel.allEntries.observe(this) { entries ->
            // Convertir las entradas a items con headers
            val items = entries.toItemsWithHeaders()
            adapter.submitList(items)
            
            // Mostrar/ocultar el estado vacío
            if (entries.isEmpty()) {
                binding.emptyStateLayout.visibility = View.VISIBLE
                binding.rvDiaryEntries.visibility = View.GONE
            } else {
                binding.emptyStateLayout.visibility = View.GONE
                binding.rvDiaryEntries.visibility = View.VISIBLE
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.fabAddEntry.setOnClickListener {
            val intent = Intent(this, AddEditEntryActivity::class.java)
            startActivity(intent)
        }
        
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        binding.btnSearch.setOnClickListener {
            // TODO: Implementar búsqueda
        }
        
        binding.btnMore.setOnClickListener {
            // TODO: Mostrar menú de opciones
        }
    }
    
    private fun showEntryMenu(entry: DiaryEntry, view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.entry_menu, popup.menu)
        
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit -> {
                    val intent = Intent(this, AddEditEntryActivity::class.java).apply {
                        putExtra("ENTRY_ID", entry.id)
                    }
                    startActivity(intent)
                    true
                }
                R.id.action_delete -> {
                    showDeleteConfirmation(entry)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
    
    private fun showDeleteConfirmation(entry: DiaryEntry) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar entrada")
            .setMessage("¿Estás seguro de que quieres eliminar esta entrada?")
            .setPositiveButton("Eliminar") { _, _ ->
                moveToTrash(entry)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun moveToTrash(entry: DiaryEntry) {
        lifecycleScope.launch {
            // Crear entrada eliminada
            val deletedEntry = DeletedEntry(
                originalId = entry.id,
                title = entry.title,
                content = entry.content,
                date = entry.date,
                createdAt = entry.createdAt,
                deletedAt = System.currentTimeMillis()
            )
            
            // Guardar en papelera y eliminar del diario
            deletedViewModel.insert(deletedEntry)
            viewModel.delete(entry)
        }
    }
}