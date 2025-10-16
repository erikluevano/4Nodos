package com.example.movilsecure_v.view.screens

import android.location.Geocoder
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import java.util.*
import com.example.movilsecure_v.model.entities.UbicacionResult


// Un data class simple para empaquetar el resultado


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeleccionarUbicacionScreen(
    onUbicacionSeleccionada: (UbicacionResult) -> Unit,
    onCancelar: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 1. Estado para guardar la ubicación del marcador y la dirección encontrada
    var marcadorPosicion by remember { mutableStateOf<LatLng?>(null) }
    var direccionEncontrada by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // 2. Estado de la cámara para centrar el mapa
    val posicionInicial = LatLng(22.7709, -102.5833)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(posicionInicial, 10f)
    }

    // 3. Efecto que se lanza cuando 'marcadorPosicion' cambia para buscar la dirección
    LaunchedEffect(marcadorPosicion) {
        marcadorPosicion?.let { latLng ->
            isLoading = true
            coroutineScope.launch {
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    // La geocodificación puede ser lenta, por eso se hace en una corrutina
                    val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                    direccionEncontrada = if (addresses?.isNotEmpty() == true) {
                        addresses[0].getAddressLine(0) // Obtiene la primera línea de la dirección
                    } else {
                        "Dirección no encontrada"
                    }
                } catch (e: Exception) {
                    direccionEncontrada = "Error al buscar dirección"
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Selecciona una ubicación") },
                navigationIcon = { /* Podrías poner un botón de cerrar aquí si lo deseas */ }
            )
        },
        floatingActionButton = {
            // 4. Botón para confirmar la selección. Solo se activa si hay una dirección válida.
            Button(
                onClick = {
                    val posicion = marcadorPosicion
                    val direccion = direccionEncontrada
                    if (posicion != null && !direccion.isNullOrBlank() && direccion != "Dirección no encontrada") {
                        onUbicacionSeleccionada(
                            UbicacionResult(
                                direccion = direccion,
                                latitud = posicion.latitude,
                                longitud = posicion.longitude
                            )
                        )
                    }
                },
                enabled = marcadorPosicion != null && !isLoading && !direccionEncontrada.isNullOrEmpty()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Confirmar Ubicación")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            // 5. El mapa de Google que ocupa toda la pantalla
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                // Al hacer un click largo, actualizamos la posición del marcador
                onMapLongClick = { latLng ->
                    marcadorPosicion = latLng
                    direccionEncontrada = null // Limpia la dirección anterior
                }
            ) {
                // 6. Dibuja el marcador en el mapa si hay una posición seleccionada
                marcadorPosicion?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "Ubicación seleccionada",
                        snippet = direccionEncontrada ?: "Buscando dirección..."
                    )
                }
            }
        }
    }
}