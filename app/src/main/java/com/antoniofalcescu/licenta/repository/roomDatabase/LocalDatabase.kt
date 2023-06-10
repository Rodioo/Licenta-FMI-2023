package com.antoniofalcescu.licenta.repository.roomDatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.antoniofalcescu.licenta.repository.roomDatabase.accessToken.AccessToken
import com.antoniofalcescu.licenta.repository.roomDatabase.accessToken.AccessTokenDao
import com.antoniofalcescu.licenta.repository.roomDatabase.gameRoom.GameRoomCode
import com.antoniofalcescu.licenta.repository.roomDatabase.gameRoom.GameRoomCodeDao

@Database(entities = [AccessToken::class, GameRoomCode::class], version = 5, exportSchema = false)
abstract class LocalDatabase: RoomDatabase() {

    abstract val accessTokenDao: AccessTokenDao
    abstract val gameRoomDao: GameRoomCodeDao

    companion object {

        //Volatile annotation is used for singleton instances
        //so that the variable value can be accessed and modified by multiple threads at any time
        @Volatile
        private var INSTANCE: LocalDatabase? = null

        fun getInstance(context: Context): LocalDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        LocalDatabase::class.java,
                        "authorization_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()

                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}