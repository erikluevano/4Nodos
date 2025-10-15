package com.example.movilsecure_v.view.components.map

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

@Composable
fun MapPlaceholder(locations: List<LatLng> = emptyList()) {
    // 1. Mapa centrado en Zacatecas
    val zacatecas = LatLng(22.7709, -102.5833)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(zacatecas, 12f) // Un zoom más cercano
    }

    GoogleMap(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        cameraPositionState = cameraPositionState
    ) {
        // 2. Itera sobre la lista de ubicaciones y crea un marcador para cada una
        locations.forEach { location ->
            Marker(
                state = rememberMarkerState(position = location),
                title = "Ubicación",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
            )
        }
    }
}