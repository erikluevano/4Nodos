package com.example.movilsecure_v.vista.ui

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.movilsecure_v.BuildConfig
import com.example.movilsecure_v.vista.componentes.mapa.*
import com.example.movilsecure_v.viewmodel.MapViewModel
import com.google.android.gms.maps.model.LatLng

@Composable
fun MapaScreen(modifier: Modifier = Modifier, mapViewModel: MapViewModel = viewModel()) {
    var query by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("all") }

    val context = LocalContext.current
    val zacatecas = LatLng(22.7709, -102.5833)

    val places by mapViewModel.places
    val selectedCardPlace by mapViewModel.selectedCardPlace
    val selectedPlaceForRoute by mapViewModel.selectedPlaceForRoute

    selectedPlaceForRoute?.let { place ->
        RouteDialog(
            place = place,
            onClose = { mapViewModel.clearSelectedPlaceForRoute() },
            onStartNavigation = {
                val gmmIntentUri =
                    "google.navigation:q=${place.location.latitude},${place.location.longitude}".toUri()
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                context.startActivity(mapIntent)
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        SearchBar(
            query = query,
            onQueryChange = { newQuery -> query = newQuery },
            onSearch = {
                mapViewModel.clearSelectedCardPlace()
                if (query.isNotBlank()) {
                    selectedType = "all" // Resetea el chip al buscar
                    mapViewModel.textSearchPlaces(
                        apiKey = BuildConfig.MAPS_API_KEY,
                        query = query,
                        location = zacatecas,
                        radius = 10000
                    )
                } else {
                    mapViewModel.places.value = emptyList()
                }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        FilterChips(
            selectedType = selectedType,
            onTypeSelected = { type ->
                selectedType = type
                mapViewModel.clearSelectedCardPlace()
                query = "" // Limpia la búsqueda de texto

                if (type == "all") {
                    mapViewModel.places.value = emptyList()
                } else {
                    mapViewModel.nearbySearchPlaces(
                        apiKey = BuildConfig.MAPS_API_KEY,
                        type = type,
                        location = zacatecas,
                        radius = 10000
                    )
                }
            }
        )
        Spacer(modifier = Modifier.height(12.dp))

        InteractiveMap(
            places = places,
            onPOIClick = { placeId ->
                query = ""
                mapViewModel.getPlaceDetailsById(BuildConfig.MAPS_API_KEY, placeId)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedCardPlace != null) {
            LocationCard(
                place = selectedCardPlace!!,
                onViewRoute = { mapViewModel.selectPlaceForRoute(selectedCardPlace!!) }
            )
        } else if (places.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(places, key = { it.id }) { place ->
                    LocationCard(
                        place = place,
                        onViewRoute = { mapViewModel.selectPlaceForRoute(place) }
                    )
                }
            }
        }
    }
}