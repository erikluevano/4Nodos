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

// La clase Establecimiento ya no es necesaria, la eliminaremos.
// data class Establecimiento(...)

@Composable
fun MapaScreen(modifier: Modifier = Modifier, mapViewModel: MapViewModel = viewModel()) {
    var query by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("all") }

    val zacatecas = LatLng(22.7709, -102.5833)
    val places by mapViewModel.places
    val selectedPlace by mapViewModel.selectedPlace

    // Muestra el RouteDialog si hay un lugar seleccionado
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
        SearchBar(query = query, onQueryChange = { query = it })
        Spacer(modifier = Modifier.height(8.dp))
        FilterChips(
            selectedType = selectedType,
            onTypeSelected = { type ->
                selectedType = type
                if (type != "all") {
                    mapViewModel.searchNearbyPlaces(
                        apiKey = BuildConfig.MAPS_API_KEY,
                        location = zacatecas,
                        type = type,
                        radius = 5000
                    )
                } else {
                    mapViewModel.places.value = emptyList()
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // El mapa ahora extrae las coordenadas de la lista de lugares
        MapPlaceholder(locations = places.map { it.location })

        Spacer(modifier = Modifier.height(12.dp))

        // Lista de resultados
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(places) { place ->
                LocationCard(
                    place = place,
                    onViewRoute = { mapViewModel.selectPlace(place) }
                )
            }
        }
    }
}