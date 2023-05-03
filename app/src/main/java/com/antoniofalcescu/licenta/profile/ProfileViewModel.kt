package com.antoniofalcescu.licenta.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.antoniofalcescu.licenta.repository.GuessifyApi
import kotlinx.coroutines.*

class ProfileViewModel(val accessToken: String): ViewModel() {

    private var viewModelJob: Job = Job()
    private var coroutineScope: CoroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _profile = MutableLiveData<Profile>()
    val profile: LiveData<Profile>
        get() = _profile

    init {
        getCurrentUserProfile()
    }

    private fun getCurrentUserProfile() {
        coroutineScope.launch {
            val response = GuessifyApi.retrofitService.getCurrentUserProfile("Bearer $accessToken")
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    Log.e("getCurrentUserProfile", response.body().toString())
                    _profile.value = response.body()
                } else {
                    Log.e("getCurrentUserProfile", response.errorBody().toString())
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}