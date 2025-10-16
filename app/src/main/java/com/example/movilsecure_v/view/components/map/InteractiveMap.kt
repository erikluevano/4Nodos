package com.example.movilsecure_v.view.components.map

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun InteractiveMap(
    modifier: Modifier = Modifier,
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
                .height(250.dp), // Dale una altura considerable para que sea usable
            cameraPositionState = cameraPositionState,
            onPOIClick = { poi ->
                // Cuando se hace clic en un POI, llamamos a la lambda
                // con el ID del lugar para que el ViewModel lo procese.
                onPOIClick(poi.placeId)
            }
        )
    }
}