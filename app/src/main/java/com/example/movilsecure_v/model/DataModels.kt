package com.example.movilsecure_v.model

import com.google.android.gms.maps.model.LatLng
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Clase principal que representa la respuesta de la API de Places
data class PlacesResponse(
    val results: List<PlaceResult>
)

// Clase que representa un lugar individual encontrado por la API
data class PlaceResult(
    val name: String,
    val geometry: Geometry,
    val vicinity: String? = null, // Dirección/vecindario
    val opening_hours: OpeningHours? = null,
    val rating: Double? = null,
    val place_id: String // ID único para cada lugar
)

data class Geometry(
    val location: Location
)

data class Location(
    val lat: Double,
    val lng: Double
)

data class OpeningHours(
    val open_now: Boolean
)

// Nuestro propio modelo de datos simplificado para usar en la UI.
data class PlaceDetails(
    val id: String,
    val name: String,
    val address: String,
    val location: LatLng,
    val isOpen: String,
    val rating: Double?
)

@Parcelize // Anotación importante
data class UbicacionResult(
    val direccion: String,
    val latitud: Double,
    val longitud: Double
) : Parcelable // Implementar Parcelable