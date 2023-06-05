package com.antoniofalcescu.licenta.repository.roomDatabase.gameRoom

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "game_room_code")
data class GameRoomCode(
    @PrimaryKey
    val id: Int = 1,
    val code: String,
): Parcelable