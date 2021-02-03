package com.example.flowerhunter

import retrofit2.Call
import retrofit2.Callback
import retrofit2.http.GET
import retrofit2.http.Query

interface IGoogleAPIService {
    @GET("maps/api/directions/json")
    fun getDirections(@Query("origin") origin: String, @Query("destination") destination: String, @Query("key") api_key: String):
            Call<String>
}