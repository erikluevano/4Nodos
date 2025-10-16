package com.example.movilsecure_v.model.repository

import com.example.movilsecure_v.model.entities.PlaceDetailsResponse
import com.example.movilsecure_v.model.entities.PlacesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesApiService {

    // Nueva búsqueda por texto para la SearchBar
    @GET("maps/api/place/textsearch/json")
    suspend fun textSearch(
        @Query("query") query: String,
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("key") apiKey: String
    ): PlacesResponse

    @GET("maps/api/place/details/json")
    suspend fun getPlaceDetails(
        @Query("place_id") placeId: String,
        @Query("key") apiKey: String,
        @Query("fields") fields: String = "place_id,name,vicinity,formatted_address,geometry,opening_hours,rating"
    ): PlaceDetailsResponse
}