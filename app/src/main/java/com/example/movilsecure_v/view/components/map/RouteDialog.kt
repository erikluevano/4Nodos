package com.example.movilsecure_v.view.components.map

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.movilsecure_v.model.entities.PlaceDetails
// NUEVAS IMPORTACIONES PARA EL MAPA
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun RouteDialog(
    place: PlaceDetails,
    onClose: () -> Unit,
    onStartNavigation: () -> Unit
) {
    // Se centra en la ubicación del lugar con un zoom adecuado
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(place.location, 15f)
    }

    Dialog(onDismissRequest = onClose) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // 1. Encabezado del diálogo
                DialogHeader(locationName = place.name, onClose = onClose)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Confirma la ubicación para iniciar la navegación.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(20.dp))

                // 2. MAPA DE GOOGLE
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp) // Altura del mapa dentro del diálogo
                        .clip(RoundedCornerShape(12.dp)) // Esquinas redondeadas para el mapa
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState
                    ) {
                        // Marcador en la ubicación del destino
                        Marker(
                            state = MarkerState(position = place.location),
                            title = place.name,
                            snippet = place.address
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 3. Botones de acción
                DialogActions(onClose = onClose, onStartNavigation = onStartNavigation)
            }
        }
    }
}

@Composable
private fun DialogHeader(locationName: String, onClose: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.NearMe,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Ruta a $locationName",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onClose) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
        }
    }
}

@Composable
private fun DialogActions(onClose: () -> Unit, onStartNavigation: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onClose,
            modifier = Modifier.weight(1f)
        ) {
            Text("Cerrar")
        }
        Button(
            onClick = onStartNavigation,
            modifier = Modifier.weight(1.8f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NearMe,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Iniciar navegación")
            }
        }
    }
}