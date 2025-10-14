package com.example.movilsecure_v.view.components.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.movilsecure_v.view.screens.Establecimiento

@Composable
fun RouteDialog(
    location: Establecimiento,
    onClose: () -> Unit,
    onStartNavigation: () -> Unit
) {
    var selectedTransport by remember { mutableStateOf("car") }

    Dialog(onDismissRequest = onClose) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // 1. Encabezado del diálogo
                DialogHeader(locationName = location.name, onClose = onClose)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Elige tu medio de transporte preferido para ver la ruta.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(20.dp))

                // 2. Información de Origen y Destino
                LocationInfo(
                    label = "Desde",
                    details = "Tu ubicación actual",
                    color = Color(0xFF16A34A) // Verde
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 8.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LocationInfo(
                    label = "Hasta",
                    details = location.address,
                    color = Color(0xFFDC2626) // Rojo
                )
                Spacer(modifier = Modifier.height(24.dp))

                // 3. Opciones de Transporte
                Text(
                    "Medio de transporte",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TransportOption(
                        icon = Icons.Default.DirectionsCar,
                        label = "Auto",
                        distance = location.distance,
                        duration = location.carTime, // Se muestra porque tiene valor
                        isSelected = selectedTransport == "car",
                        onClick = { selectedTransport = "car" }
                    )
                    TransportOption(
                        icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                        label = "A pie",
                        distance = location.distance,
                        duration = location.walkingTime, // Se mostrará si no es nulo o vacío
                        isSelected = selectedTransport == "walk",
                        onClick = { selectedTransport = "walk" }
                    )
                    TransportOption(
                        icon = Icons.Default.DirectionsBus,
                        label = "Ruta",
                        distance = location.distance,
                        duration = location.busTime, // Se mostrará si no es nulo o vacío
                        isSelected = selectedTransport == "bus",
                        onClick = { selectedTransport = "bus" }
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

                // 4. Botones de acción
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
private fun LocationInfo(label: String, details: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(label, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(details, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun TransportOption(
    icon: ImageVector,
    label: String,
    distance: String,
    duration: String?, // Cambiado a opcional
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val borderColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = contentColor)
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, fontWeight = FontWeight.Bold, color = contentColor)
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Distancia",
                modifier = Modifier.size(16.dp),
                tint = contentColor.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(distance, fontSize = 14.sp, color = contentColor.copy(alpha = 0.7f))

            // Se muestra la duración solo si no es nula o vacía
            if (!duration.isNullOrEmpty()) {
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Duración",
                    modifier = Modifier.size(16.dp),
                    tint = contentColor.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(duration, fontSize = 14.sp, color = contentColor.copy(alpha = 0.7f))
            }
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
            modifier = Modifier.weight(1f) // Menos peso para este botón
        ) {
            Text("Cerrar")
        }
        Button(
            onClick = onStartNavigation,
            modifier = Modifier.weight(1.8f) // Aumentamos un poco más el peso para dar más espacio
        ) {
            // Usamos un Row para alinear el ícono y el texto horizontalmente
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


