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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.movilsecure_v.model.entities.UbicacionResult
import com.example.movilsecure_v.model.entities.ZonaFrecuente

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
    ubicacionInicial: UbicacionResult? = null
) {
    // --- 1. GESTIÓN DEL ESTADO INTERNO ---
    var nombre by remember(zona.id) { mutableStateOf(zona.nombreZona) }
    var nota by remember(zona.id) { mutableStateOf(zona.notaZona ?: "") }

    // El estado de la ubicación seleccionada se actualiza si se proporciona una nueva (desde el mapa).
    var ubicacionSeleccionada by remember(ubicacionInicial) {
        mutableStateOf(ubicacionInicial)
    }

    // --- 2. VALIDACIÓN DEL FORMULARIO ---
    // El formulario es válido si el nombre no está vacío. La ubicación siempre tendrá un valor,
    // ya sea la original de la 'zona' o una nueva seleccionada.
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

                // --- Selector de Ubicación ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onSeleccionarUbicacionClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        // El texto muestra la nueva ubicación si se ha seleccionado una,
                        // o la dirección original de la zona si no.
                        val texto = when {
                            ubicacionSeleccionada != null -> "Nueva ubicación:\n$ubicacionSeleccionada"
                            else -> "Ubicación actual:\n${zona.direccionZona ?: "No especificada"}"
                        }
                        Text(text = texto, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
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
                            // Si se seleccionó una nueva ubicación, se usan sus datos.
                            // Si no, se mantienen los datos de la zona original.
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