package com.antoniofalcescu.licenta.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.antoniofalcescu.licenta.profile.Profile
import com.antoniofalcescu.licenta.profile.artists.Artist
import com.antoniofalcescu.licenta.repository.GuessifyApi
import com.antoniofalcescu.licenta.repository.accessToken.*
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*

class HomeViewModel(application: Application): AndroidViewModel(application) {

    private val firebase = FirebaseFirestore.getInstance()

    private var viewModelJob: Job = Job()
    private var coroutineScope: CoroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val accessTokenDao: AccessTokenDao
    private var accessToken: AccessToken? = null

    private val _error = MutableLiveData<String>()
    val error: LiveData<String>
        get() = _error

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?>
        get() = _user

    private val _genres = MutableLiveData<Genre>()
    val genres: LiveData<Genre>
        get() = _genres


    init {
        FirebaseApp.initializeApp(application)

        accessTokenDao = AccessTokenDatabase.getInstance(application).accessTokenDao

        coroutineScope.launch {
            if (accessToken?.value == null) {
                accessToken = getAccessToken(accessTokenDao)
            }
        }
    }

    fun getGameGenres() {
        coroutineScope.launch {
            val response = GuessifyApi.retrofitService.getGenres(
                "Bearer ${accessToken!!.value}",
            )
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    Log.e("getGenres_SUCCESS", response.body().toString())
                    val filteredCategories = response.body()?.categories?.items?.filter { item ->
                        val excludedNames = listOf("Party", "Cooking & Dining", "EQUAL", "Frequency")
                        !excludedNames.contains(item.name)
                    }

                    val filteredGenre = filteredCategories?.let {
                        Genre(items = it)
                    }?: Genre(items = emptyList())

                    _genres.value = filteredGenre
                } else {
                    Log.e("getGenres_FAILURE", response.code().toString())
                    Log.e("getGenres_FAILURE", response.errorBody().toString())
                }
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}