package com.antoniofalcescu.licenta.game

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Room(
    val code: String,
    val gamemode: String,
    val users: List<String>
): Parcelable {
    constructor() : this("", "", emptyList())
}