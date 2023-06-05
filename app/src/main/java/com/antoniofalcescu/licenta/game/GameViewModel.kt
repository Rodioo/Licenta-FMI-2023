package com.antoniofalcescu.licenta.game

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.antoniofalcescu.licenta.home.User
import com.antoniofalcescu.licenta.repository.*
import com.antoniofalcescu.licenta.repository.roomDatabase.LocalDatabase
import com.antoniofalcescu.licenta.repository.roomDatabase.accessToken.*
import com.antoniofalcescu.licenta.repository.roomDatabase.gameRoom.GameRoomCodeDao
import com.antoniofalcescu.licenta.repository.roomDatabase.gameRoom.getLastRoomCode
import com.antoniofalcescu.licenta.repository.roomDatabase.gameRoom.insertLastRoom
import com.antoniofalcescu.licenta.utils.EMPTY_PROFILE_IMAGE_URL
import kotlinx.coroutines.*

class GameViewModel(application: Application, gameRoomAux: GameRoom): AndroidViewModel(application) {

    private var firebase: Firebase

    private var viewModelJob: Job = Job()
    private var coroutineScope: CoroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _gameRoomDao = MutableLiveData<GameRoomCodeDao?>()
    val gameRoomDao: LiveData<GameRoomCodeDao?>
        get() = _gameRoomDao
    private val accessTokenDao: AccessTokenDao
    private var accessToken: AccessToken? = null

    private val _error = MutableLiveData<String>()
    val error: LiveData<String>
        get() = _error

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?>
        get() = _currentUser

    private val _userIds = MutableLiveData<List<String>>()
    val userIds: LiveData<List<String>>
        get() = _userIds

    private val _users = MutableLiveData<List<User?>>()
    val users: LiveData<List<User?>>
        get() = _users

    private val _gameRoom = MutableLiveData(gameRoomAux)
    val gameRoom: LiveData<GameRoom>
        get() = _gameRoom

    init {
        firebase = Firebase(application)

        _gameRoomDao.value = LocalDatabase.getInstance(application).gameRoomDao
        accessTokenDao = LocalDatabase.getInstance(application).accessTokenDao

        coroutineScope.launch {
            if (accessToken?.value == null) {
                accessToken = getAccessToken(accessTokenDao)
            }
            getCurrentUser()
            while(true) {
                getUsersFromRoom()
                delay(5000L)
            }
        }
    }

    fun getCurrentUser() {
        coroutineScope.launch {
            val response = GuessifyApi.retrofitService.getCurrentUserProfile("Bearer ${accessToken!!.value}")
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    Log.e("getCurrentUser_SUCCESS", response.body().toString())

                    val imageUrl = if (response.body()?.images?.size == 0) {
                        EMPTY_PROFILE_IMAGE_URL
                    } else {
                        response.body()?.images?.get(0)?.url ?: EMPTY_PROFILE_IMAGE_URL
                    }

                    val userAux = User(
                        id_spotify = response.body()!!.id,
                        token = accessToken!!.value!!,
                        name = response.body()!!.display_name,
                        image_url = imageUrl
                    )
                    _currentUser.value = userAux
                    updateToken(accessTokenDao, false)
                } else {
                    updateToken(accessTokenDao, true)
                    Log.e("getCurrentUserProfile_FAILURE", response.code().toString())
                    Log.e("getCurrentUserProfile_FAILURE", response.errorBody().toString())
                }
            }
        }
    }

    fun getUsersProfiles() {
        val usersAux = mutableListOf<User>()
        coroutineScope.launch {
            if (_userIds.value != null) {
                _userIds.value!!.map {idSpotify ->
                    val getUserDeferred = firebase.getUser(idSpotify)
                    try {
                        val getUserResult = getUserDeferred.await()
                        if (getUserResult == null) {
                            _error.value = getUserDeferred.getCompletionExceptionOrNull()?.message
                        } else {
                            usersAux.add(getUserResult)
                        }
                    } catch (exception: Exception) {
                        _error.value = exception.message
                    }

                }
            }
            _users.value = usersAux
        }
    }

    private fun getUsersFromRoom() {
        coroutineScope.launch {
            if (_gameRoom.value != null) {
                val getUsersFromRoomDeferred = firebase.getUsersFromRoom(_gameRoom.value!!.code)
                try {
                    val getUserFromRoomResult = getUsersFromRoomDeferred.await()
                    if (getUserFromRoomResult.isEmpty()) {
                        _error.value = getUsersFromRoomDeferred.getCompletionExceptionOrNull()?.message
                    } else {
                        _userIds.value = getUserFromRoomResult
                    }
                } catch (exception: Exception) {
                    _error.value = exception.message
                }
            }
        }
    }

    fun rejoinRoom() {
        coroutineScope.launch {
            var lastRoomCode: String? = null
            if (_gameRoomDao.value != null) {
                val lastRoom = getLastRoomCode(_gameRoomDao.value!!)
                lastRoomCode = lastRoom?.code
                Log.e("newRoom", lastRoomCode.toString())
            } else {
                Log.e("newRoom", "Game room DAO value is null")
            }
            if (_currentUser.value != null && lastRoomCode != null) {
                val joinRoomDeferred = firebase.addUserToRoom(lastRoomCode, _currentUser.value!!.id_spotify)
                try {
                    val joinRoomResult = joinRoomDeferred.await()
                    if (joinRoomResult == null) {
                        _error.value = joinRoomDeferred.getCompletionExceptionOrNull()?.message
                    } else {
                        _gameRoom.value = joinRoomResult
                    }
                } catch (exception: Exception) {
                    _error.value = exception.message
                }
            }
        }
    }

    fun leaveRoom() {
        coroutineScope.launch {
            if (_currentUser.value != null && _gameRoom.value != null) {
                val leaveRoomDeferred = firebase.removeUserFromRoom(_gameRoom.value!!.code, _currentUser.value!!.id_spotify)
                try {
                    val leaveRoomResult = leaveRoomDeferred.await()
                    if (leaveRoomResult == null) {
                        _error.value = leaveRoomDeferred.getCompletionExceptionOrNull()?.message
                    } else {
                        if (_gameRoomDao.value != null && _gameRoom.value != null) {
                            insertLastRoom(_gameRoomDao.value!!, _gameRoom.value!!.code)
                        }
                        _gameRoom.value = leaveRoomResult
                        _userIds.value = _gameRoom.value!!.users
                    }
                } catch (exception: Exception) {
                    _error.value = exception.message
                }
            }
        }
    }
}