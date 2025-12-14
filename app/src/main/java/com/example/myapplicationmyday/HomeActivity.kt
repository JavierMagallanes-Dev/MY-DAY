package com.example.myapplicationmyday

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplicationmyday.data.DiaryEntry
import com.example.myapplicationmyday.databinding.ActivityHomeBinding
import com.example.myapplicationmyday.viewmodel.DiaryViewModel
import com.example.myapplicationmyday.viewmodel.SocialMediaViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class HomeActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityHomeBinding
    private val viewModel: DiaryViewModel by viewModels()
    private val socialMediaViewModel: SocialMediaViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize Firebase Auth
        auth = Firebase.auth
        
        // Check if user is signed in
        if (auth.currentUser == null) {
            navigateToLogin()
            return
        }
        
        // Sync diaries from Firestore
        syncDiariesFromCloud()
        
        setupObservers()
        setupClickListeners()
    }
    
    private fun syncDiariesFromCloud() {
        val userId = auth.currentUser?.uid ?: return
        viewModel.syncFromFirestore(userId)
        socialMediaViewModel.setUserId(userId)
        socialMediaViewModel.syncFromFirestore(userId)
    }
    
    override fun onResume() {
        super.onResume()
        // Refrescar estadísticas cuando volvemos a esta pantalla
        viewModel.allEntries.value?.let { entries ->
            updateStatistics(entries)
        }
        // Actualizar contador de social media
        socialMediaViewModel.allLinks.value?.let { links ->
            updateSocialMediaCount(links.size)
        }
    }
    
    private fun setupObservers() {
        viewModel.allEntries.observe(this) { entries ->
            updateStatistics(entries)
        }
        
        socialMediaViewModel.allLinks.observe(this) { links ->
            updateSocialMediaCount(links.size)
        }
    }
    
    private fun updateSocialMediaCount(count: Int) {
        binding.layoutSocialMedia.findViewById<android.widget.TextView>(R.id.tvSocialMediaCount)?.text = count.toString()
        // También actualizar el contador en estadísticas
        binding.tvStreakDays.text = count.toString()
    }
    
    private fun updateStatistics(entries: List<DiaryEntry>) {
        // Actualizar estadísticas
        val entryCount = entries.size
        binding.tvEntryCount.text = entryCount.toString()
        
        // Contar palabras totales
        val totalWords = entries.sumOf { entry ->
            (entry.title.split("\\s+".toRegex()).filter { it.isNotEmpty() }.size + 
             entry.content.split("\\s+".toRegex()).filter { it.isNotEmpty() }.size)
        }
        binding.tvTotalWords.text = totalWords.toString()
        
        // Actualizar contadores de diarios
        binding.tvDiaryCount.text = entryCount.toString()
        
        // Actualizar texto descriptivo
        binding.tvEntryLabel.text = if (entryCount == 1) "entrada este año" else "entradas este año"
    }
    
    private fun setupClickListeners() {
        binding.btnMore.setOnClickListener {
            openProfile()
        }
        
        // Click en tarjeta de estadísticas
        binding.cardStatistics.setOnClickListener {
            val intent = Intent(this, StatisticsActivity::class.java)
            startActivity(intent)
        }
        
        // Click en tarjeta de lugares
        binding.cardPlaces.setOnClickListener {
            // TODO: Abrir pantalla de lugares
        }
        
        // Click en "Diario"
        binding.layoutDiary.setOnClickListener {
            openDiaryScreen()
        }
        
        // Click en "Social Media"
        binding.layoutSocialMedia.setOnClickListener {
            val intent = Intent(this, SocialMediaActivity::class.java)
            startActivity(intent)
        }
        
        // Click en "Eliminadas recientemente"
        binding.layoutDeleted.setOnClickListener {
            val intent = Intent(this, DeletedEntriesActivity::class.java)
            startActivity(intent)
        }
        
        // Botón para crear nuevo diario
        binding.btnAddDiary.setOnClickListener {
            // TODO: Mostrar diálogo para crear nuevo diario
        }
    }
    
    private fun openProfile() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }
    
    private fun showSignOutDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.sign_out))
            .setMessage(getString(R.string.sign_out_confirm))
            .setPositiveButton(getString(R.string.sign_out)) { _, _ ->
                signOut()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun signOut() {
        auth.signOut()
        Toast.makeText(this, getString(R.string.sign_out_success), Toast.LENGTH_SHORT).show()
        navigateToLogin()
    }
    
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    private fun openDiaryScreen() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}
