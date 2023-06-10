package com.antoniofalcescu.licenta

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.antoniofalcescu.licenta.repository.Firebase
import com.antoniofalcescu.licenta.repository.roomDatabase.accessToken.AccessToken
import com.antoniofalcescu.licenta.repository.roomDatabase.accessToken.AccessTokenDao
import com.antoniofalcescu.licenta.repository.roomDatabase.LocalDatabase
import kotlinx.coroutines.*

private const val TOKEN_REFRESH_TIMER_REQUEST = 5000L

class MainViewModel(application: Application): AndroidViewModel(application) {
    private var firebase: Firebase

    private var viewModelJob: Job = Job()
    private var coroutineScope: CoroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)
    private val dbScope: CoroutineScope = CoroutineScope(viewModelJob + Dispatchers.IO)

    private val accessTokenDao: AccessTokenDao
    private val _accessToken = MutableLiveData<AccessToken>()
    val accessToken: LiveData<AccessToken>
        get() = _accessToken

    init {
        firebase = Firebase(application)
        accessTokenDao = LocalDatabase.getInstance(application).accessTokenDao
        coroutineScope.launch {
            var token = getAccessToken()
            _accessToken.postValue(token)

            while(true) {
                token = getAccessToken()

                if (_accessToken.value?.needsRefresh == true) {
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

    fun saveAndGetAccessToken(token: String) {
        dbScope.launch {
            val accessToken = AccessToken(value = token)
            accessTokenDao.save(accessToken)
            withContext(Dispatchers.Main) {
                _accessToken.value = accessToken
            }
        }
    }

    fun deleteEmptyRooms() {
        firebase.deleteEmptyRooms()
    }
}