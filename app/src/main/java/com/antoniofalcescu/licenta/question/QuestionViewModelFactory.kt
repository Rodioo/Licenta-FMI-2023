package com.antoniofalcescu.licenta.question

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.antoniofalcescu.licenta.game.GameRoom
import com.antoniofalcescu.licenta.game.GameViewModel

class QuestionViewModelFactory(
    private val application: Application,
    private val gameRoom: GameRoom
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuestionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuestionViewModel(application, gameRoom) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}