package com.antoniofalcescu.licenta.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.antoniofalcescu.licenta.repository.accessToken.AccessToken
import com.antoniofalcescu.licenta.repository.accessToken.AccessTokenDao
import com.antoniofalcescu.licenta.repository.accessToken.AccessTokenDatabase
import kotlinx.coroutines.*

class HomeViewModel(application: Application): AndroidViewModel(application) {

    private var viewModelJob: Job = Job()
    private var coroutineScope: CoroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)
    private val dbScope: CoroutineScope = CoroutineScope(viewModelJob + Dispatchers.IO)

    private val accessTokenDao: AccessTokenDao
    private lateinit var accessToken: AccessToken

    init {
        accessTokenDao = AccessTokenDatabase.getInstance(application).accessTokenDao

        coroutineScope.launch {
            if (!(::accessToken.isInitialized) || accessToken.value == null) {
                accessToken = getAccessToken()
            }
        }
    }

    private suspend fun getAccessToken(): AccessToken {
        return withContext(dbScope.coroutineContext) {
            accessTokenDao.get()
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}