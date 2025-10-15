package com.example.movilsecure_v.view.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.movilsecure_v.BuildConfig
import com.example.movilsecure_v.view.components.map.FilterChips
import com.example.movilsecure_v.view.components.map.LocationCard
import com.example.movilsecure_v.view.components.map.MapPlaceholder
import com.example.movilsecure_v.view.components.map.RouteDialog
import com.example.movilsecure_v.view.components.map.SearchBar
import com.example.movilsecure_v.viewmodel.MapViewModel
import com.google.android.gms.maps.model.LatLng

@Composable
fun MapaScreen(modifier: Modifier = Modifier, mapViewModel: MapViewModel = viewModel()) {
    var query by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("all") }

    val zacatecas = LatLng(22.7709, -102.5833)
    val places by mapViewModel.places
    val selectedPlace by mapViewModel.selectedPlace

    selectedPlace?.let { place ->
        RouteDialog(
            place = place,
            onClose = { mapViewModel.clearSelectedPlace() },
            onStartNavigation = { /* Lógica de navegación futura */ }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        SearchBar(
            query = query,
            onQueryChange = { newQuery ->
                query = newQuery
                // Si la búsqueda no está vacía, llama a la API
                if (newQuery.isNotEmpty()) {
                    mapViewModel.textSearchPlaces(
                        apiKey = BuildConfig.MAPS_API_KEY,
                        query = newQuery,
                        location = zacatecas,
                        radius = 5000 
                    )
                } else {
                    // Si la búsqueda se borra, limpia los resultados
                    mapViewModel.places.value = emptyList()
                }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        FilterChips(
            selectedType = selectedType,
            onTypeSelected = { type ->
                selectedType = type
                if (type == "all") {
                    mapViewModel.searchAllCategories(
                        apiKey = BuildConfig.MAPS_API_KEY,
                        location = zacatecas,
                        radius = 5000
                    )
                } else {
                    mapViewModel.searchNearbyPlaces(
                        apiKey = BuildConfig.MAPS_API_KEY,
                        location = zacatecas,
                        type = type,
                        radius = 5000
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        MapPlaceholder(locations = places.map { it.location })

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(places, key = { it.id }) { place ->
                LocationCard(
                    place = place,
                    onViewRoute = { mapViewModel.selectPlace(place) }
                )
            }
        }
    }
}