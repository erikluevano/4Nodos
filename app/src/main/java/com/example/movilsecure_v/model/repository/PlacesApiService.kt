package com.example.movilsecure_v.model.repository

import com.example.movilsecure_v.model.PlacesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesApiService {
    // Búsqueda por cercanía (la que ya teníamos)
    @GET("maps/api/place/nearbysearch/json")
    suspend fun searchNearby(
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("type") type: String,
        @Query("key") apiKey: String
    ): PlacesResponse

    // Nueva búsqueda por texto para la SearchBar
    @GET("maps/api/place/textsearch/json")
    suspend fun textSearch(
        @Query("query") query: String,
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("key") apiKey: String
    ): PlacesResponse
}