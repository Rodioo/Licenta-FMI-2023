package com.antoniofalcescu.licenta.game

data class Room(
    val code: String,
    val gamemode: String,
    val users: List<String>
)