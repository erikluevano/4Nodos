package com.example.movilsecure_v.modelo.repositorio

import com.example.movilsecure_v.modelo.entidades.PlaceDetailsResponse
import com.example.movilsecure_v.modelo.entidades.PlacesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ServicioUbicaciones {

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
        @Query("fields") fields: String = "place_id,name,vicinity,formatted_address,geometry,opening_hours,rating,formatted_phone_number"
    ): PlaceDetailsResponse

    @GET("maps/api/place/nearbysearch/json")
    suspend fun nearbySearch(
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("type") type: String,
        @Query("key") apiKey: String
    ): PlacesResponse
}