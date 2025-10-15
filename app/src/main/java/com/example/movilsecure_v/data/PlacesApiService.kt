package com.example.movilsecure_v.data

import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesApiService {
    @GET("maps/api/place/nearbysearch/json")
    suspend fun searchNearby(
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("type") type: String,
        @Query("key") apiKey: String
    ): PlacesResponse
}