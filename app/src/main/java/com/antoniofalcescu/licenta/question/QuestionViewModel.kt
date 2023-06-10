package com.antoniofalcescu.licenta.question

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.antoniofalcescu.licenta.R
import com.antoniofalcescu.licenta.game.GameRoom
import com.antoniofalcescu.licenta.home.User
import com.antoniofalcescu.licenta.repository.Firebase
import com.antoniofalcescu.licenta.repository.GuessifyApi
import com.antoniofalcescu.licenta.repository.roomDatabase.LocalDatabase
import com.antoniofalcescu.licenta.repository.roomDatabase.accessToken.AccessToken
import com.antoniofalcescu.licenta.repository.roomDatabase.accessToken.AccessTokenDao
import com.antoniofalcescu.licenta.repository.roomDatabase.accessToken.getAccessToken
import com.antoniofalcescu.licenta.repository.roomDatabase.accessToken.updateToken
import com.antoniofalcescu.licenta.utils.EMPTY_PROFILE_IMAGE_URL
import kotlinx.coroutines.*

class QuestionViewModel(private val application: Application, gameRoomAux: GameRoom): AndroidViewModel(application) {

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

    private val _users = MutableLiveData<List<User?>>()
    val users: LiveData<List<User?>>
        get() = _users

    private val _gameRoom = MutableLiveData(gameRoomAux)
    val gameRoom: LiveData<GameRoom>
        get() = _gameRoom

    private val _gameQuestions = MutableLiveData<GameQuestions>()
    val gameQuestions: LiveData<GameQuestions>
        get() = _gameQuestions

    private val _currentQuestion = MutableLiveData<Question>()
    val currentQuestion: LiveData<Question>
        get() = _currentQuestion

    private val _leaderboardZipped = MutableLiveData<List<Triple<User?, GameRoom, Question>>>()
    val leaderboardZipped: LiveData<List<Triple<User?, GameRoom, Question>>>
        get() = _leaderboardZipped

    private val _questionsProfileZipped = MutableLiveData<List<Pair<String, String>>>()
    val questionsProfileZipped: LiveData<List<Pair<String, String>>>
        get() = _questionsProfileZipped

    private val _userHasAnswered = MutableLiveData(false)
    val userHasAnswered: LiveData<Boolean>
        get() = _userHasAnswered

    private val _everybodyAnswered = MutableLiveData(false)
    val everybodyAnswered: LiveData<Boolean>
        get() = _everybodyAnswered

    init {
        firebase = Firebase(application)

        accessTokenDao = LocalDatabase.getInstance(application).accessTokenDao

        coroutineScope.launch {
            if (accessToken?.value == null) {
                accessToken = getAccessToken(accessTokenDao)
            }

            _currentUser.value = getCurrentUserAsync().await()

            if (_currentUser.value?.id_spotify == _gameRoom.value?.users?.get(0)) {
                insertQuestions()
            }

            getUsersProfiles()

            if (_gameQuestions.value != null) {
                getQuestionsProfileZipped(_gameQuestions.value!!.questions[0]) {}
            }

            while(true) {
                getRoom()
                delay(200L)
            }
        }
    }

    fun syncQuestion(question: Question) {
        _currentQuestion.value = question
        _userHasAnswered.value = false
        _everybodyAnswered.value = false
    }

    private fun getCurrentUserAsync(): Deferred<User?> = coroutineScope.async {
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
                updateToken(accessTokenDao, false)
                return@withContext userAux
            } else {
                updateToken(accessTokenDao, true)
                Log.e("getCurrentUserProfile_FAILURE", response.code().toString())
                Log.e("getCurrentUserProfile_FAILURE", response.errorBody().toString())
                return@withContext null
            }
        }
    }

    private fun getUsersProfiles() {
        val usersAux = mutableListOf<User>()
        coroutineScope.launch {
            if (_gameRoom.value != null) {
                _gameRoom.value!!.users.map {idSpotify ->
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

    private fun getRoom() {
        coroutineScope.launch {
            if (_gameRoom.value != null) {
                val getRoomDeferred = firebase.getRoom(_gameRoom.value!!.code)
                try {
                    val getRoomResult = getRoomDeferred.await()
                    if (getRoomResult == null) {
                        _error.value = getRoomDeferred.getCompletionExceptionOrNull()?.message
                    } else {
                        _gameRoom.value = getRoomResult
                        checkIfEverybodyAnswered(getRoomResult)
                    }
                } catch (exception: Exception) {
                    _error.value = exception.message
                }
            }
        }
    }

    fun checkIfEverybodyAnswered(gameRoom: GameRoom) {
        if (_currentQuestion.value != null) {
            val allUsersInRoom = gameRoom.users.toSet()
            val usersWhoAnswered = mutableSetOf<String>()

            for ((userId, answer) in gameRoom.answers) {
                if (answer.containsKey(_currentQuestion.value!!.id)) {
                    usersWhoAnswered.add(userId)
                }
            }

            _everybodyAnswered.value = allUsersInRoom == usersWhoAnswered
        }
    }


    fun getQuestionsProfileZipped(question: Question, completion: () -> Unit) {
        if (_currentUser.value != null) {
            _questionsProfileZipped.value = question.answers.map {
                it to _currentUser.value!!.image_url
            }
        }
        completion()
    }

    fun getLeaderboardZipped(question: Question, completion: () -> Unit) {
        if (_users.value != null && _gameRoom.value != null) {
            _leaderboardZipped.value = _users.value!!.map {user ->
                Triple(user, _gameRoom.value!!, question)
            }
        }
        completion()
    }

    private suspend fun getIncorrectQuestionAnswers(correctAnswerId: String): List<String> {
        val response = GuessifyApi.retrofitService.getCurrentUserRecommendations(
            "Bearer ${accessToken!!.value}",
            tracksId = correctAnswerId,
            limit = 3
        )

        return if (response.isSuccessful) {
            response.body()?.tracks?.map { it.name } ?: emptyList()
        } else {
            Log.e("getIncorrectQuestionAnswers_FAILED", response.code().toString())
            emptyList()
        }
    }


    private fun insertQuestions() {
        coroutineScope.launch {
            if (_gameRoom.value != null) {
                val response = when (_gameRoom.value!!.gamemode) {
                    application.resources.getString(R.string.most_listened_songs) -> {
                        return@launch
                    }
                    application.resources.getString(R.string.most_listened_artists) -> {
                        return@launch
                    }
                    else -> {
                        GuessifyApi.retrofitService.getCurrentUserRecommendations(
                            "Bearer ${accessToken!!.value}",
                            genres = _gameRoom.value!!.gamemode.lowercase(),
                            limit = 30
                        )
                    }
                }
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            Log.e("getQuestionTracks_SUCCESS", response.body().toString())
                            val questionsAux = mutableListOf<Question>()
                            response.body()!!.tracks.map {track ->
                                if (!track.preview_url.isNullOrBlank() && questionsAux.size < 10) {
                                    val incorrectAnswers = getIncorrectQuestionAnswers(track.id)
                                    val question = Question(
                                        track.id,
                                        track.album.images[0].url,
                                        track.preview_url,
                                        track.name,
                                        (mutableListOf(track.name) + incorrectAnswers).shuffled()
                                    )
                                    questionsAux.add(question)
                                }
                            }
                            addQuestions(questionsAux)
                        } else {
                            _error.value = "Failed to get questions"
                            Log.e("getQuestionTracks_FAILURE", "Response body is null")
                        }
                    } else {
                        Log.e("getQuestionTracks_FAILURE", response.code().toString())
                        Log.e("getQuestionTracks_FAILURE", response.errorBody().toString())
                    }
                }
            }
        }
    }

    private fun addQuestions(questions: List<Question>) {
        coroutineScope.launch {
            val addQuestionsDeferred = firebase.addQuestions(questions)
            try {
                val addQuestionsResult = addQuestionsDeferred.await()
                if (!addQuestionsResult) {
                    _error.value = addQuestionsDeferred.getCompletionExceptionOrNull()?.message
                } else {
                    if (_gameRoom.value != null) {
                        val addQuestionsToRoomDeferred = firebase.addQuestionsToRoom(
                            _gameRoom.value!!.code,
                            questions.map { it.id }
                        )
                        try {
                            val addQuestionsToRoomResult = addQuestionsToRoomDeferred.await()
                            if (addQuestionsToRoomResult == null) {
                                _error.value = addQuestionsToRoomDeferred.getCompletionExceptionOrNull()?.message
                            } else {
                                _gameRoom.value = addQuestionsToRoomResult
                                onFinishLoading()
                            }
                        } catch (exception: Exception) {
                            _error.value = exception.message
                        }
                    }
                }
            } catch (exception: Exception) {
                _error.value = exception.message
            }
        }
    }

    fun getQuestionsFromRoom() {
        coroutineScope.launch {
            if (_gameRoom.value != null) {
                val getQuestionsFromRoomDeferred = firebase.getQuestionsFromRoom(_gameRoom.value!!.code)
                try {
                    val getQuestionsFromRoomResult = getQuestionsFromRoomDeferred.await()
                    if (getQuestionsFromRoomResult.isNullOrEmpty()) {
                        _error.value = getQuestionsFromRoomDeferred.getCompletionExceptionOrNull()?.message
                    } else {
                        _gameQuestions.value = GameQuestions(getQuestionsFromRoomResult.sortedBy { it.id })
                    }
                } catch (exception: Exception) {
                    _error.value = exception.message
                }
            }
        }
    }

    private fun onFinishLoading() {
        coroutineScope.launch {
            if (_gameRoom.value != null) {
                val updates = hashMapOf<String, Any>(
                    "doneLoading" to true
                )
                val startRoomDeferred = firebase.updateRoom(_gameRoom.value!!.code, updates)
                try {
                    val startRoomResult = startRoomDeferred.await()
                    if (!startRoomResult) {
                        _error.value = startRoomDeferred.getCompletionExceptionOrNull()?.message
                    }
                } catch (exception: Exception) {
                    _error.value = exception.message
                }
            }
        }
    }

    fun onUserAnswer(question: Question, points: Int) {
        coroutineScope.launch {
            if (_gameRoom.value != null && _currentUser.value != null) {
                try {
                    val addAnswerDeferred = firebase.addAnswerToRoom(_gameRoom.value!!.code, _currentUser.value!!.id_spotify, question.id, points)
                    try {
                        val addAnswerResult = addAnswerDeferred.await()
                        if (addAnswerResult == null) {
                            _error.value = "The room does not exist"

                        } else {
                            _gameRoom.value = addAnswerResult
                            _userHasAnswered.value = true
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