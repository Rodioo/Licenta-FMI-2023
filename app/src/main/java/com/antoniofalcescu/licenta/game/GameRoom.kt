package com.antoniofalcescu.licenta.game

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GameRoom(
    val code: String,
    val hasStarted: Boolean,
    val doneLoading: Boolean,
    val gamemode: String,
    val users: List<String>,
    val questions: List<String>
): Parcelable {
    constructor() : this("", false, false,"", emptyList(), emptyList())
}
