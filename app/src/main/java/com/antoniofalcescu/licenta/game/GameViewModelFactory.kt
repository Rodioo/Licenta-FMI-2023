package com.antoniofalcescu.licenta.game

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.antoniofalcescu.licenta.MainViewModel

class GameViewModelFactory(
    private val application: Application,
    private val room: Room
    ): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(application, room) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}