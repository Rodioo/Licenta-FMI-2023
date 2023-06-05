package com.antoniofalcescu.licenta.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.antoniofalcescu.licenta.repository.roomDatabase.accessToken.AccessToken
import com.antoniofalcescu.licenta.repository.roomDatabase.accessToken.AccessTokenDao
import com.antoniofalcescu.licenta.repository.roomDatabase.LocalDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class LoginViewModel(application: Application): AndroidViewModel(application) {

    private val viewModelJob: Job = Job()
    private val dbScope: CoroutineScope = CoroutineScope(viewModelJob + Dispatchers.IO)

    private val accessTokenDao: AccessTokenDao

    init {
        accessTokenDao = LocalDatabase.getInstance(application).accessTokenDao
    }

    fun saveAccessToken(token: String) {
        dbScope.launch {
            val accessToken = AccessToken(value = token)
            accessTokenDao.save(accessToken)
        }
    }

}