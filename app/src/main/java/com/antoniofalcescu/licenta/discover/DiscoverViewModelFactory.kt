package com.antoniofalcescu.licenta.discover

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.antoniofalcescu.licenta.profile.ProfileViewModel

class DiscoverViewModelFactory(private val application: Application): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiscoverViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DiscoverViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}