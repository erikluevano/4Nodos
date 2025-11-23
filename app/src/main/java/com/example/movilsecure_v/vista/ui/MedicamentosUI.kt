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
    onMedicamentoClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    SolicitarListaMedicamentos(viewModel)

    uiState.mensajeError?.let {
        MostrarMensaje(mensaje = it, context = context)
        viewModel.onMensajeErrorMostrado()
    }

    if (false) {
        MostrarDetallesCompleto(med = Medicamento(nombre = "", tipoMedicamento = "", horaInicio = "", frecuencia = ""))
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
        MostrarListaInicial(lista = lista, onMedicamentoClick = onMedicamentoClick)
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
fun MostrarFormulario(uiState: MedicamentosGlobalState, viewModel: MedicamentosViewModel, onDismiss: () -> Unit) {
    var tipoMenuAbierto by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

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
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.onActivarNotificacionesChange(!uiState.activarNotificaciones) }
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.padding(end = 16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Activar notificaciones")
                        Text(
                            "Recibe recordatorios en los horarios de la toma.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.activarNotificaciones,
                        onCheckedChange = viewModel::onActivarNotificacionesChange
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (uiState.nombre.isBlank() || uiState.horaInicio.isBlank() || uiState.frecuencia.isBlank()) {
                            viewModel.mostrarMensajeError("Todos los campos son obligatorios.")
                        } else if (!"^([01]\\d|2[0-3]):([0-5]\\d)$".toRegex().matches(uiState.horaInicio)) {
                            viewModel.mostrarMensajeError("El formato de la hora debe ser HH:MM.")
                        } else {
                            val nuevoMedicamento = Medicamento(
                                nombre = uiState.nombre, tipoMedicamento = uiState.tipoMedicamento, 
                                horaInicio = uiState.horaInicio, frecuencia = uiState.frecuencia, 
                                notificacionesActivas = uiState.activarNotificaciones
                            )
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

@Composable
fun MostrarDetallesCompleto(med: Medicamento) {
    Text(text = "Placeholder para detalles de ${med.nombre}", color = Color.Transparent)
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
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Medication, contentDescription = "Icono de pastilla", modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text(medInfo.medicamento.nombre, style = MaterialTheme.typography.titleLarge)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE0B2)),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Tiempo restante para la siguiente toma:", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(medInfo.tiempoRestante, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Próximas horas de tratamiento:", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(medInfo.proximasTomas, style = MaterialTheme.typography.bodyMedium, lineHeight = 20.sp)
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
