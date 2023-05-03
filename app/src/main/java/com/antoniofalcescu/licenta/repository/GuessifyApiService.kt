package com.antoniofalcescu.licenta.repository

import com.antoniofalcescu.licenta.profile.Profile
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header

private const val BASE_URL = "https://api.spotify.com/v1/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface GuessifyApiService {
    @GET(value= "me")
    suspend fun getCurrentUserProfile(@Header("Authorization") accessToken: String): Response<Profile>
}

object GuessifyApi {
    val retrofitService: GuessifyApiService by lazy {
        retrofit.create(GuessifyApiService::class.java)
    }
}