package com.example.movilsecure_v.view.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.movilsecure_v.BuildConfig
import com.example.movilsecure_v.view.components.map.*
import com.example.movilsecure_v.viewmodel.MapViewModel
import com.google.android.gms.maps.model.LatLng
import androidx.core.net.toUri

@Composable
fun MapaScreen(modifier: Modifier = Modifier, mapViewModel: MapViewModel = viewModel()) {
    var query by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("all") }
    var showMap by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val zacatecas = LatLng(22.7709, -102.5833)

    // Observamos todos los estados relevantes del ViewModel
    val places by mapViewModel.places
    val selectedCardPlace by mapViewModel.selectedCardPlace
    val selectedPlaceForRoute by mapViewModel.selectedPlaceForRoute

    // El diálogo de ruta sigue funcionando igual
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
        // --- SECCIÓN MODIFICADA ---
        SearchBar(
            query = query,
            // onQueryChange AHORA SOLO ACTUALIZA EL ESTADO
            onQueryChange = { newQuery ->
                query = newQuery
            },
            // onSearch AHORA CONTIENE TODA LA LÓGICA DE BÚSQUEDA
            onSearch = {
                // 1. Al buscar, limpiamos la selección del mapa
                mapViewModel.clearSelectedCardPlace()

                if (query.isNotBlank()) { // Usamos isNotBlank para ignorar espacios en blanco
                    // 2. Llamamos a la función de búsqueda con el 'query' actual
                    mapViewModel.textSearchPlaces(
                        apiKey = BuildConfig.MAPS_API_KEY,
                        query = query,
                        location = zacatecas,
                        radius = 10000
                    )
                } else {
                    // 3. Si la búsqueda está vacía, limpiamos la lista
                    mapViewModel.places.value = emptyList()
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))
        FilterChips(
            selectedType = selectedType,
            onTypeSelected = { type ->
                // La lógica de filtros podría también limpiar la selección del mapa
                mapViewModel.clearSelectedCardPlace()
                // ... (tu lógica de filtros aquí) ...
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                showMap = !showMap
                if (showMap) {
                    // Si mostramos el mapa, limpiamos los resultados de búsqueda
                    query = "" // Opcional: limpiar también el texto de búsqueda
                    mapViewModel.places.value = emptyList()
                } else {
                    // Si ocultamos el mapa, limpiamos la tarjeta seleccionada
                    mapViewModel.clearSelectedCardPlace()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (showMap) "Ocultar mapa" else "Seleccionar ubicación en mapa")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- LÓGICA DE VISUALIZACIÓN CONDICIONAL ---

        // Si se muestra el mapa, lo dibujamos
        if (showMap) {
            InteractiveMap(
                onPOIClick = { placeId ->
                    mapViewModel.getPlaceDetailsById(BuildConfig.MAPS_API_KEY, placeId)
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Si hay un lugar seleccionado DESDE EL MAPA, mostramos su tarjeta
        if (selectedCardPlace != null) {
            LocationCard(
                place = selectedCardPlace!!,
                onViewRoute = { mapViewModel.selectPlaceForRoute(selectedCardPlace!!) }
            )
        }
        // Si NO hay selección de mapa Y hay resultados DE LA BÚSQUEDA, mostramos la lista
        else if (places.isNotEmpty()) {
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