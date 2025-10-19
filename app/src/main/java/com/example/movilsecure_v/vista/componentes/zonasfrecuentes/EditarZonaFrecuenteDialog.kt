package com.example.movilsecure_v.vista.componentes.zonasfrecuentes

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.movilsecure_v.modelo.entidades.UbicacionResultado
import com.example.movilsecure_v.modelo.entidades.ZonaFrecuente
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

/**
 * Un diálogo componible para modificar una Zona Frecuente existente.
 *
 * @param zona La zona frecuente original que se va a editar.
 * @param onDismissRequest Se llama cuando el usuario solicita cerrar el diálogo.
 * @param onGuardarCambios Se llama cuando el usuario pulsa "Guardar cambios", proporcionando la zona con los datos actualizados.
 * @param onSeleccionarUbicacionClick Se llama cuando el usuario pulsa para seleccionar una nueva ubicación en el mapa.
 * @param ubicacionInicial La nueva ubicación seleccionada desde el mapa (si existe).
 */
@Composable
fun EditarZonaFrecuenteDialog(
    zona: ZonaFrecuente,
    onDismissRequest: () -> Unit,
    onGuardarCambios: (ZonaFrecuente) -> Unit,
    onSeleccionarUbicacionClick: () -> Unit,
    ubicacionInicial: UbicacionResultado? = null
) {
    // --- 1. GESTIÓN DEL ESTADO INTERNO ---
    var nombre by remember(zona.id) { mutableStateOf(zona.nombreZona) }
    var nota by remember(zona.id) { mutableStateOf(zona.notaZona ?: "") }

    var ubicacionSeleccionada by remember(ubicacionInicial) {
        mutableStateOf(ubicacionInicial)
    }

    // --- 2. VALIDACIÓN DEL FORMULARIO ---
    val isFormValid = nombre.isNotBlank()

    // --- 3. CONSTRUCCIÓN DEL DIÁLOGO ---
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- Encabezado ---
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Editar zona frecuente", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Modifica los datos de la zona seleccionada.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar diálogo")
                    }
                }

                // --- Selector de Ubicación con Mapa y Overlay Clicable ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clip(RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // CAPA 1: EL MAPA (SÓLO VISUAL)
                    val latitudMostrada = ubicacionSeleccionada?.latitud ?: zona.latitud
                    val longitudMostrada = ubicacionSeleccionada?.longitud ?: zona.longitud
                    val posicion = LatLng(latitudMostrada, longitudMostrada)
                    val cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(posicion, 15f)
                    }
                    LaunchedEffect(posicion) {
                        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(posicion, 15f))
                    }

                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        googleMapOptionsFactory = {
                            GoogleMapOptions().liteMode(true)
                        },
                        uiSettings = MapUiSettings(mapToolbarEnabled = false)
                    ) {
                        Marker(
                            state = MarkerState(position = posicion),
                            title = if (ubicacionSeleccionada != null) "Nueva ubicación" else "Ubicación actual"
                        )
                    }

                    // CAPA 2: EL BOTÓN TRANSPARENTE QUE CAPTURA EL CLIC
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { onSeleccionarUbicacionClick() }
                    )
                }


                // --- Campos de texto adicionales ---
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de la zona *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = nota,
                    onValueChange = { nota = it },
                    label = { Text("Nota (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // --- Botones de acción ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    OutlinedButton(onClick = onDismissRequest) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            val zonaActualizada = zona.copy(
                                nombreZona = nombre.trim(),
                                direccionZona = ubicacionSeleccionada?.direccion?.trim() ?: zona.direccionZona,
                                latitud = ubicacionSeleccionada?.latitud ?: zona.latitud,
                                longitud = ubicacionSeleccionada?.longitud ?: zona.longitud,
                                notaZona = nota.trim().takeIf { it.isNotEmpty() }
                            )
                            onGuardarCambios(zonaActualizada)
                        },
                        enabled = isFormValid
                    ) {
                        Text("Guardar cambios")
                    }
                }
            }
        }
    }
}