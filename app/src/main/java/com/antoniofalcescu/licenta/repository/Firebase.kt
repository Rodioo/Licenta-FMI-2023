package com.antoniofalcescu.licenta.repository

import android.app.Application
import android.util.Log
import com.antoniofalcescu.licenta.game.Room
import com.antoniofalcescu.licenta.home.User
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CompletableDeferred

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

    fun addRoom(room: Room): CompletableDeferred<Boolean> {
        val deferred = CompletableDeferred<Boolean>()

        firebaseInstance.collection("rooms")
            .document(room.code)
            .set(room)
            .addOnSuccessListener {
                Log.i("addedRoom", room.toString())
                deferred.complete(true)
            }
            .addOnFailureListener { exception ->
                Log.e(
                    "HomeViewModel",
                    "Failed to add room: ${room.code}: ${exception.message}"
                )
                deferred.completeExceptionally(exception)
            }

        return deferred
    }

    fun addUserToRoom(roomCode: String, userId: String): CompletableDeferred<Room?> {
        val deferred = CompletableDeferred<Room?>()

        val roomRef = firebaseInstance.collection("rooms").document(roomCode)

        firebaseInstance.runTransaction { transaction ->
            val roomSnapshot = transaction.get(roomRef)

            if (roomSnapshot.exists()) {
                val userList = roomSnapshot.toObject(Room::class.java)?.users?.toMutableList()
                if (userList != null && !userList.contains(userId)) {
                    userList.add(userId)
                }
                transaction.update(roomRef, "users", userList)
            } else {
                // Room doesn't exist
                deferred.complete(null)
            }
        }
            .addOnSuccessListener {
                roomRef.get()
                    .addOnSuccessListener { roomSnapshot ->
                        val updatedRoom = roomSnapshot.toObject(Room::class.java)
                        deferred.complete(updatedRoom)
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

    fun removeUserFromRoom(roomCode: String, userId: String): CompletableDeferred<Room?> {
        val deferred = CompletableDeferred<Room?>()

        val roomRef = firebaseInstance.collection("rooms").document(roomCode)

        firebaseInstance.runTransaction { transaction ->
            val roomSnapshot = transaction.get(roomRef)

            if (roomSnapshot.exists()) {
                val userList = roomSnapshot.toObject(Room::class.java)?.users?.toMutableList()
                userList?.remove(userId)

                transaction.update(roomRef, "users", userList)
            } else {
                // Room doesn't exist
                deferred.complete(null)
            }
        }
            .addOnSuccessListener {
                roomRef.get()
                    .addOnSuccessListener { roomSnapshot ->
                        val updatedRoom = roomSnapshot.toObject(Room::class.java)
                        deferred.complete(updatedRoom)
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
                    val room = documentSnapshot.toObject(Room::class.java)
                    val users = room?.users ?: emptyList()
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
}