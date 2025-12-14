package com.example.myapplicationmyday.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.myapplicationmyday.data.DiaryDatabase
import com.example.myapplicationmyday.data.SocialMediaLink
import com.example.myapplicationmyday.data.SocialMediaRepository
import com.example.myapplicationmyday.data.SocialPlatform
import kotlinx.coroutines.launch

class SocialMediaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SocialMediaRepository
    private val _selectedPlatform = MutableLiveData<SocialPlatform?>(null)
    private val _userId = MutableLiveData<String>("")
    
    val allLinks: LiveData<List<SocialMediaLink>>
    val filteredLinks: LiveData<List<SocialMediaLink>>
    
    init {
        val socialMediaDao = DiaryDatabase.getDatabase(application).socialMediaDao()
        repository = SocialMediaRepository(socialMediaDao)
        allLinks = repository.getAllLinks()
        
        filteredLinks = _selectedPlatform.switchMap { platform ->
            val userId = _userId.value ?: ""
            if (platform == null) {
                if (userId.isNotEmpty()) {
                    repository.getLinksByUserId(userId)
                } else {
                    allLinks
                }
            } else {
                if (userId.isNotEmpty()) {
                    repository.getLinksByUserAndPlatform(userId, platform)
                } else {
                    repository.getLinksByPlatform(platform)
                }
            }
        }
    }
    
    fun setUserId(userId: String) {
        _userId.value = userId
    }
    
    fun setFilter(platform: SocialPlatform?) {
        _selectedPlatform.value = platform
    }
    
    fun insert(link: SocialMediaLink) = viewModelScope.launch {
        repository.insert(link)
    }
    
    fun update(link: SocialMediaLink) = viewModelScope.launch {
        repository.update(link)
    }
    
    fun delete(link: SocialMediaLink) = viewModelScope.launch {
        repository.delete(link)
    }
    
    fun syncFromFirestore(userId: String) = viewModelScope.launch {
        repository.syncFromFirestore(userId)
    }
}
