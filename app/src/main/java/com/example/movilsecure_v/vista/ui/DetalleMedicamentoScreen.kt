package com.example.movilsecure_v.vista.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.movilsecure_v.viewmodel.MedicamentoDisplayInfo
import com.example.movilsecure_v.viewmodel.MedicamentosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleMedicamentoScreen(
    viewModel: MedicamentosViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    medicamentoId: Int
) {
    LaunchedEffect(medicamentoId) {
        viewModel.ObtenerDetallesMedicamento(medicamentoId)
    }

    val medicamentoInfo by viewModel.medicamentoActual.collectAsState()
    var mostrarDialogoConfirmacion by remember { mutableStateOf(false) }

    if (mostrarDialogoConfirmacion) {
        mostrarConfirmacionEliminar(
            onConfirm = {
                viewModel.eliminarMedicamento(medicamentoId)
                onBack() // Vuelve a la pantalla anterior
            },
            onDismiss = { mostrarDialogoConfirmacion = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Medicamento") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        if (medicamentoInfo == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            MostrarDetallesCompleto(
                med = medicamentoInfo!!,
                onEliminarClick = { mostrarDialogoConfirmacion = true },
                modifier = Modifier.padding(padding)
            )
        }
    }
}


@Composable
private fun MostrarDetallesCompleto(
    med: MedicamentoDisplayInfo,
    onEliminarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(med.medicamento.nombre, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))

        // --- Tarjetas de Información ---
        InfoCard(
            label = "Horario de inicio",
            value = med.medicamento.horaInicio,
            icon = Icons.Default.Schedule
        )
        InfoCard(
            label = "Intervalo entre dosis",
            value = "Cada ${med.medicamento.frecuencia} horas",
            icon = Icons.Default.Timelapse
        )
        // ... (resto de InfoCards)
        InfoCard(
            label = "Próximas tomas",
            value = med.proximasTomas,
            icon = Icons.Default.EventAvailable
        )

        Spacer(modifier = Modifier.weight(1f)) // Empuja el botón hacia abajo

        Button(
            onClick = onEliminarClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Eliminar Medicamento")
        }
    }
}

/**
 * Corresponde a mostrarConfirmacionEliminar() del diagrama.
 */
@Composable
private fun mostrarConfirmacionEliminar(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar Eliminación") },
        text = { Text("¿Estás seguro de que deseas eliminar este medicamento? Esta acción no se puede deshacer.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun InfoCard(
    label: String,
    value: String,
    icon: ImageVector,
    isHighlighted: Boolean = false
) {
    val backgroundColor = if (isHighlighted) Color(0xFFFFE0B2) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(label, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
                Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
