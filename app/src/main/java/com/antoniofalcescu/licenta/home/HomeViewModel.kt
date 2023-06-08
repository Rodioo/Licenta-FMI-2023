package com.antoniofalcescu.licenta.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.antoniofalcescu.licenta.game.GameRoom
import com.antoniofalcescu.licenta.repository.*
import com.antoniofalcescu.licenta.repository.roomDatabase.LocalDatabase
import com.antoniofalcescu.licenta.repository.roomDatabase.accessToken.*
import com.antoniofalcescu.licenta.utils.EMPTY_PROFILE_IMAGE_URL
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.random.Random

class HomeViewModel(application: Application): AndroidViewModel(application) {

    private var firebase: Firebase

    private var viewModelJob: Job = Job()
    private var coroutineScope: CoroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val accessTokenDao: AccessTokenDao
    private var accessToken: AccessToken? = null

    private val _error = MutableLiveData<String>()
    val error: LiveData<String>
        get() = _error

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?>
        get() = _user

    private val _genres = MutableLiveData<Genre>()
    val genres: LiveData<Genre>
        get() = _genres

    private val _gameRoom = MutableLiveData<GameRoom?>()
    val gameRoom: LiveData<GameRoom?>
        get() = _gameRoom


    init {
        firebase = Firebase(application)

        accessTokenDao = LocalDatabase.getInstance(application).accessTokenDao

        coroutineScope.launch {
            if (accessToken?.value == null) {
                accessToken = getAccessToken(accessTokenDao)
            }
            getCurrentUser()
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
                    _user.value = userAux

                    updateToken(accessTokenDao, false)
                } else {
                    updateToken(accessTokenDao, true)
                    Log.e("getCurrentUserProfile_FAILURE", response.code().toString())
                    Log.e("getCurrentUserProfile_FAILURE", response.errorBody().toString())
                }
            }
        }
    }

    fun addUser() {
        coroutineScope.launch {
            if (_user.value != null) {
                val addUserDeferred = firebase.addUser(_user.value!!)
                try {
                    val addUserResult = addUserDeferred.await()
                    if (!addUserResult) {
                        _error.value = addUserDeferred.getCompletionExceptionOrNull()?.message
                    }
                } catch (exception: Exception) {
                    _error.value = exception.message
                }
            }
        }
    }

    private suspend fun deleteUserFromAllRooms(): Boolean = suspendCoroutine {continuation ->
        coroutineScope.launch {
            if (_user.value != null) {
                val deleteUserDeferred = firebase.deleteUserFromAllRooms(_user.value!!.id_spotify)
                try {
                    val deleteUserResult = deleteUserDeferred.await()
                    if (!deleteUserResult) {
                        continuation.resumeWith(Result.failure(Exception(
                            deleteUserDeferred.getCompletionExceptionOrNull()?.message)
                        ))
                    } else {
                        continuation.resume(true)
                    }
                } catch (exception: Exception) {
                    continuation.resumeWith(Result.failure(exception))
                }
            } else {
                continuation.resumeWith(Result.failure(Exception("User is null!")))
            }
        }
    }

    fun getGameGenres() {
        coroutineScope.launch {
            val response = GuessifyApi.retrofitService.getGenres(
                "Bearer ${accessToken!!.value}",
            )
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    Log.e("getGenres_SUCCESS", response.body().toString())
                    val filteredCategories = response.body()?.categories?.items?.filter { item ->
                        val excludedNames = listOf("Party", "Cooking & Dining", "EQUAL", "Frequency")
                        !excludedNames.contains(item.name)
                    }

                    val filteredGenre = filteredCategories?.let {
                        Genre(items = it)
                    }?: Genre(items = emptyList())

                    _genres.value = filteredGenre
                } else {
                    Log.e("getGenres_FAILURE", response.code().toString())
                    Log.e("getGenres_FAILURE", response.errorBody().toString())
                }
            }
        }
    }

    private fun generateRoomCode(existingCodes: Set<String>): String {
        val random = Random
        val stringBuilder = StringBuilder()
        var randomString: String
        do {
            stringBuilder.clear()

            repeat(4) {
                val digit = random.nextInt(10)
                stringBuilder.append(digit)
            }

            randomString = stringBuilder.toString()
        } while (existingCodes.contains(randomString))

        return randomString
    }

    fun createRoom(gameMode: String) {
        coroutineScope.launch {
            if (_user.value != null) {
                try {
                    deleteUserFromAllRooms()
                    val getUsedCodesDeferred = firebase.getUsedRoomCodes()
                    try {
                        val getUsedCodesResult = getUsedCodesDeferred.await()
                        if (getUsedCodesResult.isEmpty()) {
                            _error.value = getUsedCodesDeferred.getCompletionExceptionOrNull()?.message
                        } else {

                            val answers: HashMap<String, HashMap<String, Int>> = hashMapOf(
                                _user.value!!.id_spotify to HashMap()
                            )
                            val totalPoints: HashMap<String, Int> = hashMapOf(
                                _user.value!!.id_spotify to 0
                            )

                            val gameRoom = GameRoom(
                                generateRoomCode(getUsedCodesResult),
                                hasStarted = false,
                                doneLoading = false,
                                gameMode,
                                mutableListOf(_user.value?.id_spotify).filterNotNull(),
                                emptyList(),
                                answers,
                                totalPoints
                            )

                            val addRoomDeferred = firebase.addRoom(gameRoom)
                            try {
                                val addRoomResult = addRoomDeferred.await()
                                if (!addRoomResult) {
                                    _error.value = addRoomDeferred.getCompletionExceptionOrNull()?.message
                                } else {
                                    _gameRoom.value = gameRoom
                                }
                            } catch (exception: Exception) {
                                _error.value = exception.message
                            }
                        }
                    } catch (exception: Exception) {
                        _error.value = exception.message
                    }
                } catch (exception: Exception) {
                    _error.value = exception.message
                }
            }
        }
    }

    fun joinRoom(roomCode: String) {
        coroutineScope.launch {
            if (_user.value != null) {
                try {
                    deleteUserFromAllRooms()
                    val joinRoomDeferred = firebase.addUserToRoom(roomCode, _user.value!!.id_spotify)
                    try {
                        val joinRoomResult = joinRoomDeferred.await()
                        if (joinRoomResult == null) {
                            _error.value = "The room does not exist"

                        } else {
                            _gameRoom.value = joinRoomResult
                        }
                    } catch (exception: Exception) {
                        _error.value = exception.message
                    }
                } catch (exception: Exception) {
                    _error.value = exception.message
                }
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}