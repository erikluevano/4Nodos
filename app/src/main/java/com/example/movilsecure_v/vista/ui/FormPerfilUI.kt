package com.example.movilsecure_v.vista.ui

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.movilsecure_v.modelo.basedatos.AppDatabase
import com.example.movilsecure_v.modelo.entidades.PerfilAdultoMayor
import com.example.movilsecure_v.modelo.repositorio.RepositorioPerfil
import com.example.movilsecure_v.modelo.servicios.ServicioPerfil
import com.example.movilsecure_v.viewmodel.PerfilUiState
import com.example.movilsecure_v.viewmodel.PerfilVM
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Check
import java.util.Calendar

/**
 * Clase que encapsula las acciones de la UI, comunicándose con el ViewModel.
 * Sigue el diseño del diagrama de clases.
 */
class FormPerfilUI(val viewModel: PerfilVM) {
    fun mostrarOpcionRegistrar() {
        viewModel.onMostrarFormulario()
    }

    fun validarYGuardar() {
        viewModel.registrarOActualizarPerfil()
    }

    fun cancelarRegistro() {
        viewModel.onOcultarFormulario()
    }
}

@Composable
fun PerfilScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // Instanciación manual de dependencias. Se elimina el ViewModelFactory.
    // 'remember' asegura que no se re-creen en cada recomposición.
    val repositorio = remember { RepositorioPerfil(AppDatabase.getDatabase(context).perfilDao()) }
    val servicio = remember { ServicioPerfil(repositorio) }

    // El composable `viewModel()` se encarga de proveer la instancia de PerfilVM
    // atada al ciclo de vida correcto y la crea usando nuestra lógica.
    val viewModel: PerfilVM = viewModel { PerfilVM(repositorio, servicio) }

    val formPerfilUI = remember { FormPerfilUI(viewModel) }
    val uiState by viewModel.uiState.collectAsState()
    val perfiles by viewModel.perfilesRegistrados.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Efecto para mostrar mensajes de error como Snackbars
    LaunchedEffect(uiState.mensajeError) {
        uiState.mensajeError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMensajeErrorMostrado()
        }
    }

    // Muestra un diálogo de éxito cuando hay un mensaje de confirmación
    if (uiState.mensajeConfirmacion != null) {
        DialogoExito(
            mensaje = uiState.mensajeConfirmacion!!,
            onDismiss = viewModel::onMensajeConfirmacionMostrado
        )
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.mostrandoFormulario) {
            FormularioPerfil(
                modifier = Modifier.padding(paddingValues),
                formUI = formPerfilUI,
                uiState = uiState
            )
        } else {
            VistaPrincipalPerfil(
                modifier = Modifier.padding(paddingValues),
                formUI = formPerfilUI,
                perfiles = perfiles,
                onEditarPerfil = { viewModel.iniciarEdicion(it) },
                onEliminarPerfil = { viewModel.eliminarPerfil(it) },
                calcularEdad = viewModel::calcularEdad

            )
        }
    }
}

// --- El resto de los Composables (VistaPrincipal, Formulario, Tarjeta, etc.) ---
// Se modifican para usar las nuevas llamadas y dependencias.

@Composable
fun VistaPrincipalPerfil(
    modifier: Modifier = Modifier,
    formUI: FormPerfilUI,
    perfiles: List<PerfilAdultoMayor>,
    onEditarPerfil: (PerfilAdultoMayor) -> Unit,
    onEliminarPerfil: (String) -> Unit,
    calcularEdad: (String) -> Int?
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Si no hay perfiles, muestra la tarjeta de invitación para registrar.
            if (perfiles.isEmpty()) {
                TarjetaRegistro(onRegistrarClick = formUI::mostrarOpcionRegistrar)
            }
        }

        // Muestra la lista de perfiles registrados.
        items(perfiles, key = { it.id }) { perfil ->
            TarjetaPerfil(
                perfil = perfil,
                onEdit = { onEditarPerfil(perfil) },
                onEliminar = { onEliminarPerfil(perfil.id) },
                calcularEdad = calcularEdad
            )
        }
    }
}

@Composable
fun TarjetaRegistro(onRegistrarClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PersonOutline,
                    contentDescription = "Icono de perfiles",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Perfil del Adulto Mayor", style = MaterialTheme.typography.titleLarge)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Gestiona la información del adulto mayor bajo tu cuidado",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRegistrarClick, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Registrar Datos")
            }
        }
    }
}


@Composable
fun TarjetaPerfil(
    perfil: PerfilAdultoMayor,
    onEdit: () -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier,
    calcularEdad: (String) -> Int?

) {
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }

    if (mostrarDialogoEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = false },
            title = { Text("Confirmar Eliminación") },
            text = { Text("¿Estás seguro de que deseas eliminar el perfil de ${perfil.nombre}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEliminar()
                        mostrarDialogoEliminar = false
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEliminar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // --- Cabecera con Nombre y Botones ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = perfil.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Editar Perfil") }
                IconButton(onClick = { mostrarDialogoEliminar = true }) {
                    Icon(
                        Icons.Default.Delete,
                        "Eliminar Perfil",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // --- Fila de Información Rápida (Edad, Sexo, Sangre) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val edad = calcularEdad(perfil.fechaNacimiento)
                InfoRapida(
                    icon = Icons.Default.CalendarToday,
                    text = if (edad != null) "$edad años" else "N/A"
                )
                InfoRapida(icon = Icons.Default.Person, text = perfil.sexo)
                InfoRapida(icon = Icons.Default.Bloodtype, text = perfil.tipoDeSangre)
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            // --- Sección de Detalles Médicos ---
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoItem(
                    icon = Icons.Default.FavoriteBorder,
                    title = "Historial médico",
                    detail = perfil.historialMedico.ifEmpty { "No especificado" }
                )
                InfoItem(
                    icon = Icons.Default.Medication,
                    title = "Medicamentos actuales",
                    detail = perfil.medicamentosActuales
                )
                InfoItem(
                    icon = Icons.Default.ReportProblem,
                    title = "Alergias",
                    detail = perfil.alergias,
                    detailColor = MaterialTheme.colorScheme.error // Color rojo para alergias
                )
            }
        }
    }
}

@Composable
private fun InfoRapida(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}



@Composable
fun InfoItem(
    icon: ImageVector,
    title: String,
    detail: String,
    detailColor: Color = Color.Unspecified
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = detail,
            style = MaterialTheme.typography.bodyMedium,
            color = if (detailColor != Color.Unspecified) detailColor else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 28.dp)
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormularioPerfil(modifier: Modifier = Modifier, formUI: FormPerfilUI, uiState: PerfilUiState) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            formUI.viewModel.onFechaNacimientoChange("$dayOfMonth/${month + 1}/$year")
        },
        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (uiState.idPerfilEditando != null) "Editando Perfil" else "Registrar Perfil",
            style = MaterialTheme.typography.headlineMedium
        )
        OutlinedTextField(value = uiState.nombre, onValueChange = formUI.viewModel::onNombreChange, label = { Text("Nombre Completo *") }, modifier = Modifier.fillMaxWidth())

        OutlinedTextField(
            value = uiState.fechaNacimiento, onValueChange = {},
            label = { Text("Fecha de Nacimiento *") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { datePickerDialog.show() },
            readOnly = true, enabled = false,
            colors = TextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledIndicatorColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        OutlinedTextField(value = uiState.sexo, onValueChange = formUI.viewModel::onSexoChange, label = { Text("Sexo *") }, modifier = Modifier.fillMaxWidth())
        Text("Información Médica", style = MaterialTheme.typography.titleMedium)

        ExposedDropdownMenuBox(
            expanded = uiState.menuSangreAbierto,
            onExpandedChange = { formUI.viewModel.onMenuSangreDismissRequest(!uiState.menuSangreAbierto) }
        ) {
            OutlinedTextField(
                value = uiState.tipoDeSangre, onValueChange = {}, readOnly = true,
                label = { Text("Tipo de Sangre *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = uiState.menuSangreAbierto) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = uiState.menuSangreAbierto,
                onDismissRequest = { formUI.viewModel.onMenuSangreDismissRequest(false) }
            ) {
                uiState.listaTiposDeSangre.forEach { tipo ->
                    DropdownMenuItem(text = { Text(tipo) }, onClick = { formUI.viewModel.onTipoDeSangreChange(tipo) })
                }
            }
        }

        OutlinedTextField(value = uiState.historialMedico, onValueChange = formUI.viewModel::onHistorialMedicoChange, label = { Text("Historial médico (Opcional)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = uiState.medicamentosActuales, onValueChange = formUI.viewModel::onMedicamentosChange, label = { Text("Medicamentos actuales *") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = uiState.alergias, onValueChange = formUI.viewModel::onAlergiasChange, label = { Text("Alergias *") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = formUI::cancelarRegistro, modifier = Modifier.weight(1f)) { Text("Cancelar") }
            Button(onClick = formUI::validarYGuardar, modifier = Modifier.weight(1f)) { Text("Guardar") }
        }
    }
}

@Composable
fun DialogoExito(mensaje: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp), //
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Icono con fondo circular
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Éxito",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Text(
                    text = mensaje,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Los datos del adulto mayor han sido modificados con exito",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continuar")
                }
            }
        }
    }
}

@Composable
fun InfoItem(icon: ImageVector, title: String, detail: String) {
    if (detail.isNotBlank()) {
        Row(modifier = Modifier.padding(top = 8.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(detail, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}