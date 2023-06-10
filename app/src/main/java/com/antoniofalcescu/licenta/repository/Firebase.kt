package com.antoniofalcescu.licenta.repository

import android.app.Application
import android.util.Log
import com.antoniofalcescu.licenta.game.GameRoom
import com.antoniofalcescu.licenta.home.User
import com.antoniofalcescu.licenta.question.Question
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CompletableDeferred

const val FIREBASE_DELETE_EMPTY_ROOMS_INTERVAL: Long = 3 * 60 * 1000
const val CHECK_IF_USER_GOT_KICKED_FROM_ROOM_INTERVAL: Long = 2 * 1000

class Firebase(application: Application) {

    private val firebaseInstance = FirebaseFirestore.getInstance()

    init {
        FirebaseApp.initializeApp(application)
    }

    fun addUser(user: User): CompletableDeferred<Boolean> {
        val deferred = CompletableDeferred<Boolean>()

        firebaseInstance.collection("users")
            .document(user.id_spotify)
            .set(user)
            .addOnSuccessListener {
                Log.i("addedUser", user.toString())
                deferred.complete(true)
            }
            .addOnFailureListener { exception ->
                Log.e(
                    "HomeViewModel",
                    "Failed to add/replace user: ${user.id_spotify}: ${exception.message}"
                )
                deferred.completeExceptionally(exception)
            }

        return deferred
    }

    fun getUser(idSpotify: String): CompletableDeferred<User?> {
        val deferred = CompletableDeferred<User?>()

        firebaseInstance.collection("users")
            .document(idSpotify)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val user = documentSnapshot.toObject(User::class.java)
                    deferred.complete(user)
                } else {
                    deferred.complete(null)
                }
            }
            .addOnFailureListener { exception ->
                deferred.completeExceptionally(exception)
            }

        return deferred
    }

    fun addRoom(gameRoom: GameRoom): CompletableDeferred<Boolean> {
        val deferred = CompletableDeferred<Boolean>()

        firebaseInstance.collection("rooms")
            .document(gameRoom.code)
            .set(gameRoom)
            .addOnSuccessListener {
                Log.i("addedRoom", gameRoom.toString())
                deferred.complete(true)
            }
            .addOnFailureListener { exception ->
                Log.e(
                    "HomeViewModel",
                    "Failed to add room: ${gameRoom.code}: ${exception.message}"
                )
                deferred.completeExceptionally(exception)
            }

        return deferred
    }

    fun getRoom(roomCode: String): CompletableDeferred<GameRoom?> {
        val deferred = CompletableDeferred<GameRoom?>()

        firebaseInstance.collection("rooms").document(roomCode).get()
            .addOnSuccessListener { roomSnapshot ->
                val updatedGameRoom = roomSnapshot.toObject(GameRoom::class.java)
                deferred.complete(updatedGameRoom)
            }
            .addOnFailureListener { exception ->
                Log.e("updateRoomWithUser", "Failed to get updated room: $roomCode: ${exception.message}")
                deferred.completeExceptionally(exception)
            }

        return deferred
    }

    fun updateRoom(roomCode: String, updates: HashMap<String, Any>): CompletableDeferred<Boolean> {
        val deferred = CompletableDeferred<Boolean>()

        firebaseInstance.collection("rooms").document(roomCode)
            .update(updates)
            .addOnSuccessListener {
                deferred.complete(true)
            }
            .addOnFailureListener { exception ->
                Log.e("updateRoomHasStarted", "Failed to update room: $roomCode: ${exception.message}")
                deferred.completeExceptionally(exception)
            }

        return deferred
    }

    fun addAnswerToRoom(roomCode: String, userId: String, questionId: String, points: Int): CompletableDeferred<GameRoom?> {
        val deferred = CompletableDeferred<GameRoom?>()

        val roomRef = firebaseInstance.collection("rooms").document(roomCode)

        firebaseInstance.runTransaction { transaction ->
            val roomSnapshot = transaction.get(roomRef)

            if (roomSnapshot.exists()) {
                val answers = roomSnapshot.toObject(GameRoom::class.java)?.answers?.toMutableMap()
                if (answers != null && answers.containsKey(userId)) {
                    answers[userId] = hashMapOf(
                        questionId to points
                    )
                }

                transaction.update(roomRef, "answers", answers)

                val totalPoints = roomSnapshot.toObject(GameRoom::class.java)?.totalPoints?.toMutableMap()
                if (totalPoints != null && totalPoints.containsKey(userId)) {
                    if (totalPoints[userId] == null) {
                        totalPoints[userId] = points
                    } else {
                        totalPoints[userId] = totalPoints[userId]!! + points
                    }
                }

                transaction.update(roomRef, "totalPoints", totalPoints)
            } else {
                // Room doesn't exist
                deferred.complete(null)
            }
        }
            .addOnSuccessListener {
                roomRef.get()
                    .addOnSuccessListener { roomSnapshot ->
                        val updatedGameRoom = roomSnapshot.toObject(GameRoom::class.java)
                        deferred.complete(updatedGameRoom)
                    }
                    .addOnFailureListener { exception ->
                        Log.e("updateRoomWithAnswer", "Failed to get updated room: $roomCode: ${exception.message}")
                        deferred.completeExceptionally(exception)
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("updateRoomWithAnswer", "Failed to update room: $roomCode: ${exception.message}")
                deferred.completeExceptionally(exception)
            }

        return deferred
    }

    fun resetRoomStatus(roomCode: String): CompletableDeferred<GameRoom?> {
        val deferred = CompletableDeferred<GameRoom?>()

        val roomRef = firebaseInstance.collection("rooms").document(roomCode)

        firebaseInstance.runTransaction { transaction ->
            val roomSnapshot = transaction.get(roomRef)

            if (roomSnapshot.exists()) {

                transaction.update(roomRef, "doneLoading", false)
                transaction.update(roomRef, "hasStarted", false)
                transaction.update(roomRef, "questions", emptyList<String>())

                val answers = roomSnapshot.toObject(GameRoom::class.java)?.answers?.toMutableMap()
                if (answers != null) {
                    for (userId in answers.keys) {
                        answers[userId] = hashMapOf()
                    }
                }

                transaction.update(roomRef, "answers", answers)

                val totalPoints = roomSnapshot.toObject(GameRoom::class.java)?.totalPoints?.toMutableMap()
                if (totalPoints != null) {
                    for (userId in totalPoints.keys) {
                        totalPoints[userId] = 0
                    }
                }

                transaction.update(roomRef, "totalPoints", totalPoints)
            } else {
                // Room doesn't exist
                deferred.complete(null)
            }
        }
            .addOnSuccessListener {
                roomRef.get()
                    .addOnSuccessListener { roomSnapshot ->
                        val updatedGameRoom = roomSnapshot.toObject(GameRoom::class.java)
                        deferred.complete(updatedGameRoom)
                    }
                    .addOnFailureListener { exception ->
                        Log.e("updateRoomWithAnswer", "Failed to get updated room: $roomCode: ${exception.message}")
                        deferred.completeExceptionally(exception)
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("updateRoomWithAnswer", "Failed to update room: $roomCode: ${exception.message}")
                deferred.completeExceptionally(exception)
            }

        return deferred
    }

    fun deleteEmptyRooms() {
        firebaseInstance.collection("rooms").get()
            .addOnSuccessListener { querySnapshot ->
                val batch = firebaseInstance.batch()

                for (document in querySnapshot.documents) {
                    val gameRoom = document.toObject(GameRoom::class.java)
                    if (gameRoom?.users.isNullOrEmpty()) {
                        batch.delete(document.reference)
                    }
                }

                batch.commit()
                    .addOnSuccessListener {
                        // Deletion successful
                        Log.i("deleteRooms", "Rooms deleted successfully.")
                    }
                    .addOnFailureListener { exception ->
                        // Error occurred during deletion
                        Log.e("deleteRooms", "Failed to delete rooms: ${exception.message}")
                    }
            }
            .addOnFailureListener { exception ->
                // Error occurred while fetching the documents
                Log.e("deleteRooms", "Failed to fetch rooms: ${exception.message}")
            }
    }

    fun addUserToRoom(roomCode: String, userId: String): CompletableDeferred<GameRoom?> {
        val deferred = CompletableDeferred<GameRoom?>()

        val roomRef = firebaseInstance.collection("rooms").document(roomCode)

        firebaseInstance.runTransaction { transaction ->
            val roomSnapshot = transaction.get(roomRef)

            if (roomSnapshot.exists()) {
                val gameRoom = roomSnapshot.toObject(GameRoom::class.java)
                if (gameRoom?.hasStarted == true) {
                    deferred.complete(null)
                } else {
                    val userList = roomSnapshot.toObject(GameRoom::class.java)?.users?.toMutableList()
                    if (userList != null && !userList.contains(userId)) {
                        userList.add(userId)
                    }

                    transaction.update(roomRef, "users", userList)

                    val answers = roomSnapshot.toObject(GameRoom::class.java)?.answers?.toMutableMap()
                    if (answers != null && !answers.containsKey(userId)) {
                        answers[userId] = hashMapOf()
                    }

                    transaction.update(roomRef, "answers", answers)

                    val totalPoints = roomSnapshot.toObject(GameRoom::class.java)?.totalPoints?.toMutableMap()
                    if (totalPoints != null && !totalPoints.containsKey(userId)) {
                        totalPoints[userId] = 0
                    }

                    transaction.update(roomRef, "totalPoints", totalPoints)
                }
            } else {
                // Room doesn't exist
                deferred.complete(null)
            }
        }
            .addOnSuccessListener {
                roomRef.get()
                    .addOnSuccessListener { roomSnapshot ->
                        val updatedGameRoom = roomSnapshot.toObject(GameRoom::class.java)
                        deferred.complete(updatedGameRoom)
                    }
                    .addOnFailureListener { exception ->
                        Log.e("updateRoomWithUser", "Failed to get updated room: $roomCode: ${exception.message}")
                        deferred.completeExceptionally(exception)
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("updateRoomWithUser", "Failed to update room: $roomCode: ${exception.message}")
                deferred.completeExceptionally(exception)
            }

        return deferred
    }

    fun removeUserFromRoom(roomCode: String, userId: String): CompletableDeferred<GameRoom?> {
        val deferred = CompletableDeferred<GameRoom?>()

        val roomRef = firebaseInstance.collection("rooms").document(roomCode)

        firebaseInstance.runTransaction { transaction ->
            val roomSnapshot = transaction.get(roomRef)

            if (roomSnapshot.exists()) {
                val userList = roomSnapshot.toObject(GameRoom::class.java)?.users?.toMutableList()
                userList?.remove(userId)
                transaction.update(roomRef, "users", userList)

                val answers = roomSnapshot.toObject(GameRoom::class.java)?.answers?.toMutableMap()
                answers?.remove(userId)
                transaction.update(roomRef, "answers", answers)

                val totalPoints = roomSnapshot.toObject(GameRoom::class.java)?.totalPoints?.toMutableMap()
                totalPoints?.remove(userId)
                transaction.update(roomRef, "totalPoints", totalPoints)

            } else {
                // Room doesn't exist
                deferred.complete(null)
            }
        }
            .addOnSuccessListener {
                roomRef.get()
                    .addOnSuccessListener { roomSnapshot ->
                        val updatedGameRoom = roomSnapshot.toObject(GameRoom::class.java)
                        deferred.complete(updatedGameRoom)
                    }
                    .addOnFailureListener { exception ->
                        Log.e("updateRoomWithUser", "Failed to get updated room: $roomCode: ${exception.message}")
                        deferred.completeExceptionally(exception)
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("updateRoomWithUser", "Failed to update room: $roomCode: ${exception.message}")
                deferred.completeExceptionally(exception)
            }

        return deferred
    }

    fun deleteUserFromAllRooms(userId: String): CompletableDeferred<Boolean> {
        val deferred = CompletableDeferred<Boolean>()

        firebaseInstance.collection("rooms")
            .whereArrayContains("users", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val batch = firebaseInstance.batch()

                for (document in querySnapshot.documents) {
                    val userList = document.toObject(GameRoom::class.java)?.users?.toMutableList()
                    userList?.remove(userId)

                    batch.update(document.reference, "users", userList)
                }

                batch.commit()
                    .addOnSuccessListener {
                        deferred.complete(true)
                    }
                    .addOnFailureListener { exception ->
                        Log.e("deleteUserFromAllRooms", "Failed to update rooms: ${exception.message}")
                        deferred.completeExceptionally(exception)
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("deleteUserFromAllRooms", "Failed to query rooms: ${exception.message}")
                deferred.completeExceptionally(exception)
            }

        return deferred
    }

    fun getUsedRoomCodes(): CompletableDeferred<Set<String>> {
        val deferred = CompletableDeferred<Set<String>>()

        firebaseInstance.collection("rooms").get()
            .addOnSuccessListener { querySnapshot ->
                val usedRoomCodes = mutableSetOf<String>()
                for (document in querySnapshot.documents) {
                    val code = document.id
                    usedRoomCodes.add(code)
                }
                deferred.complete(usedRoomCodes)
            }
            .addOnFailureListener { exception ->
                deferred.completeExceptionally(exception)
            }

        return deferred
    }

    fun getUsersFromRoom(roomCode: String): CompletableDeferred<List<String>> {
        val deferred = CompletableDeferred<List<String>>()

        firebaseInstance.collection("rooms")
            .document(roomCode)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val gameRoom = documentSnapshot.toObject(GameRoom::class.java)
                    val users = gameRoom?.users ?: emptyList()
                    deferred.complete(users)
                } else {
                    deferred.complete(emptyList())
                }
            }
            .addOnFailureListener { exception ->
                deferred.completeExceptionally(exception)
            }

        return deferred
    }

    fun addQuestions(questions: List<Question>): CompletableDeferred<Boolean> {
        val deferred = CompletableDeferred<Boolean>()

        val batch = firebaseInstance.batch()

        for (question in questions) {
            val questionRef = firebaseInstance.collection("questions").document(question.id)
            batch.set(questionRef, question)
        }

        batch.commit()
            .addOnSuccessListener {
                deferred.complete(true)
            }
            .addOnFailureListener { exception ->
                Log.e("HomeViewModel", "Failed to add questions: ${exception.message}")
                deferred.completeExceptionally(exception)
            }

        return deferred
    }

    fun addQuestionsToRoom(roomCode: String, questionIds: List<String>): CompletableDeferred<GameRoom?> {
        val deferred = CompletableDeferred<GameRoom?>()

        val roomRef = firebaseInstance.collection("rooms").document(roomCode)

        firebaseInstance.runTransaction { transaction ->
            val roomSnapshot = transaction.get(roomRef)

            if (roomSnapshot.exists()) {
                transaction.update(roomRef, "questions", questionIds)
            } else {
                // Room doesn't exist
                deferred.complete(null)
            }
        }
            .addOnSuccessListener {
                roomRef.get()
                    .addOnSuccessListener { roomSnapshot ->
                        val updatedGameRoom = roomSnapshot.toObject(GameRoom::class.java)
                        deferred.complete(updatedGameRoom)
                    }
                    .addOnFailureListener { exception ->
                        Log.e("addedQuestionsToRoom", "Failed to get updated room: $roomCode: ${exception.message}")
                        deferred.completeExceptionally(exception)
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("addedQuestionsToRoom", "Failed to update room: $roomCode: ${exception.message}")
                deferred.completeExceptionally(exception)
            }

        return deferred
    }

    fun getQuestionsFromRoom(roomCode: String): CompletableDeferred<List<Question>> {
        val deferred = CompletableDeferred<List<Question>>()

        firebaseInstance.collection("rooms")
            .document(roomCode)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val gameRoom = documentSnapshot.toObject(GameRoom::class.java)
                    val questionIds = gameRoom?.questions ?: emptyList()

                    if (!questionIds.isNullOrEmpty()) {
                        firebaseInstance.collection("questions")
                            .whereIn(FieldPath.documentId(), questionIds)
                            .get()
                            .addOnSuccessListener {questionSnapshot ->
                                val questions = questionSnapshot.toObjects(Question::class.java)
                                deferred.complete(questions)
                            }
                            .addOnFailureListener { exception ->
                                Log.e("Firestore", "Failed to fetch questions: ${exception.message}")
                                deferred.completeExceptionally(exception)
                            }
                    }
                } else {
                    deferred.complete(emptyList())
                }
            }
            .addOnFailureListener { exception ->
                deferred.completeExceptionally(exception)
            }

        return deferred
    }

}