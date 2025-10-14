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
import com.example.movilsecure_v.view.components.map.FilterChips
import com.example.movilsecure_v.view.components.map.LocationCard
import com.example.movilsecure_v.view.components.map.MapPlaceholder
import com.example.movilsecure_v.view.components.map.RouteDialog
import com.example.movilsecure_v.view.components.map.SearchBar

data class Establecimiento(
    val id: Int,
    val name: String,
    val type: String,
    val address: String,
    val distance: String,
    val walkingTime: String,
    val busTime: String,
    val carTime: String,
    val hours: String
)

@Composable
fun MapaScreen(modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("all") }
    var showRouteDialog by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf<Establecimiento?>(null) }

    val locations = remember {
        listOf(
            Establecimiento(1, "Hospital General", "hospital", "Av. Salud 123, Centro", "1.2 km", "15 min", "8 min", "5 min", "24 horas"),
            Establecimiento(2, "Clínica San José", "clinic", "Calle Bienestar 456, Norte", "0.8 km", "10 min", "5 min", "3 min", "7:00 - 22:00"),
            Establecimiento(3, "Farmacia del Pueblo", "pharmacy", "Plaza Central 789, Centro", "0.3 km", "4 min", "2 min", "1 min", "24 horas")
        )
    }

    val filtered = locations.filter { loc ->
        (selectedType == "all" || loc.type == selectedType) &&
                (loc.name.contains(query, ignoreCase = true) || loc.address.contains(query, ignoreCase = true))
    }

    // 2. Aplicamos el modifier recibido al componente raíz (Column).
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Search bar + chips
        SearchBar(query = query, onQueryChange = { query = it })
        Spacer(modifier = Modifier.height(8.dp))
        FilterChips(selectedType = selectedType, onTypeSelected = { selectedType = it })

        Spacer(modifier = Modifier.height(12.dp))

        // Mapa placeholder (no implementas Google Map aún)
        MapPlaceholder(count = filtered.size)

        Spacer(modifier = Modifier.height(12.dp))

        // Lista de resultados
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filtered) { loc ->
                LocationCard(
                    establecimiento = loc,
                    onViewRoute = {
                        selectedLocation = loc
                        showRouteDialog = true
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // Dialog para seleccionar ruta (simulado)
    if (showRouteDialog && selectedLocation != null) {
        RouteDialog(
            location = selectedLocation!!,
            onClose = { showRouteDialog = false },
            onStartNavigation = {
                // Aquí iría la llamada al ViewModel para iniciar navegación
                showRouteDialog = false
            }
        )
    }
}
