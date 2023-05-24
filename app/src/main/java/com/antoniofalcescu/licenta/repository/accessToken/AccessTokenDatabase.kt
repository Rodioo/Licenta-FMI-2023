package com.antoniofalcescu.licenta.repository.accessToken

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AccessToken::class], version = 2, exportSchema = false)
abstract class AccessTokenDatabase: RoomDatabase() {

    abstract val accessTokenDao: AccessTokenDao
    companion object {

        //Volatile annotation is used for singleton instances
        //so that the variable value can be accessed and modified by multiple threads at any time
        @Volatile
        private var INSTANCE: AccessTokenDatabase? = null

        fun getInstance(context: Context): AccessTokenDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AccessTokenDatabase::class.java,
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