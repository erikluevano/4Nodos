package com.example.movilsecure_v.view.components.map

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.movilsecure_v.model.entities.PlaceDetails
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun InteractiveMap(
    modifier: Modifier = Modifier,
    places: List<PlaceDetails>,
    onPOIClick: (placeId: String) -> Unit
) {
    val zacatecas = LatLng(22.7709, -102.5833)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(zacatecas, 12f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        GoogleMap(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            cameraPositionState = cameraPositionState,
            onPOIClick = { poi ->
                onPOIClick(poi.placeId)
            }
        ) {
            // --- NUEVA LÓGICA ---
            // Iteramos sobre la lista de lugares y creamos un marcador para cada uno
            places.forEach { place ->
                Marker(
                    state = MarkerState(position = place.location),
                    title = place.name,
                    snippet = place.address
                )
            }
        }
    }
}