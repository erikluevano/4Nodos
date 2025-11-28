// Archivo: app/src/main/java/com/example/movilsecure_v/vista/ui/MedicamentosUI.kt
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
                onDismiss = { viewModel.ocultarFormulario() },
                // Lógica de guardado cuando se usa desde la pantalla de Medicamentos
                onMedicamentoGuardado = { nuevoMedicamento ->
                    viewModel.EnviarFormularioRegistro(nuevoMedicamento)
                    viewModel.ocultarFormulario()
                }
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
fun MostrarFormulario(
    uiState: MedicamentosGlobalState,
    viewModel: MedicamentosViewModel,
    onDismiss: () -> Unit,
    onMedicamentoGuardado: (Medicamento) -> Unit // <- Se mantiene el callback
) {
    var tipoMenuAbierto by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

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
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (uiState.nombre.isBlank() || uiState.horaInicio.isBlank() || uiState.frecuencia.isBlank()) {
                            viewModel.mostrarMensajeError("Todos los campos son obligatorios.")
                        } else if (!"^([01]\\d|2[0-3]):([0-5]\\d)$".toRegex().matches(uiState.horaInicio)) {
                            viewModel.mostrarMensajeError("El formato de la hora debe ser HH:MM.")
                        } else {
                            val nuevoMedicamento = Medicamento(
                                nombre = uiState.nombre.trim(),
                                tipoMedicamento = uiState.tipoMedicamento,
                                horaInicio = uiState.horaInicio,
                                frecuencia = uiState.frecuencia,
                                notificacionesActivas = uiState.activarNotificaciones
                            )
                            // CAMBIO CLAVE: Notifica al llamador con el nuevo medicamento.
                            // El llamador (PerfilScreen o MedicamentosUI) decide qué hacer.
                            onMedicamentoGuardado(nuevoMedicamento)
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

// El resto del archivo (SolicitarListaMedicamentos, MostrarDetallesCompleto, etc.) no necesita cambios.
// ... (pegar el resto de tu código de MedicamentosUI.kt aquí)
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
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(med.medicamento.nombre, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                InfoDetalle(Icons.Default.Category, "Tipo", med.medicamento.tipoMedicamento)
                InfoDetalle(Icons.Default.WatchLater, "Próxima toma en", med.tiempoRestante)
                InfoDetalle(Icons.Default.Event, "Próximas 3 tomas", med.proximasTomas)

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { mostrarDialogoConfirmacion = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = "Eliminar")
                    Spacer(Modifier.width(8.dp))
                    Text("Eliminar Medicamento")
                }
            }
        }
    }
}

@Composable
fun InfoDetalle(icon: ImageVector, label: String, value: String) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 32.dp))
    }
}


@Composable
fun mostrarConfirmacionEliminar(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar Eliminación") },
        text = { Text("¿Estás seguro de que quieres eliminar este medicamento? Esta acción no se puede deshacer.") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Eliminar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun EncabezadoMedicamentos() {
    Text(
        text = "Control de Medicamentos",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun MensajeBienvenida() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.MedicalServices,
            contentDescription = "Icono de bienvenida",
            modifier = Modifier.size(64.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("No hay medicamentos registrados.", fontSize = 18.sp, color = Color.Gray)
        Text("Usa el botón '+' para añadir el primero.", textAlign = TextAlign.Center, color = Color.Gray)
    }
}

@Composable
fun TarjetaMedicamento(medInfo: MedicamentoDisplayInfo, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(medInfo.medicamento.nombre, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Próxima toma en: ${medInfo.tiempoRestante}", color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "Ver detalles")
        }
    }
}

@Composable
fun DialogoExito(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¡Éxito!") },
        text = { Text("El medicamento se ha guardado correctamente.") },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Aceptar") } }
    )
}
