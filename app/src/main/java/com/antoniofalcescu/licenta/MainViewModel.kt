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

        if (_accessToken.value == null) {
            coroutineScope.launch {
                val token = getAccessToken()
                _accessToken.postValue(token)
            }
        }
    }

    private suspend fun getAccessToken(): AccessToken {
        return withContext(dbScope.coroutineContext) {
            accessTokenDao.get()
        }
    }

    fun restoreAccessToken(accessToken: AccessToken?) {
        _accessToken.value = accessToken!!
    }

    fun saveAccessToken(token: String) {
        dbScope.launch {
            val accessToken = AccessToken(value = token)
            accessTokenDao.save(accessToken)
            withContext(Dispatchers.Main) {
                _accessToken.value = accessToken
            }
        }
    }
}