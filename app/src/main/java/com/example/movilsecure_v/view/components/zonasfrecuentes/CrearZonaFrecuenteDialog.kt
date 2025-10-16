package com.example.movilsecure_v.view.components.zonasfrecuentes

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.movilsecure_v.model.UbicacionResult

private enum class AddMethod { MAP, TEXT }

/**
 * Un diálogo componible para añadir una nueva Zona Frecuente.
 * Es stateful internamente para manejar los campos del formulario.
 *
 * @param onDismissRequest Se llama cuando el usuario solicita cerrar el diálogo (tocando fuera o el botón de cerrar/cancelar).
 * @param onGuardarZona Se llama cuando el usuario pulsa "Guardar zona", proporcionando los datos del formulario.
 */
@Composable
fun CrearZonaFrecuenteDialog(
    ubicacionInicial: UbicacionResult? = null,
    onDismissRequest: () -> Unit,
    onGuardarZona: (nombre: String, direccion: String, lat: Double, lon: Double, nota: String?) -> Unit,
    onSeleccionarUbicacionClick: () -> Unit
) {
    // --- 1. GESTIÓN DEL ESTADO INTERNO ---
    var nombre by remember { mutableStateOf("") }
    var nota by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var addMethod by remember { mutableStateOf(AddMethod.MAP) }

    // Simula una ubicación seleccionada del mapa. En una implementación real,
    // esto se actualizaría al volver de la pantalla del mapa con un resultado.
    //var ubicacionSeleccionada by remember { mutableStateOf<String?>(null) }
    var ubicacionSeleccionada by remember(ubicacionInicial) {
        mutableStateOf(ubicacionInicial)
    }
    // --- 2. VALIDACIÓN DEL FORMULARIO ---
    //val isFormValid = nombre.isNotBlank() &&
      //      ( (addMethod == AddMethod.TEXT && direccion.isNotBlank()) ||
        //            (addMethod == AddMethod.MAP && ubicacionSeleccionada != null) )
    val isFormValid = nombre.isNotBlank() && ubicacionSeleccionada != null

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
                        Text("Añadir zona frecuente", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Registra una nueva ubicación como zona frecuente.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar diálogo")
                    }
                }

                // --- Input de Ubicación (Condicional) ---
                when(addMethod) {
                    AddMethod.MAP -> {
                        // Placeholder para seleccionar en mapa
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    onSeleccionarUbicacionClick()
                                    // TODO: Navegar a la pantalla del mapa para seleccionar una ubicación.
                                    // Por ahora, simulamos una selección para la prueba.
                                    //ubicacionSeleccionada = "Av. Siempre Viva 742"
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                val texto = when {
                                    ubicacionSeleccionada != null -> "Ubicación seleccionada:\n$ubicacionSeleccionada"
                                    else -> "Toca para seleccionar ubicación"
                                }
                                Text(text = texto, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    AddMethod.TEXT -> {
                        OutlinedTextField(
                            value = direccion,
                            onValueChange = { direccion = it },
                            label = { Text("Dirección *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                // --- Campos de texto adicionales ---
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de la zona *") },
                    placeholder = { Text("Ej: Casa de María, Centro médico...")},
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = nota,
                    onValueChange = { nota = it },
                    label = { Text("Nota (opcional)") },
                    placeholder = { Text("Ej: Visitamos los martes, portón azul...")},
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
                            val finalDireccion = if (addMethod == AddMethod.MAP) ubicacionSeleccionada!! else direccion
                            val ubicacion = ubicacionSeleccionada!!
                            onGuardarZona(
                                nombre.trim(),
                                ubicacion.direccion.trim(),
                                ubicacion.latitud,
                                ubicacion.longitud,
                                //finalDireccion.trim(),
                                nota.trim().takeIf { it.isNotEmpty() }
                            )
                        },
                        enabled = isFormValid
                    ) {
                        Text("Guardar zona")
                    }
                }
            }
        }
    }
}