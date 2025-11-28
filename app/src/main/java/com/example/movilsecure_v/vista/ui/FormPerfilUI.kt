// Archivo: app/src/main/java/com/example/movilsecure_v/vista/ui/FormPerfilUI.kt
package com.example.movilsecure_v.vista.ui

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.movilsecure_v.modelo.basedatos.AppDatabase
import com.example.movilsecure_v.modelo.entidades.Medicamento
import com.example.movilsecure_v.modelo.entidades.PerfilAdultoMayor
import com.example.movilsecure_v.modelo.repositorio.RepositorioMedicamentos
import com.example.movilsecure_v.modelo.repositorio.RepositorioPerfil
import com.example.movilsecure_v.modelo.servicios.ServicioPerfil
import com.example.movilsecure_v.viewmodel.MedicamentosViewModel
import com.example.movilsecure_v.viewmodel.PerfilUiState
import com.example.movilsecure_v.viewmodel.PerfilVM
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

    // --- Dependencias ---
    val db = AppDatabase.getDatabase(context)
    val repositorioPerfil = remember { RepositorioPerfil(db.perfilDao()) }
    // CAMBIO: Crear instancia del repositorio de medicamentos
    val repositorioMedicamentos = remember { RepositorioMedicamentos(db.medicamentosDAO()) }
    val servicioPerfil = remember { ServicioPerfil(repositorioPerfil) }

    // CAMBIO: Inyectar el repositorio de medicamentos al PerfilVM
    val viewModel: PerfilVM = viewModel { PerfilVM(repositorioPerfil, servicioPerfil, repositorioMedicamentos) }

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

    // CAMBIO CLAVE: Lógica para mostrar el formulario de medicamentos
    if (uiState.mostrandoFormularioMedicamento) {
        // Instancia el ViewModel de medicamentos aquí para el diálogo
        val medViewModel: MedicamentosViewModel = viewModel(factory = MedicamentosViewModel.Factory)
        val medUiState by medViewModel.uiState.collectAsState()

        MostrarFormulario( // <- Este es el Composable de MedicamentosUI.kt
            uiState = medUiState,
            viewModel = medViewModel,
            onDismiss = { viewModel.onOcultarFormularioMedicamento() },
            onMedicamentoGuardado = { nuevoMedicamento ->
                // Acción al guardar:
                // 1. Llama al PerfilVM para que guarde el medicamento y actualice la lista.
                viewModel.guardarMedicamentoYActualizarPerfil(nuevoMedicamento)
                // 2. Limpia el estado del formulario de medicamentos.
                medViewModel.ocultarFormulario()
                // 3. (Opcional, ya que guardarMedicamentoYActualizarPerfil lo hace) Cierra el diálogo.
                // viewModel.onOcultarFormularioMedicamento()
            }
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
                uiState = uiState,
                // Pasar las funciones del viewModel directamente
                onEliminarMedicamento = viewModel::onEliminarMedicamento,
                onAgregarMedicamentoClick = viewModel::onMostrarFormularioMedicamento
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

// El resto de los composables no necesita cambios significativos, pero asegúrate de que
// FormularioPerfil tiene los parámetros 'onEliminarMedicamento' y 'onAgregarMedicamentoClick'

// ... (pegar el resto de tu código de FormPerfilUI.kt aquí, asegúrate de que concuerde)

@Composable
fun DialogoExito(mensaje: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Éxito") },
        text = { Text(mensaje) },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Aceptar") } }
    )
}

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
            if (perfiles.isEmpty()) {
                TarjetaRegistro(onRegistrarClick = formUI::mostrarOpcionRegistrar)
            }
        }
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
                TextButton(onClick = {
                    onEliminar()
                    mostrarDialogoEliminar = false
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEliminar = false }) { Text("Cancelar") }
            }
        )
    }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = perfil.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Editar Perfil") }
                IconButton(onClick = { mostrarDialogoEliminar = true }) {
                    Icon(Icons.Default.Delete, "Eliminar Perfil", tint = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val edad = calcularEdad(perfil.fechaNacimiento)
                InfoRapida(icon = Icons.Default.CalendarToday, text = if (edad != null) "$edad años" else "N/A")
                InfoRapida(icon = Icons.Default.Person, text = perfil.sexo)
                InfoRapida(icon = Icons.Default.Bloodtype, text = perfil.tipoDeSangre)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoItem(
                    icon = Icons.Default.FavoriteBorder,
                    title = "Historial médico",
                    detail = perfil.historialMedico.ifEmpty { "No especificado" }
                )
                InfoItem(
                    icon = Icons.Default.Medication,
                    title = "Medicamentos actuales",
                    detail = if (perfil.medicamentosActuales.isBlank()) "Ninguno" else perfil.medicamentosActuales
                )
                InfoItem(
                    icon = Icons.Default.ReportProblem,
                    title = "Alergias",
                    detail = perfil.alergias,
                    detailColor = MaterialTheme.colorScheme.error
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
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun InfoItem(icon: ImageVector, title: String, detail: String, detailColor: Color = Color.Unspecified) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
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


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun FormularioPerfil(
    modifier: Modifier = Modifier,
    formUI: FormPerfilUI,
    uiState: PerfilUiState,
    onEliminarMedicamento: (String) -> Unit,
    onAgregarMedicamentoClick: () -> Unit
) {
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

        val sexoSeleccionado = when {
            uiState.sexo in uiState.listaSexos.dropLast(1) -> uiState.sexo
            else -> "Otro"
        }

        ExposedDropdownMenuBox(
            expanded = uiState.menuSexoAbierto,
            onExpandedChange = { formUI.viewModel.onMenuSexoDismissRequest(!uiState.menuSexoAbierto) }
        ) {
            OutlinedTextField(
                value = uiState.sexo,
                onValueChange = formUI.viewModel::onSexoChange,
                label = { Text("Sexo *") },
                readOnly = sexoSeleccionado != "Otro",
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = uiState.menuSexoAbierto) }
            )
            ExposedDropdownMenu(
                expanded = uiState.menuSexoAbierto,
                onDismissRequest = { formUI.viewModel.onMenuSexoDismissRequest(false) }
            ) {
                uiState.listaSexos.forEach { sexo ->
                    DropdownMenuItem(
                        text = { Text(sexo) },
                        onClick = { formUI.viewModel.onSexoSeleccionado(sexo) }
                    )
                }
            }
        }

        OutlinedTextField(value = uiState.historialMedico, onValueChange = formUI.viewModel::onHistorialMedicoChange, label = { Text("Historial Médico") }, modifier = Modifier.fillMaxWidth())

        ExposedDropdownMenuBox(
            expanded = uiState.menuSangreAbierto,
            onExpandedChange = { formUI.viewModel.onMenuSangreDismissRequest(!uiState.menuSangreAbierto) }
        ) {
            OutlinedTextField(
                value = uiState.tipoDeSangre,
                onValueChange = {},
                readOnly = true,
                label = { Text("Tipo de Sangre *") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = uiState.menuSangreAbierto) }
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

        OutlinedTextField(value = uiState.alergias, onValueChange = formUI.viewModel::onAlergiasChange, label = { Text("Alergias") }, modifier = Modifier.fillMaxWidth())

        // --- SECCIÓN DE MEDICAMENTOS ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                .padding(16.dp)
        ) {
            Text("Medicamentos Actuales", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            if (uiState.medicamentosActuales.isEmpty()) {
                Text("Ninguno", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                uiState.medicamentosActuales.forEach { med ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(med, modifier = Modifier.weight(1f))
                        IconButton(onClick = { onEliminarMedicamento(med) }) {
                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Eliminar medicamento", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onAgregarMedicamentoClick, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Añadir Medicamento")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = formUI::cancelarRegistro, modifier = Modifier.weight(1f)) { Text("Cancelar") }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = formUI::validarYGuardar, modifier = Modifier.weight(1f)) { Text("Guardar") }
        }
    }
}
