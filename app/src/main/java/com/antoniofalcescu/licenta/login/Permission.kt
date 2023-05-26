package com.antoniofalcescu.licenta.login

enum class Permission(val scopes: Array<String> ) {
    PLAYBACK(arrayOf("streaming")),
    PLAYLISTS(arrayOf("playlist-read-private", "playlist-read-collaborative")),
    FOLLOWING(arrayOf("user-follow-read")),
    LISTENING_HISTORY(arrayOf("user-top-read", "user-read-recently-played", "user-read-currently-playing")),
    LIBRARY(arrayOf("user-library-read")),
    PROFILE(arrayOf("user-read-email", "user-read-private")),
    ALL_ACCESS(arrayOf(
        "streaming",
        "playlist-read-private", "playlist-read-collaborative",
        "user-follow-read",
        "user-top-read", "user-read-recently-played", "user-read-currently-playing",
        "user-library-read",
        "user-read-email", "user-read-private"
    ))
}