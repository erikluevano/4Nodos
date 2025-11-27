package com.example.movilsecure_v.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilsecure_v.modelo.entidades.PlaceDetails
import com.example.movilsecure_v.modelo.entidades.PlaceDetailsResult
import com.example.movilsecure_v.modelo.repositorio.RepositorioUbicaciones
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.Locale
import kotlin.coroutines.resume

class MapViewModel : ViewModel() {
    val places = mutableStateOf<List<PlaceDetails>>(emptyList())
    val selectedPlaceForRoute = mutableStateOf<PlaceDetails?>(null)
    val selectedCardPlace = mutableStateOf<PlaceDetails?>(null)

    // ... (El resto de tus funciones como textSearchPlaces, nearbySearchPlaces, etc. van aquí sin cambios)

    fun textSearchPlaces(apiKey: String, query: String, location: LatLng, radius: Int) {
        viewModelScope.launch {
            try {
                places.value = emptyList()
                val locationStr = "${location.latitude},${location.longitude}"
                val searchResponse = RepositorioUbicaciones.service.textSearch(
                    query = query,
                    location = locationStr,
                    radius = radius,
                    apiKey = apiKey
                )
                if (searchResponse.results.isEmpty()) return@launch

                val detailedPlaces = searchResponse.results.map {
                    async {
                        try {
                            val detailsResponse = RepositorioUbicaciones.service.getPlaceDetails(it.place_id, apiKey)
                            detailsResponse.result.toPlaceDetails()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                    }
                }.awaitAll()
                places.value = detailedPlaces.filterNotNull()
            } catch (e: Exception) {
                e.printStackTrace()
                places.value = emptyList()
            }
        }
    }

    fun nearbySearchPlaces(apiKey: String, type: String, location: LatLng, radius: Int) {
        viewModelScope.launch {
            try {
                places.value = emptyList()
                val locationStr = "${location.latitude},${location.longitude}"
                val searchResponse = RepositorioUbicaciones.service.nearbySearch(
                    type = type,
                    location = locationStr,
                    radius = radius,
                    apiKey = apiKey
                )
                if (searchResponse.results.isEmpty()) return@launch

                val detailedPlaces = searchResponse.results.map {
                    async {
                        try {
                            val detailsResponse = RepositorioUbicaciones.service.getPlaceDetails(it.place_id, apiKey)
                            detailsResponse.result.toPlaceDetails()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                    }
                }.awaitAll()
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
                val response = RepositorioUbicaciones.service.getPlaceDetails(placeId, apiKey)
                selectedCardPlace.value = response.result.toPlaceDetails()
            } catch (e: Exception) {
                e.printStackTrace()
                selectedCardPlace.value = null
            }
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocationAddress(context: Context): String {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val geocoder = Geocoder(context, Locale.getDefault())
        val defaultAddress = "Dirección no encontrada"

        return try {
            val location = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).await()

            if (location != null) {
                val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    getAddressFromLocation(geocoder, location.latitude, location.longitude)
                } else {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(location.latitude, location.longitude, 1)
                }
                addresses?.firstOrNull()?.getAddressLine(0) ?: defaultAddress
            } else {
                defaultAddress
            }
        } catch (e: Exception) {
            e.printStackTrace()
            defaultAddress
        }
    }

    @Suppress("DEPRECATION")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun getAddressFromLocation(geocoder: Geocoder, latitude: Double, longitude: Double): List<Address>? {
        return suspendCancellableCoroutine { continuation ->
            geocoder.getFromLocation(latitude, longitude, 1,
                object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        continuation.resume(addresses)
                    }

                    override fun onError(errorMessage: String?) {
                        continuation.resume(null)
                    }
                }
            )
        }
    }

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
        rating = this.rating,
        phoneNumber = this.formatted_phone_number
    )
}