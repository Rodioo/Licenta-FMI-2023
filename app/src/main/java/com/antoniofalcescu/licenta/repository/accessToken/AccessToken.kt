package com.antoniofalcescu.licenta.repository.accessToken

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

private const val HOUR_IN_MILLISECONDS = 60 * 60 * 1000

@Entity(tableName = "access_token")
data class AccessToken(

    @PrimaryKey
    var id: Int = 1,
    var value: String? = null,
    var createdAt: Long = System.currentTimeMillis(),
    var expiresAt: Long = System.currentTimeMillis() + HOUR_IN_MILLISECONDS
)
