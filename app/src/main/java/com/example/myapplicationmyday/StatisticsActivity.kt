package com.example.myapplicationmyday

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplicationmyday.databinding.ActivityStatisticsBinding
import com.example.myapplicationmyday.viewmodel.DiaryViewModel
import com.example.myapplicationmyday.viewmodel.SocialMediaViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class StatisticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatisticsBinding
    private val diaryViewModel: DiaryViewModel by viewModels()
    private val socialMediaViewModel: SocialMediaViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    
    private var selectedStartDate: Long = 0
    private var selectedEndDate: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        setupClickListeners()
        setupObservers()
        loadData()
    }

    private fun loadData() {
        val userId = auth.currentUser?.uid ?: return
        diaryViewModel.syncFromFirestore(userId)
        socialMediaViewModel.setUserId(userId)
        socialMediaViewModel.syncFromFirestore(userId)
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.chipAllTime.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedStartDate = 0
                selectedEndDate = System.currentTimeMillis()
                updateStatistics()
            }
        }

        binding.chipThisMonth.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                selectedStartDate = calendar.timeInMillis
                selectedEndDate = System.currentTimeMillis()
                updateStatistics()
            }
        }

        binding.chipThisWeek.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                selectedStartDate = calendar.timeInMillis
                selectedEndDate = System.currentTimeMillis()
                updateStatistics()
            }
        }

        binding.btnShowCalendar.setOnClickListener {
            binding.calendarView.visibility = if (binding.calendarView.visibility == View.VISIBLE) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth, 0, 0, 0)
            selectedStartDate = calendar.timeInMillis
            
            calendar.set(year, month, dayOfMonth, 23, 59, 59)
            selectedEndDate = calendar.timeInMillis
            
            binding.chipAllTime.isChecked = false
            binding.chipThisMonth.isChecked = false
            binding.chipThisWeek.isChecked = false
            
            updateStatistics()
        }
    }

    private fun setupObservers() {
        diaryViewModel.allEntries.observe(this) { entries ->
            updateStatistics()
        }

        socialMediaViewModel.allLinks.observe(this) { links ->
            updateStatistics()
        }
    }

    private fun updateStatistics() {
        val entries = diaryViewModel.allEntries.value ?: emptyList()
        val links = socialMediaViewModel.allLinks.value ?: emptyList()

        // Filtrar por fecha si se seleccionó
        val filteredEntries = if (selectedStartDate > 0) {
            entries.filter { it.date in selectedStartDate..selectedEndDate }
        } else {
            entries
        }

        val filteredLinks = if (selectedStartDate > 0) {
            links.filter { it.createdAt in selectedStartDate..selectedEndDate }
        } else {
            links
        }

        // Total de entradas
        binding.tvTotalEntries.text = filteredEntries.size.toString()

        // Total de videos/links
        binding.tvTotalVideos.text = filteredLinks.size.toString()

        // Total de palabras
        val totalWords = filteredEntries.sumOf { entry ->
            (entry.title.split("\\s+".toRegex()).filter { it.isNotEmpty() }.size +
                    entry.content.split("\\s+".toRegex()).filter { it.isNotEmpty() }.size)
        }
        binding.tvTotalWords.text = totalWords.toString()

        // Promedio de palabras por entrada
        val avgWords = if (filteredEntries.isNotEmpty()) totalWords / filteredEntries.size else 0
        binding.tvAvgWords.text = avgWords.toString()

        // Días activos (días con al menos una entrada)
        val activeDays = filteredEntries.map { 
            it.date / (24 * 60 * 60 * 1000) 
        }.distinct().size
        binding.tvActiveDays.text = activeDays.toString()

        // Distribución por plataforma
        val facebookCount = filteredLinks.count { it.platform.name == "FACEBOOK" }
        val instagramCount = filteredLinks.count { it.platform.name == "INSTAGRAM" }
        val tiktokCount = filteredLinks.count { it.platform.name == "TIKTOK" }
        val twitterCount = filteredLinks.count { it.platform.name == "TWITTER" }
        val youtubeCount = filteredLinks.count { it.platform.name == "YOUTUBE" }

        binding.tvFacebookCount.text = facebookCount.toString()
        binding.tvInstagramCount.text = instagramCount.toString()
        binding.tvTiktokCount.text = tiktokCount.toString()
        binding.tvTwitterCount.text = twitterCount.toString()
        binding.tvYoutubeCount.text = youtubeCount.toString()

        // Porcentajes de plataforma
        val total = filteredLinks.size
        if (total > 0) {
            binding.progressFacebook.progress = (facebookCount * 100) / total
            binding.progressInstagram.progress = (instagramCount * 100) / total
            binding.progressTiktok.progress = (tiktokCount * 100) / total
            binding.progressTwitter.progress = (twitterCount * 100) / total
            binding.progressYoutube.progress = (youtubeCount * 100) / total
        }

        // Entradas por mes (últimos 6 meses)
        updateMonthlyChart(entries)

        // Mostrar mensaje si hay filtro activo
        if (selectedStartDate > 0) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            binding.tvFilterInfo.text = "Filtrando desde ${dateFormat.format(Date(selectedStartDate))}"
            binding.tvFilterInfo.visibility = View.VISIBLE
        } else {
            binding.tvFilterInfo.visibility = View.GONE
        }
    }

    private fun updateMonthlyChart(entries: List<com.example.myapplicationmyday.data.DiaryEntry>) {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        val monthlyCounts = mutableMapOf<String, Int>()
        
        // Últimos 6 meses
        for (i in 5 downTo 0) {
            calendar.set(Calendar.MONTH, currentMonth - i)
            calendar.set(Calendar.YEAR, currentYear)
            val monthKey = SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time)
            monthlyCounts[monthKey] = 0
        }

        // Contar entradas por mes
        entries.forEach { entry ->
            calendar.timeInMillis = entry.date
            val monthKey = SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time)
            if (monthlyCounts.containsKey(monthKey)) {
                monthlyCounts[monthKey] = (monthlyCounts[monthKey] ?: 0) + 1
            }
        }

        // Actualizar barras del gráfico
        val maxCount = monthlyCounts.values.maxOrNull() ?: 1
        val months = monthlyCounts.keys.toList()
        
        if (months.size >= 6) {
            binding.tvMonth1.text = months[0]
            binding.tvMonth2.text = months[1]
            binding.tvMonth3.text = months[2]
            binding.tvMonth4.text = months[3]
            binding.tvMonth5.text = months[4]
            binding.tvMonth6.text = months[5]

            val heights = monthlyCounts.values.map { count ->
                if (maxCount > 0) (count.toFloat() / maxCount * 200).toInt() else 0
            }

            binding.barMonth1.layoutParams.height = heights[0]
            binding.barMonth2.layoutParams.height = heights[1]
            binding.barMonth3.layoutParams.height = heights[2]
            binding.barMonth4.layoutParams.height = heights[3]
            binding.barMonth5.layoutParams.height = heights[4]
            binding.barMonth6.layoutParams.height = heights[5]

            binding.barMonth1.requestLayout()
            binding.barMonth2.requestLayout()
            binding.barMonth3.requestLayout()
            binding.barMonth4.requestLayout()
            binding.barMonth5.requestLayout()
            binding.barMonth6.requestLayout()
        }
    }
}
