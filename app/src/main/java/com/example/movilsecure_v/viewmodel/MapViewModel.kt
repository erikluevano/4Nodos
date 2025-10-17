package com.example.movilsecure_v.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilsecure_v.model.entities.PlaceDetails
import com.example.movilsecure_v.model.entities.PlaceDetailsResult
import com.example.movilsecure_v.model.repository.PlacesClient
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {
    // ... (los estados no cambian)
    val places = mutableStateOf<List<PlaceDetails>>(emptyList())
    val selectedPlaceForRoute = mutableStateOf<PlaceDetails?>(null)
    val selectedCardPlace = mutableStateOf<PlaceDetails?>(null)

    // --- FUNCIÓN MODIFICADA PARA LA BARRA DE BÚSQUEDA ---
    fun textSearchPlaces(apiKey: String, query: String, location: LatLng, radius: Int) {
        viewModelScope.launch {
            try {
                // 1. Limpiamos la lista anterior para mostrar que algo está cargando
                places.value = emptyList()

                // 2. Hacemos la búsqueda por texto inicial para obtener la lista de lugares
                val locationStr = "${location.latitude},${location.longitude}"
                val searchResponse = PlacesClient.service.textSearch(
                    query = query,
                    location = locationStr,
                    radius = radius,
                    apiKey = apiKey
                )

                if (searchResponse.results.isEmpty()) {
                    return@launch // No hay nada que hacer si no hay resultados
                }

                // 3. Para cada resultado, lanzamos una llamada de "details" en paralelo
                val detailedPlaces = searchResponse.results.map { placeResult ->
                    // Usamos 'async' para que cada llamada a la API se ejecute concurrentemente
                    async {
                        try {
                            val detailsResponse = PlacesClient.service.getPlaceDetails(
                                placeId = placeResult.place_id,
                                apiKey = apiKey
                            )
                            // Usamos la función de conversión que ya teníamos para los detalles
                            detailsResponse.result.toPlaceDetails()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null // Si una llamada individual falla, devolvemos null
                        }
                    }
                }.awaitAll() // Esperamos a que TODAS las llamadas de 'details' terminen

                // 4. Filtramos los resultados que pudieron fallar (nulos) y actualizamos la UI
                places.value = detailedPlaces.filterNotNull()

            } catch (e: Exception) {
                e.printStackTrace()
                places.value = emptyList()
            }
        }
    }

    fun getPlaceDetailsById(apiKey: String, placeId: String) {
        viewModelScope.launch {
            try {
                places.value = emptyList()

                val response = PlacesClient.service.getPlaceDetails(
                    placeId = placeId,
                    apiKey = apiKey
                )
                selectedCardPlace.value = response.result.toPlaceDetails()
            } catch (e: Exception) {
                e.printStackTrace()
                selectedCardPlace.value = null
            }
        }
    }

    // --- Funciones de utilidad ---
    fun selectPlaceForRoute(place: PlaceDetails) {
        selectedPlaceForRoute.value = place
    }

    fun clearSelectedPlaceForRoute() {
        selectedPlaceForRoute.value = null
    }

    fun clearSelectedCardPlace() {
        selectedCardPlace.value = null
    }
}

private fun PlaceDetailsResult.toPlaceDetails(): PlaceDetails {
    return PlaceDetails(
        id = this.place_id,
        name = this.name,
        address = this.formatted_address ?: this.vicinity ?: "Dirección no encontrada",
        location = LatLng(this.geometry.location.lat, this.geometry.location.lng),
        isOpen = when (this.opening_hours?.open_now) {
            true -> "Abierto ahora"
            false -> "Cerrado"
            null -> "Horario no disponible"
        },
        rating = this.rating
    )
}
