package com.antoniofalcescu.licenta.repository.accessToken

import kotlinx.coroutines.*

private val viewModelJob = Job()
private val dbScope = CoroutineScope(viewModelJob + Dispatchers.IO)

suspend fun getAccessToken(accessTokenDao: AccessTokenDao): AccessToken {
    return withContext(dbScope.coroutineContext) {
        accessTokenDao.get()
    }
}

fun updateToken(accessTokenDao: AccessTokenDao, needsRefresh: Boolean) {
    dbScope.launch {
        accessTokenDao.updateRefresh(needsRefresh)
    }
}