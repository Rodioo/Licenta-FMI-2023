package com.antoniofalcescu.licenta.game

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.antoniofalcescu.licenta.home.User
import com.antoniofalcescu.licenta.repository.GuessifyApi
import com.antoniofalcescu.licenta.repository.accessToken.*
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlin.random.Random

class GameViewModel(application: Application, val gameMode: String): AndroidViewModel(application) {

    private val firebase = FirebaseFirestore.getInstance()

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

    private val _users = MutableLiveData<List<User?>>()
    val users: LiveData<List<User?>>
        get() = _users

    private val usedRoomCodes = mutableSetOf<String>()

    private val _roomCode = MutableLiveData<String>()
    val roomCode: LiveData<String>
        get() = _roomCode

    init {
        FirebaseApp.initializeApp(application)

        accessTokenDao = AccessTokenDatabase.getInstance(application).accessTokenDao

        coroutineScope.launch {
            if (accessToken?.value == null) {
                accessToken = getAccessToken(accessTokenDao)
            }
            getCurrentUser()
            getUsedCodes()
        }
        Log.e("viewModel", gameMode)
    }

    private fun getCurrentUser() {
        coroutineScope.launch {
            Log.e("request", accessToken!!.value.toString())
            val response = GuessifyApi.retrofitService.getCurrentUserProfile("Bearer ${accessToken!!.value}")
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    Log.e("getCurrentUser_SUCCESS", response.body().toString())
                    val userAux = User(
                        id_spotify = response.body()!!.id,
                        token = accessToken!!.value!!,
                        name = response.body()!!.display_name,
                        image_url = response.body()!!.images[0].url,
                    )
                    _currentUser.value = userAux

                    val currentUsers = _users.value?.toMutableList() ?: mutableListOf()
                    currentUsers.add(userAux)

                    _users.postValue(currentUsers)
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
        Log.e("user", _currentUser.value.toString())
        if (_currentUser.value != null) {
            firebase.collection("users").add(_currentUser.value!!)
                .addOnSuccessListener {
                    Log.i("addedUser", _currentUser.value.toString())
                }
                .addOnFailureListener {exception ->
                    Log.e("HomeViewModel",
                        "Failed to add user: ${_currentUser.value!!.name}: ${exception.message}"
                    )
                    _error.value = exception.message
                }
        }
    }

    fun deleteUser() {
        Log.e("delete", _currentUser.value.toString())
        if (_currentUser.value != null) {
            firebase.collection("users").whereEqualTo("id_spotify", _currentUser.value!!.id_spotify).get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        document.reference.delete()
                            .addOnSuccessListener {
                                Log.i("HomeViewModel",
                                    "User: ${_currentUser.value?.name} deleted successfully"
                                )
                            }
                            .addOnFailureListener { exception ->
                                _error.value = exception.message
                                Log.i("HomeViewModel",
                                    "Failed to delete user ${_currentUser.value?.name}: ${exception.message}"
                                )
                            }
                    }
                }
        }
    }

    private fun getUsedCodes() {
        firebase.collection("rooms").get()
            .addOnSuccessListener {querySnapshot ->
                for (document in querySnapshot.documents) {
                    val code = document.getString("code")
                    if (code != null) {
                        usedRoomCodes.add(code)
                    }
                }
                generateRoomCode(usedRoomCodes)
            }
            .addOnFailureListener {exception ->
                _error.value = exception.message
            }
    }

    private fun generateRoomCode(existingCodes: Set<String>) {
        val random = Random
        val stringBuilder = StringBuilder()
        var randomString: String
        Log.e("viewModel", existingCodes.toString())
        do {
            stringBuilder.clear()

            repeat(4) {
                val digit = random.nextInt(10)
                stringBuilder.append(digit)
            }

            randomString = stringBuilder.toString()
        } while (existingCodes.contains(randomString))

        _roomCode.value = randomString
    }
}