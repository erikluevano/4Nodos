package com.example.movilsecure_v.view.components.zonasfrecuentes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.movilsecure_v.model.entities.ZonaFrecuente

/**
 * Un diálogo componible para modificar una Zona Frecuente existente.
 *
 * @param zona La zona frecuente original que se va a editar.
 * @param onDismissRequest Se llama cuando el usuario solicita cerrar el diálogo.
 * @param onGuardarCambios Se llama cuando el usuario pulsa "Guardar cambios", proporcionando la zona con los datos actualizados.
 */
@Composable
fun EditarZonaFrecuenteDialog(
    zona: ZonaFrecuente,
    onDismissRequest: () -> Unit,
    onGuardarCambios: (ZonaFrecuente) -> Unit
) {
    // --- 1. GESTIÓN DEL ESTADO INTERNO ---
    // El estado se inicializa con los datos de la 'zona' que recibimos.
    // Usamos 'zona.id' como clave para que 'remember' reinicie el estado si el
    // diálogo se abre para una zona diferente, evitando datos obsoletos.
    var nombre by remember(zona.id) { mutableStateOf(zona.nombreZona) }
    var direccion by remember(zona.id) { mutableStateOf(zona.direccionZona ?: "") }
    var nota by remember(zona.id) { mutableStateOf(zona.notaZona ?: "") }

    // --- 2. VALIDACIÓN DEL FORMULARIO ---
    val isFormValid = nombre.isNotBlank() && direccion.isNotBlank()

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

                // --- Campos de texto (pre-rellenados) ---
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de la zona *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = direccion,
                    onValueChange = { direccion = it },
                    label = { Text("Dirección *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = nota,
                    onValueChange = { nota = it },
                    label = { Text("Nota (opcional)") },
                    placeholder = { Text("Ej: Visitamos los martes, portón azul...") },
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
                            // Usamos .copy() para crear un nuevo objeto ZonaFrecuente
                            // con los datos actualizados, manteniendo el id y coordenadas originales.
                            val zonaActualizada = zona.copy(
                                nombreZona = nombre.trim(),
                                direccionZona = direccion.trim(),
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