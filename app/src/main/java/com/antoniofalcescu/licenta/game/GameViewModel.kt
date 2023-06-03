package com.antoniofalcescu.licenta.game

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.antoniofalcescu.licenta.home.User
import com.antoniofalcescu.licenta.repository.Firebase
import com.antoniofalcescu.licenta.repository.GuessifyApi
import com.antoniofalcescu.licenta.repository.accessToken.*
import com.antoniofalcescu.licenta.utils.EMPTY_PROFILE_IMAGE_URL
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlin.random.Random

class GameViewModel(application: Application, roomAux: Room): AndroidViewModel(application) {

    private var firebase: Firebase

    private var viewModelJob: Job = Job()
    private var coroutineScope: CoroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

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

    private val _room = MutableLiveData(roomAux)
    val room: LiveData<Room>
        get() = _room

    init {
        firebase = Firebase(application)

        accessTokenDao = AccessTokenDatabase.getInstance(application).accessTokenDao

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

    private fun getCurrentUser() {
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
                            _users.value = usersAux
                        }
                    } catch (exception: Exception) {
                        _error.value = exception.message
                    }

                }
            }
        }
    }

    private fun getUsersFromRoom() {
        coroutineScope.launch {
            if (_room.value != null) {
                val getUsersFromRoomDeferred = firebase.getUsersFromRoom(_room.value!!.code)
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

    fun leaveRoom() {
        coroutineScope.launch {
            if (_currentUser.value != null && _room.value != null) {
                val leaveRoomDeferred = firebase.removeUserFromRoom(_room.value!!.code, _currentUser.value!!.id_spotify)
                try {
                    val leaveRoomResult = leaveRoomDeferred.await()
                    if (leaveRoomResult == null) {
                        _error.value = leaveRoomDeferred.getCompletionExceptionOrNull()?.message
                    } else {
                        _room.value = leaveRoomResult
                        _userIds.value = _room.value!!.users
                    }
                } catch (exception: Exception) {
                    _error.value = exception.message
                }
            }
        }
    }
}