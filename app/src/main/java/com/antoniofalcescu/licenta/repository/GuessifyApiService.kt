package com.antoniofalcescu.licenta.repository

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header

private const val BASE_URL = "https://api.spotify.com/v1/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

interface GuessifyApiService {

    @GET(value= "me")
    fun getCurrentUserProfile(@Header("Authorization") accessToken: String): Call<String>
}

object GuessifyApi {
    val retrofitService: GuessifyApiService by lazy {
        retrofit.create(GuessifyApiService::class.java)
    }
}