package com.example.movilsecure_v.vista.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.movilsecure_v.modelo.entidades.Cita
import com.example.movilsecure_v.viewmodel.CitasViewModel
import com.example.movilsecure_v.viewmodel.FiltroCitas
import com.example.movilsecure_v.viewmodel.UiState
import java.text.SimpleDateFormat
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
    var citaParaPosponer by remember { mutableStateOf<Cita?>(null) }

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


    fun mostrarDialogoPosponer(cita: Cita) {
        cerrarDialogo() // Cerrar el diálogo de detalles
        citaParaPosponer = cita // Abrir el diálogo para posponer
    }

    fun posponerCita(cita: Cita, nuevaFecha: String, nuevaHora: String) {
        viewModel.posponerCita(cita, nuevaFecha, nuevaHora)
        citaParaPosponer = null // Cerrar el diálogo al confirmar
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is UiState.Exito -> {
                // Modificado para mostrar mensaje de éxito también al posponer
                if (state.mensaje.contains("pospuesta", ignoreCase = true) || state.mensaje.contains("eliminada", ignoreCase = true)) {
                    MostrarMensaje(state.mensaje)
                    viewModel.consumirUiState()
                }
            }
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

    if (uiState is UiState.Exito && mostrandoFormulario) {
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
            onPosponer = { mostrarDialogoPosponer(citaSeleccionada!!) }
        )
    }

    if (citaParaPosponer != null) {
        DialogoPosponerCita(
            cita = citaParaPosponer!!,
            onConfirmar = { nuevaFecha, nuevaHora ->
                posponerCita(citaParaPosponer!!, nuevaFecha, nuevaHora)
            },
            onCancelar = { citaParaPosponer = null }
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
        // Eliminamos el título y botones por defecto para crear un layout personalizado
        title = { },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // 1. Encabezado
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = "Detalles",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Detalles de la Cita",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    "Información completa de la cita médica",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))

                // 2. Tarjeta con la información
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        DetalleInfoRow(icon = Icons.Default.AccessTime, label = "Fecha y Hora") {
                            Text(formatoFecha.format(cita.fecha), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                            Text(cita.hora, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }
                        DetalleInfoRow(icon = Icons.Default.LocationOn, label = "Lugar") {
                            Text(cita.lugar, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                        }
                        if (cita.motivo.isNotBlank()) {
                            DetalleInfoRow(icon = Icons.Default.Info, label = "Motivo") {
                                Text(cita.motivo, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))


                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onPosponer,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Outlined.EditCalendar, contentDescription = "Posponer")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Posponer cita")
                    }
                    OutlinedButton(
                        onClick = onEliminar,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Eliminar")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Eliminar cita")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            // Botón de cerrar como dismiss
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.Close, contentDescription = "Cerrar")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar")
            }
        }
    )
}

@Composable
private fun DetalleInfoRow(
    icon: ImageVector,
    label: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 4.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(content = content)
    }
}

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
        modifier = modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CalendarToday, contentDescription = "Calendario", modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Consultar Citas", style = MaterialTheme.typography.headlineMedium)
        }
        Text("Gestiona tus citas médicas de manera sencilla", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRegistrarClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("+ Registrar Cita", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            FiltroButton(
                text = "Más antiguas",
                icon = Icons.Default.CalendarToday,
                isSelected = filtroActivo == FiltroCitas.ANTIGUAS,
                onClick = { onFiltroCambiado(FiltroCitas.ANTIGUAS) },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            FiltroButton(
                text = "Más próximas",
                icon = Icons.Default.CalendarToday,
                isSelected = filtroActivo == FiltroCitas.PROXIMAS,
                onClick = { onFiltroCambiado(FiltroCitas.PROXIMAS) },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        // --- INICIO DE LA MODIFICACIÓN ---
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            val textoCitas = if (citas.size == 1) "1 cita" else "${citas.size} citas"
            Text(textoCitas, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

            // Texto dinámico según el filtro activo
            val textoFiltroActivo = when (filtroActivo) {
                FiltroCitas.PROXIMAS -> "Próximas citas"
                FiltroCitas.ANTIGUAS -> "Citas antiguas"
            }
            Text(textoFiltroActivo, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
        }
        // --- FIN DE LA MODIFICACIÓN ---
        Spacer(modifier = Modifier.height(16.dp))

        MostrarListaCitas(citas = citas, onCitaClick = onCitaClick)
    }
}

@Composable
private fun MostrarListaCitas(citas: List<Cita>, onCitaClick: (Cita) -> Unit) {
    if (citas.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No tienes citas programadas.", color = Color.Gray, fontSize = 16.sp)
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
            .clickable(onClick = onClick),
        // --- INICIO DE LA MODIFICACIÓN ---
        // Se elimina la elevación para un look más plano y se añade un borde.
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        // --- FIN DE LA MODIFICACIÓN ---
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Icono de calendario",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                val formatoFecha = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale.getDefault())
                val textoFechaHora = "${formatoFecha.format(cita.fecha)} - ${cita.hora}"

                FilaInformacion(icon = Icons.Default.AccessTime, text = textoFechaHora)
                Spacer(modifier = Modifier.height(8.dp))
                FilaInformacion(icon = Icons.Default.LocationOn, text = cita.lugar)
                if (cita.motivo.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FilaInformacion(icon = Icons.Default.Info, text = cita.motivo)
                }
            }
        }
    }
}


@Composable
private fun FiltroButton(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // --- INICIO DE LA MODIFICACIÓN ---
    // Usaremos OutlinedButton como base para ambos estados
    if (isSelected) {
        // Estado seleccionado: Botón sólido
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = modifier
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text, fontWeight = FontWeight.SemiBold)
            }
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            ),
            modifier = modifier
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start // Alineación a la izquierda
    ) {
        // 1. Encabezado del formulario
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Registrar",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Registrar Cita", style = MaterialTheme.typography.headlineMedium)
        }
        Text(
            "Completa la información de la cita médica",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(32.dp))

        // 2. Campos de texto con iconos y texto de ayuda
        OutlinedTextField(
            value = fecha,
            onValueChange = { fecha = it },
            label = { Text("Fecha *") },
            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
            supportingText = { Text("Formato: DD/MM/AAAA") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = hora,
            onValueChange = { hora = it },
            label = { Text("Hora *") },
            leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
            supportingText = { Text("Formato: HH:MM (24h)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = lugar,
            onValueChange = { lugar = it },
            label = { Text("Lugar *") },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
            placeholder = { Text("Ej. Hospital General") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = motivo,
            onValueChange = { motivo = it },
            label = { Text("Motivo (Opcional)") },
            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
            placeholder = { Text("Ej. Examen de sangre") },
            modifier = Modifier.fillMaxWidth(),
            // Permitimos múltiples líneas para el motivo
            singleLine = false,
            minLines = 3,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )

        // Spacer para empujar los botones al final de la pantalla
        Spacer(modifier = Modifier.weight(1f))

        // 3. Botones de acción rediseñados y apilados
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = { onConfirmar(fecha, hora, lugar, motivo) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Confirmar")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Confirmar Cita", fontWeight = FontWeight.Bold)
            }
            TextButton(
                onClick = onCancelar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.Close, contentDescription = "Cancelar")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cancelar")
            }
        }
    }
}

@Composable
private fun FilaInformacion(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
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
private fun DialogoPosponerCita(
    cita: Cita,
    onConfirmar: (nuevaFecha: String, nuevaHora: String) -> Unit,
    onCancelar: () -> Unit
) {
    // Formatos para mostrar y para los campos de texto
    val formatoFechaOriginal = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale.getDefault())
    val formatoFechaCampo = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val fechaOriginalCampoStr = formatoFechaCampo.format(cita.fecha)

    // Estados para los campos de texto, inicializados con los valores actuales
    var nuevaFecha by remember { mutableStateOf(fechaOriginalCampoStr) }
    var nuevaHora by remember { mutableStateOf(cita.hora) }

    val focusManager = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = onCancelar,
        title = {},
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // 1. Encabezado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.EditCalendar,
                        contentDescription = "Posponer",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Posponer Cita", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Text(
                    "Selecciona la nueva fecha y hora para la cita",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 2. Tarjeta de información actual
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            "Fecha actual: ${formatoFechaOriginal.format(cita.fecha)} - ${cita.hora}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Lugar: ${cita.lugar}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // 3. Campos de texto para nueva fecha y hora
                OutlinedTextField(
                    value = nuevaFecha,
                    onValueChange = { nuevaFecha = it },
                    label = { Text("Nueva Fecha *") },
                    supportingText = { Text("Formato: DD/MM/AAAA") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nuevaHora,
                    onValueChange = { nuevaHora = it },
                    label = { Text("Nueva Hora *") },
                    supportingText = { Text("Formato: HH:MM (24h)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )
                Spacer(modifier = Modifier.height(24.dp))

                // 4. Botones de acción
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onConfirmar(nuevaFecha, nuevaHora) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Confirmar")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Confirmar cambios")
                    }
                    TextButton(onClick = onCancelar, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Outlined.Close, contentDescription = "Cancelar")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cancelar")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
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
                Button(
                    onClick = onConfirmar,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Sí, Eliminar")
                }
                Spacer(modifier = Modifier.width(16.dp))
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