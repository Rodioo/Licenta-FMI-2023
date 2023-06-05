package com.antoniofalcescu.licenta.repository.roomDatabase.gameRoom

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface GameRoomCodeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(gameRoom: GameRoomCode)

    @Query("SELECT * FROM game_room_code WHERE id = 1")
    fun get(): GameRoomCode?
}