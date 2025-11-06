package com.example.movilsecure_v.vista.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.movilsecure_v.modelo.Cita
import com.example.movilsecure_v.viewmodel.CitasViewModel
import com.example.movilsecure_v.viewmodel.FiltroCitas
import com.example.movilsecure_v.viewmodel.UiState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CitasUI(modifier: Modifier = Modifier, viewModel: CitasViewModel = viewModel()) {
    var mostrandoFormulario by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    var citaSeleccionada by remember { mutableStateOf<Cita?>(null) }
    var mostrandoDialogoDetalles by remember { mutableStateOf(false) }
    var citaParaEliminar by remember { mutableStateOf<Cita?>(null) }

    fun SolicitarFormularioRegistro() {
        mostrandoFormulario = true
    }

    fun EnviarDatosFormulario(fecha: String, hora: String, lugar: String, motivo: String) {
        viewModel.EnviarFormularioRegistro(fecha, hora, lugar, motivo)
    }

    suspend fun MostrarMensaje(mensaje: String) {
        snackbarHostState.showSnackbar(mensaje)
    }

    @Composable
    fun SolicitarListaCitas(): State<List<Cita>> {
        return viewModel.citas.collectAsState()
    }

    fun EnviarCriterioFiltro(criterio: FiltroCitas) {
        viewModel.cambiarFiltro(criterio)
    }

    fun AbrirDialogoDetalles(cita: Cita) {
        citaSeleccionada = cita
        mostrandoDialogoDetalles = true
    }

    fun cerrarDialogo() {
        mostrandoDialogoDetalles = false
        citaSeleccionada = null // Limpiar la selección
    }

    fun mostrarConfirmacion(cita: Cita) {
        cerrarDialogo()
        citaParaEliminar = cita
    }

    fun EliminarCita(cita: Cita) {
        viewModel.eliminarCita(cita) // Llamada al método del ViewModel
        citaParaEliminar = null // Limpiar la cita para eliminación
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is UiState.Exito -> {}
            is UiState.Error -> {
                MostrarMensaje(state.mensaje)
                viewModel.consumirUiState()
            }
            null -> {}
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (mostrandoFormulario) {
            MostrarFormulario(
                modifier = Modifier.padding(padding),
                onCancelar = { mostrandoFormulario = false },
                onConfirmar = { fecha, hora, lugar, motivo ->
                    EnviarDatosFormulario(fecha, hora, lugar, motivo)
                }
            )
        } else {
            val citas by SolicitarListaCitas()
            val filtroActivo by viewModel.filtroActual.collectAsState()
            MostrarOpcion_RegistroCita(
                modifier = Modifier.padding(padding),
                citas = citas,
                filtroActivo = filtroActivo,
                onRegistrarClick = { 
                    SolicitarFormularioRegistro()
                },
                onFiltroCambiado = { nuevoFiltro -> EnviarCriterioFiltro(nuevoFiltro) },
                onCitaClick = { cita -> AbrirDialogoDetalles(cita) }
            )
        }
    }

    if (uiState is UiState.Exito) {
        DialogoExitoRegistro {
            viewModel.consumirUiState()
            mostrandoFormulario = false
        }
    }

    if (mostrandoDialogoDetalles && citaSeleccionada != null) {
        DialogoDetallesCita(
            cita = citaSeleccionada!!,
            onDismiss = { cerrarDialogo() },
            onEliminar = { mostrarConfirmacion(citaSeleccionada!!) },
            onPosponer = { /* Lógica de posponer (pendiente) */ }
        )
    }

    if (citaParaEliminar != null) {
        DialogoConfirmacionEliminar(
            cita = citaParaEliminar!!,
            onConfirmar = { EliminarCita(citaParaEliminar!!) },
            onCancelar = { citaParaEliminar = null }
        )
    }
}

/**
 * 1. Muestro la pantalla principal de citas y el boton "+ Registrar cita".
 */
@Composable
private fun MostrarOpcion_RegistroCita(
    modifier: Modifier = Modifier,
    citas: List<Cita>,
    filtroActivo: FiltroCitas,
    onRegistrarClick: () -> Unit,
    onFiltroCambiado: (FiltroCitas) -> Unit,
    onCitaClick: (Cita) -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CalendarToday, contentDescription = "Calendario", modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Consultar citas", style = MaterialTheme.typography.headlineMedium)
        }
        Text("Gestiona tus citas medicas de manera sencilla", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRegistrarClick,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("+ Registrar cita", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(24.dp))

        Row {
            FiltroButton(
                text = "Mas Proximas",
                isSelected = filtroActivo == FiltroCitas.PROXIMAS,
                onClick = { onFiltroCambiado(FiltroCitas.PROXIMAS) }
            )
            Spacer(modifier = Modifier.width(16.dp))
            FiltroButton(
                text = "Mas Antiguas",
                isSelected = filtroActivo == FiltroCitas.ANTIGUAS,
                onClick = { onFiltroCambiado(FiltroCitas.ANTIGUAS) }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        // 8. MostrarListaCitas(citas: List<Cita>)
        MostrarListaCitas(citas = citas, filtroActivo = filtroActivo, onCitaClick = onCitaClick)
    }
}

/**
 * 3. Mando el formulario en pantalla para registrar una cita.
 */
@Composable
private fun MostrarFormulario(
    modifier: Modifier = Modifier,
    onCancelar: () -> Unit,
    onConfirmar: (fecha: String, hora: String, lugar: String, motivo: String) -> Unit
) {
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }
    var lugar by remember { mutableStateOf("") }
    var motivo by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Registrar cita", style = MaterialTheme.typography.headlineMedium)
        Text("Completa la informacion de la cita medica", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(value = fecha, onValueChange = { fecha = it }, label = { Text("Fecha") }, placeholder = { Text("DD/MM/AAAA") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next), keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = hora, onValueChange = { hora = it }, label = { Text("Hora") }, placeholder = { Text("HH:MM (formato 24h)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next), keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = lugar, onValueChange = { lugar = it }, label = { Text("Lugar") }, placeholder = { Text("Ej. Clinica San Jose, Hospital General") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next), keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = motivo, onValueChange = { motivo = it }, label = { Text("Motivo (Opcional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }))

        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = { onConfirmar(fecha, hora, lugar, motivo) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), modifier = Modifier.weight(1f)) { Text("Confirmar cita") }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = onCancelar, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)), modifier = Modifier.weight(1f)) { Text("Cancelar") }
        }
    }
}

/**
 *  Muestro visualmente la lista de citas en tarjetas.
 */
@Composable
private fun MostrarListaCitas(citas: List<Cita>, filtroActivo: FiltroCitas, onCitaClick: (Cita) -> Unit) {
    if (citas.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val texto = if(filtroActivo == FiltroCitas.PROXIMAS) "No tienes citas proximas" else "No tienes citas antiguas"
            Text(texto, color = Color.Gray)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(citas) { cita ->
                CitaCard(cita = cita, onClick = { onCitaClick(cita) })
            }
        }
    }
}

@Composable
private fun CitaCard(cita: Cita, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable (onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val formatoFecha = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale.getDefault())
            
            InfoRow(icon = Icons.Default.CalendarToday, text = formatoFecha.format(cita.fecha))
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow(icon = Icons.Default.AccessTime, text = cita.hora)
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow(icon = Icons.Default.LocationOn, text = cita.lugar)
            if (cita.motivo.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow(icon = Icons.Default.Info, text = cita.motivo)
            }
        }
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun DialogoExitoRegistro(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Exito", tint = Color(0xFF4CAF50), modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("¡Cita registrada con exito!", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            }
        },
        text = { Text("La cita ha sido guardada correctamente y ya aparece en tu lista de citas programadas.", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) {
                Text("Aceptar")
            }
        }
    )
}

@Composable
private fun FiltroButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val colors = if (isSelected) {
        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White)
    } else {
        ButtonDefaults.outlinedButtonColors()
    }
    Button(onClick = onClick, colors = colors, shape = RoundedCornerShape(50)) {
        Text(text)
    }
}

@Composable
private fun DialogoDetallesCita(
    cita: Cita,
    onDismiss: () -> Unit,
    onEliminar: () -> Unit,
    onPosponer: () -> Unit
) {
    val formatoFecha = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Info, contentDescription = "Detalles", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Detalles de la Cita", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column {
                InfoRow(icon = Icons.Default.CalendarToday, text = "Fecha: ${formatoFecha.format(cita.fecha)}")
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow(icon = Icons.Default.AccessTime, text = "Hora: ${cita.hora}")
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow(icon = Icons.Default.LocationOn, text = "Lugar: ${cita.lugar}")
                if (cita.motivo.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow(icon = Icons.Default.Info, text = "Motivo: ${cita.motivo}")
                }
            }
        },
        confirmButton = {
            // Botones de acción
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                // Botón "Eliminar" (Llama a la lógica de eliminación)
                Button(
                    onClick = onEliminar,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Eliminar")
                }
                Spacer(modifier = Modifier.width(16.dp))
                // Botón "Posponer" (Lógica pendiente)
                Button(
                    onClick = onPosponer,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Posponer")
                }
            }
        },
        dismissButton = {
            // Botón para cerrar el diálogo
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
private fun DialogoConfirmacionEliminar(
    cita: Cita,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Info, contentDescription = "Advertencia", tint = Color(0xFFF44336), modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Confirmar Eliminación", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "¿Estás seguro de que deseas eliminar la siguiente cita?",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Mostrar un detalle de la cita a eliminar
                val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                Text(
                    "Motivo: ${cita.motivo.ifBlank { "Sin motivo" }}\nFecha: ${formatoFecha.format(cita.fecha)} a las ${cita.hora}",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Esta acción no se puede deshacer.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Red
                )
            }
        },
        confirmButton = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                // Botón "Sí, Eliminar"
                Button(
                    onClick = onConfirmar,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Sí, Eliminar")
                }
                Spacer(modifier = Modifier.width(16.dp))
                // Botón "No, Cancelar"
                OutlinedButton(
                    onClick = onCancelar,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("No, Cancelar")
                }
            }
        }
    )
}