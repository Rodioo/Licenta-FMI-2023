package com.antoniofalcescu.licenta.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat.startActivity

const val EMPTY_PROFILE_IMAGE_URL = "https://t4.ftcdn.net/jpg/04/08/24/43/360_F_408244382_Ex6k7k8XYzTbiXLNJgIL8gssebpLLBZQ.jpg"

data class SpotifyImage(
    val url: String
)

data class SpotifyUrl(
    val spotify: String
)

