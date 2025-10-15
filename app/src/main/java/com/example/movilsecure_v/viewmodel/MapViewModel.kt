package com.example.movilsecure_v.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilsecure_v.data.PlaceDetails
import com.example.movilsecure_v.data.PlacesClient
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {
    // 1. El estado ahora almacena una lista de PlaceDetails
    val places = mutableStateOf<List<PlaceDetails>>(emptyList())
    val selectedPlace = mutableStateOf<PlaceDetails?>(null)

    fun searchNearbyPlaces(apiKey: String, location: LatLng, type: String, radius: Int) {
        viewModelScope.launch {
            try {
                val response = PlacesClient.service.searchNearby(
                    location = "${location.latitude},${location.longitude}",
                    radius = radius,
                    type = type,
                    apiKey = apiKey
                )
                // 2. Convierte la respuesta de la API a nuestra clase PlaceDetails
                places.value = response.results.map { placeResult ->
                    PlaceDetails(
                        name = placeResult.name,
                        address = placeResult.vicinity ?: "Dirección no disponible",
                        location = LatLng(placeResult.geometry.location.lat, placeResult.geometry.location.lng),
                        isOpen = when (placeResult.opening_hours?.open_now) {
                            true -> "Abierto ahora"
                            false -> "Cerrado"
                            null -> "Horario no disponible"
                        },
                        rating = placeResult.rating
                    )
                }
            } catch (e: Exception) {
                places.value = emptyList()
                e.printStackTrace()
            }
        }
    }

    fun selectPlace(place: PlaceDetails) {
        selectedPlace.value = place
    }

    fun clearSelectedPlace() {
        selectedPlace.value = null
    }
}