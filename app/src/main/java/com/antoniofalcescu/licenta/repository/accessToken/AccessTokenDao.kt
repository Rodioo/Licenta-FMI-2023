package com.antoniofalcescu.licenta.repository.accessToken

import androidx.room.*

@Dao
interface AccessTokenDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(accessToken: AccessToken)

    @Query("SELECT * FROM access_token WHERE id = 1")
    fun get(): AccessToken

    @Query("UPDATE access_token SET needsRefresh=:needsRefresh WHERE id = 1")
    fun updateRefresh(needsRefresh: Boolean)
}