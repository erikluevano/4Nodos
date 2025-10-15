package com.example.movilsecure_v.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilsecure_v.model.PlaceDetails
import com.example.movilsecure_v.model.PlaceResult
import com.example.movilsecure_v.model.entities.PlacesClient
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {
    val places = mutableStateOf<List<PlaceDetails>>(emptyList())
    val selectedPlace = mutableStateOf<PlaceDetails?>(null)

    private val allCategories = listOf("hospital", "pharmacy", "clinic")

    // Búsqueda por tipo (para los chips de filtro)
    fun searchNearbyPlaces(apiKey: String, location: LatLng, type: String, radius: Int) {
        viewModelScope.launch {
            try {
                val response = PlacesClient.service.searchNearby(
                    location = "${location.latitude},${location.longitude}",
                    radius = radius,
                    type = type,
                    apiKey = apiKey
                )
                places.value = response.results.map { it.toPlaceDetails() }
            } catch (e: Exception) {
                places.value = emptyList()
                e.printStackTrace()
            }
        }
    }
    
    // Nueva búsqueda por texto (para la SearchBar)
    fun textSearchPlaces(apiKey: String, query: String, location: LatLng, radius: Int) {
        viewModelScope.launch {
            try {
                val response = PlacesClient.service.textSearch(
                    query = query,
                    location = "${location.latitude},${location.longitude}",
                    radius = radius,
                    apiKey = apiKey
                )
                places.value = response.results.map { it.toPlaceDetails() }
            } catch (e: Exception) {
                places.value = emptyList()
                e.printStackTrace()
            }
        }
    }

    // Búsqueda en todas las categorías (para el chip "Todos")
    fun searchAllCategories(apiKey: String, location: LatLng, radius: Int) {
        viewModelScope.launch {
            try {
                val deferredResults = allCategories.map { category ->
                    async {
                        PlacesClient.service.searchNearby(
                            location = "${location.latitude},${location.longitude}",
                            radius = radius,
                            type = category,
                            apiKey = apiKey
                        ).results
                    }
                }
                val allResults = deferredResults.awaitAll().flatten()
                
                places.value = allResults
                    .map { it.toPlaceDetails() }
                    .distinctBy { it.id }
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

private fun PlaceResult.toPlaceDetails(): PlaceDetails {
    return PlaceDetails(
        id = this.place_id,
        name = this.name,
        address = this.vicinity ?: "Dirección no disponible",
        location = LatLng(this.geometry.location.lat, this.geometry.location.lng),
        isOpen = when (this.opening_hours?.open_now) {
            true -> "Abierto ahora"
            false -> "Cerrado"
            null -> "Horario no disponible"
        },
        rating = this.rating
    )
}