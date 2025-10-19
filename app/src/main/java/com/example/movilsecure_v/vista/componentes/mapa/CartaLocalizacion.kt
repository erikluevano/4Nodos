package com.example.movilsecure_v.vista.componentes.mapa

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.example.movilsecure_v.modelo.entidades.PlaceDetails
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri

@Composable
fun LocationCard(
    place: PlaceDetails,
    onViewRoute: () -> Unit
) {
    val context = LocalContext.current

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = place.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Normal
            )

            InfoRow(icon = Icons.Default.LocationOn, text = place.address)
            InfoRow(icon = Icons.Default.Schedule, text = place.isOpen)
            place.rating?.let {
                InfoRow(icon = Icons.Default.Star, text = "Calificación: $it")
            }

            // --- Fila de Botones ---
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Botón Ver Ruta
                Button(
                    onClick = onViewRoute,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ver ruta")
                }

                // Botón Llamar
                OutlinedButton(
                    onClick = {
                        place.phoneNumber?.let {
                            val intent = Intent(Intent.ACTION_DIAL, "tel:$it".toUri())
                            context.startActivity(intent)
                        } ?: Toast.makeText(context, "Número no disponible", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Llamar")
                }
            }
        }
    }
}

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
