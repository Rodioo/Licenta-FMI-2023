package com.antoniofalcescu.licenta.repository.roomDatabase.gameRoom

import android.util.Log
import kotlinx.coroutines.*

private val viewModelJob = Job()
private val dbScope = CoroutineScope(viewModelJob + Dispatchers.IO)

fun insertLastRoom(gameRoomCodeDao: GameRoomCodeDao, roomCode: String) {
    dbScope.launch {
        val gameRoomCode = GameRoomCode(code = roomCode)
        gameRoomCodeDao.save(gameRoomCode)
    }
}

suspend fun getLastRoomCode(gameRoomCodeDao: GameRoomCodeDao): GameRoomCode? {
    return withContext(dbScope.coroutineContext) {
        gameRoomCodeDao.get()
    }
}