package com.antoniofalcescu.licenta.login

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.antoniofalcescu.licenta.repository.accessToken.AccessToken
import com.antoniofalcescu.licenta.repository.accessToken.AccessTokenDao
import com.antoniofalcescu.licenta.repository.accessToken.AccessTokenDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class LoginViewModel(application: Application): AndroidViewModel(application) {

    private val viewModelJob: Job = Job()
    private val dbScope: CoroutineScope = CoroutineScope(viewModelJob + Dispatchers.IO)

    private val accessTokenDao: AccessTokenDao

    init {
        val db = AccessTokenDatabase.getInstance(application)
        accessTokenDao = db.accessTokenDao
    }

    fun saveAccessToken(token: String) {
        dbScope.launch {
            val accessToken = AccessToken(value = token)
            accessTokenDao.save(accessToken)
        }
    }

}