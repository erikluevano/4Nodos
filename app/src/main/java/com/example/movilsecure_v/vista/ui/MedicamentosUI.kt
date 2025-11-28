package com.example.movilsecure_v.vista.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.* 
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.movilsecure_v.modelo.entidades.Medicamento
import com.example.movilsecure_v.viewmodel.MedicamentoDisplayInfo
import com.example.movilsecure_v.viewmodel.MedicamentosGlobalState
import com.example.movilsecure_v.viewmodel.MedicamentosViewModel
import com.example.movilsecure_v.viewmodel.tiposDeMedicamento

@Composable
fun MedicamentosUI(
    viewModel: MedicamentosViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var selectedMedicamentoId by remember { mutableStateOf<Int?>(null) }

    val medicamentoDetalle by viewModel.medicamentoActual.collectAsState()

    if (selectedMedicamentoId == null) {
        // --- VISTA DE LISTA ---
        SolicitarListaMedicamentos(viewModel)

        uiState.mensajeError?.let {
            MostrarMensaje(mensaje = it, context = context)
            viewModel.onMensajeErrorMostrado()
        }

        Column(
            modifier = modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EncabezadoMedicamentos()
            Spacer(modifier = Modifier.height(24.dp))
            mostrarBoton_AgregarMedicamento(viewModel = viewModel)
            Spacer(modifier = Modifier.height(32.dp))
            val lista by viewModel.listaMedicamentos.collectAsState()
            MostrarListaInicial(lista = lista, onMedicamentoClick = { id -> selectedMedicamentoId = id })
        }

        if (uiState.mostrandoFormulario) {
            MostrarFormulario(
                uiState = uiState,
                viewModel = viewModel,
                onDismiss = { viewModel.ocultarFormulario() }
            )
        }

        if (uiState.mostrandoDialogoExito) {
            DialogoExito(onDismiss = { viewModel.ocultarDialogoExito() })
        }
    } else {
        // --- VISTA DE DETALLE ---
        val currentId = selectedMedicamentoId!!
        LaunchedEffect(currentId) {
            viewModel.ObtenerDetallesMedicamento(currentId)
        }
        
        MostrarDetallesCompleto(
            viewModel = viewModel,
            med = medicamentoDetalle,
            onBack = {
                selectedMedicamentoId = null
                viewModel.limpiarDetalle()
            }
        )
    }
}

@Composable
fun mostrarBoton_AgregarMedicamento(viewModel: MedicamentosViewModel) {
    Button(
        onClick = { SolicitarFormularioRegistro(viewModel) },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
    ) {
        Text("+ Agregar Medicamento", modifier = Modifier.padding(8.dp))
    }
}

fun SolicitarFormularioRegistro(viewModel: MedicamentosViewModel) {
    viewModel.solicitarFormulario()
}

@Composable
fun MostrarListaInicial(lista: List<MedicamentoDisplayInfo>, onMedicamentoClick: (Int) -> Unit) {
    if (lista.isEmpty()) {
        MensajeBienvenida()
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(lista) { medInfo ->
                TarjetaMedicamento(medInfo, onClick = { onMedicamentoClick(medInfo.medicamento.ID) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MostrarFormulario( // <- Se quitó 'private'
    uiState: MedicamentosGlobalState, 
    viewModel: MedicamentosViewModel, 
    onDismiss: () -> Unit,
    onMedicamentoGuardado: (Medicamento) -> Unit = {} // <- Se añadió este parámetro
) {
    var tipoMenuAbierto by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Este método se mantiene intacto
    fun EnviarDatosFormulario(medicamento: Medicamento) {
        viewModel.EnviarFormularioRegistro(medicamento)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large, tonalElevation = 8.dp) {
            Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                Text("+ Añadir Medicamento", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(24.dp))

                Text("Nombre del medicamento", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                TextField(
                    modifier = Modifier.fillMaxWidth(), 
                    value = uiState.nombre, 
                    onValueChange = viewModel::onNombreChange, 
                    placeholder = { Text("Ej: Losartán") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Tipo", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                ExposedDropdownMenuBox(expanded = tipoMenuAbierto, onExpandedChange = { tipoMenuAbierto = !tipoMenuAbierto }) {
                    TextField(
                        modifier = Modifier.menuAnchor().fillMaxWidth(), 
                        value = uiState.tipoMedicamento, 
                        onValueChange = {}, 
                        readOnly = true, 
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tipoMenuAbierto) }
                    )
                    ExposedDropdownMenu(expanded = tipoMenuAbierto, onDismissRequest = { tipoMenuAbierto = false }) {
                        tiposDeMedicamento.forEach { selection ->
                            DropdownMenuItem(text = { Text(selection) }, onClick = { viewModel.onTipoMedicamentoChange(selection); tipoMenuAbierto = false })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text("Hora de primera toma", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                TextField(
                    modifier = Modifier.fillMaxWidth(), 
                    value = uiState.horaInicio, 
                    onValueChange = viewModel::onHoraInicioChange, 
                    placeholder = { Text("Formato 24h (HH:MM)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text("Frecuencia (horas)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                TextField(
                    modifier = Modifier.fillMaxWidth(), 
                    value = uiState.frecuencia, 
                    onValueChange = viewModel::onFrecuenciaChange, 
                    placeholder = { Text("Ej: 8, 12, 24") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // SECCIÓN DE NOTIFICACIONES ELIMINADA DE LA UI

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (uiState.nombre.isBlank() || uiState.horaInicio.isBlank() || uiState.frecuencia.isBlank()) {
                            viewModel.mostrarMensajeError("Todos los campos son obligatorios.")
                        } else if (!"^([01]\\d|2[0-3]):([0-5]\\d)$".toRegex().matches(uiState.horaInicio)) {
                            viewModel.mostrarMensajeError("El formato de la hora debe ser HH:MM.")
                        } else {
                            val nuevoMedicamento = Medicamento(
                                nombre = uiState.nombre, 
                                tipoMedicamento = uiState.tipoMedicamento, 
                                horaInicio = uiState.horaInicio, 
                                frecuencia = uiState.frecuencia,
                                notificacionesActivas = uiState.activarNotificaciones // Este valor se mantiene por consistencia de la entidad, aunque no se vea
                            )
                            onMedicamentoGuardado(nuevoMedicamento) // <- Se notifica a quien llamó al formulario
                            EnviarDatosFormulario(nuevoMedicamento)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                ) { Text("Guardar") }
                OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
            }
        }
    }
}

@Composable
fun SolicitarListaMedicamentos(viewModel: MedicamentosViewModel) {
    LaunchedEffect(key1 = Unit) {
        viewModel.ObtenerListaMedicamentos()
    }
}

fun MostrarMensaje(mensaje: String, context: Context) {
    Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MostrarDetallesCompleto(
    viewModel: MedicamentosViewModel,
    med: MedicamentoDisplayInfo?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mostrarDialogoConfirmacion by remember { mutableStateOf(false) }

    if (mostrarDialogoConfirmacion && med != null) {
        mostrarConfirmacionEliminar(
            onConfirm = {
                viewModel.eliminarMedicamento(med.medicamento.ID)
                onBack()
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
        if (med == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(med.medicamento.nombre, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))

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
                InfoCard(
                    label = "Tipo de medicamento",
                    value = med.medicamento.tipoMedicamento,
                    icon = Icons.Default.Medication
                )
                InfoCard(
                    label = "Tiempo para siguiente toma",
                    value = med.tiempoRestante,
                    icon = Icons.Default.AccessTime,
                    isHighlighted = true
                )
                InfoCard(
                    label = "Próximas tomas",
                    value = med.proximasTomas,
                    icon = Icons.Default.EventAvailable
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { mostrarDialogoConfirmacion = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar Medicamento")
                }
            }
        }
    }
}

@Composable
private fun EncabezadoMedicamentos() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Medication, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Medicamentos", style = MaterialTheme.typography.headlineSmall)
    }
    Text("Gestiona los medicamentos y horarios de toma.", style = MaterialTheme.typography.bodyMedium)
}

@Composable
private fun MensajeBienvenida() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = Icons.Default.Medication, contentDescription = null, modifier = Modifier.size(100.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(16.dp))
        Text("No hay medicamentos registrados", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Presiona el botón verde para añadir un medicamento.", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
    }
}

@Composable
fun TarjetaMedicamento(medInfo: MedicamentoDisplayInfo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Nombre del medicamento con ícono
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Medication,
                    contentDescription = "Icono de pastilla",
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(medInfo.medicamento.nombre, style = MaterialTheme.typography.titleLarge)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Tiempo restante para la siguiente toma ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE0B2)),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Tiempo restante para la siguiente toma:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.AccessTime, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            medInfo.tiempoRestante.split(", ").forEach { toma ->
                                Text(
                                    text = toma,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 24.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Próximas tomas ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Próximas horas de tratamiento:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    medInfo.proximasTomas.split(", ").forEach { hora ->
                        Text(
                            text = hora,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun DialogoExito(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF388E3C), modifier = Modifier.size(48.dp)) },
        title = { Text("¡Medicamento añadido con éxito!", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
        text = { Text("El medicamento ha sido guardado correctamente y ya aparece en tu lista.", textAlign = TextAlign.Center) },
        confirmButton = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))) {
                    Text("Aceptar")
                }
            }
        }
    )
}

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
