package com.antoniofalcescu.licenta

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.antoniofalcescu.licenta.login.LoginFragment
import com.antoniofalcescu.licenta.profile.Profile
import com.antoniofalcescu.licenta.repository.GuessifyApi
import com.antoniofalcescu.licenta.repository.accessToken.AccessToken
import com.antoniofalcescu.licenta.repository.accessToken.AccessTokenDao
import com.antoniofalcescu.licenta.repository.accessToken.AccessTokenDatabase
import kotlinx.coroutines.*

private const val TOKEN_REFRESH_TIMER_REQUEST = 2000L

class MainViewModel(application: Application): AndroidViewModel(application) {

    private var viewModelJob: Job = Job()
    private var coroutineScope: CoroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)
    private val dbScope: CoroutineScope = CoroutineScope(viewModelJob + Dispatchers.IO)

    private val accessTokenDao: AccessTokenDao
    private val _accessToken = MutableLiveData<AccessToken>()
    val accessToken: LiveData<AccessToken>
        get() = _accessToken

    init {
        accessTokenDao = AccessTokenDatabase.getInstance(application).accessTokenDao
        coroutineScope.launch {
            var token = getAccessToken()
            _accessToken.postValue(token)

            while(true) {
                Log.e("MainViewModel", "Checking token: $token")
                if (_accessToken.value?.needsRefresh == true) {
                    token = getAccessToken()
                    _accessToken.postValue(token)
                }
                delay(TOKEN_REFRESH_TIMER_REQUEST)
            }
        }
    }

    private suspend fun getAccessToken(): AccessToken {
        return withContext(dbScope.coroutineContext) {
            accessTokenDao.get()
        }
    }

    fun saveAccessToken(token: String) {
        dbScope.launch {
            val accessToken = AccessToken(value = token)
            accessTokenDao.save(accessToken)
        }
    }
}