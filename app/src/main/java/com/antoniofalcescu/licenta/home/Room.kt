package com.antoniofalcescu.licenta.home

import com.antoniofalcescu.licenta.profile.Profile

data class Room(
    private val id: String,
    private val code: String,
    private val category: String,
    val users: List<Profile>
)
