package com.example.myapplicationmyday

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplicationmyday.data.DiaryDatabase
import com.example.myapplicationmyday.data.DiaryEntry
import com.example.myapplicationmyday.data.DiaryRepository
import com.example.myapplicationmyday.databinding.ActivityAddEditEntryBinding
import com.example.myapplicationmyday.viewmodel.DiaryViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddEditEntryActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAddEditEntryBinding
    private val viewModel: DiaryViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private var entryId: Long = -1L
    private var currentEntry: DiaryEntry? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        auth = Firebase.auth
        entryId = intent.getLongExtra("ENTRY_ID", -1L)
        
        setupUI()
        setupClickListeners()
        
        if (entryId != -1L) {
            loadEntry()
        } else {
            setCurrentDate()
        }
    }
    
    private fun setupUI() {
        // Configurar la fecha actual
        setCurrentDate()
    }
    
    private fun setCurrentDate() {
        val dateFormat = SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-ES"))
        binding.tvDate.text = dateFormat.format(Date())
    }
    
    private fun loadEntry() {
        lifecycleScope.launch {
            val repository = DiaryRepository(DiaryDatabase.getDatabase(applicationContext).diaryDao())
            currentEntry = repository.getEntryById(entryId)
            currentEntry?.let { entry ->
                binding.etTitle.setText(entry.title)
                binding.etContent.setText(entry.content)
                
                val dateFormat = SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-ES"))
                binding.tvDate.text = dateFormat.format(Date(entry.date))
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        binding.btnSave.setOnClickListener {
            saveEntry()
        }
    }
    
    private fun saveEntry() {
        val title = binding.etTitle.text.toString()
        val content = binding.etContent.text.toString()
        
        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, "Por favor escribe algo", Toast.LENGTH_SHORT).show()
            return
        }
        
        val entry = if (entryId != -1L && currentEntry != null) {
            // Actualizar entrada existente
            currentEntry!!.copy(
                title = title,
                content = content
            )
        } else {
            // Crear nueva entrada
            val userId = auth.currentUser?.uid ?: ""
            DiaryEntry(
                title = title,
                content = content,
                date = System.currentTimeMillis(),
                userId = userId,
                firestoreId = ""
            )
        }
        
        if (entryId != -1L) {
            viewModel.update(entry)
        } else {
            viewModel.insert(entry)
        }
        
        finish()
    }
}
