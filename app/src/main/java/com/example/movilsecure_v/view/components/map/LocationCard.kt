package com.example.movilsecure_v.view.components.map

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.example.movilsecure_v.view.screens.Establecimiento

// Función auxiliar para obtener la etiqueta y el color del tipo de establecimiento
private fun getTypeInfo(type: String): Pair<String, Color> {
    return when (type) {
        "hospital" -> "Hospitales" to Color(0xFFFEE2E2) // Rojo claro
        "clinic" -> "Clínicas" to Color(0xFFE0E7FF) // Azul claro
        "pharmacy" -> "Farmacias" to Color(0xFFD1FAE5) // Verde claro
        else -> "Desconocido" to Color.LightGray
    }
}

@Composable
fun LocationCard(
    establecimiento: Establecimiento,
    onViewRoute: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp) // Espacio uniforme entre elementos
        ) {
            // --- Fila 1: Nombre y Distancia ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = establecimiento.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = establecimiento.distance,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // --- Fila 2: Etiqueta de tipo ---
            val (typeLabel, typeColor) = getTypeInfo(establecimiento.type)
            SuggestionChip(
                onClick = { /* No hace nada */ },
                label = { Text(typeLabel) },
                colors = SuggestionChipDefaults.suggestionChipColors(containerColor = typeColor)
            )

            // --- Fila 3: Dirección y Horario ---
            InfoRow(icon = Icons.Default.LocationOn, text = establecimiento.address)
            InfoRow(icon = Icons.Default.Schedule, text = establecimiento.hours)

            // --- Fila 4: Tiempos de viaje ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoRow(icon = Icons.AutoMirrored.Filled.DirectionsWalk, text = establecimiento.walkingTime)
                InfoRow(icon = Icons.Default.DirectionsBus, text = establecimiento.busTime)
                InfoRow(icon = Icons.Default.DirectionsCar, text = establecimiento.carTime)
            }

            // --- Fila 5: Botón ---
            Button(
                onClick = onViewRoute,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz, // Icono similar al de la imagen
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ver ruta", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Composable auxiliar para las filas con icono y texto
@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
