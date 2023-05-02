package com.antoniofalcescu.licenta.utils

fun generateRandomString(length: Int): String {
    val random = kotlin.random.Random

    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')

    return (1..length)
        .map { allowedChars[random.nextInt(allowedChars.size)] }
        .joinToString("")
}