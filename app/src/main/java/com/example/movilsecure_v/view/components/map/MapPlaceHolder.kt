package com.example.movilsecure_v.view.components.map

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapPlaceholder() {
    val lima = LatLng(-12.046374, -77.042793)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(lima, 10f)
    }

    GoogleMap(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        cameraPositionState = cameraPositionState
    ) {
        // Puedes agregar marcadores aquí en el futuro
    }
}