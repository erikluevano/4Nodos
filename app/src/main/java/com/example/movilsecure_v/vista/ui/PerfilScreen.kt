
package com.example.movilsecure_v.vista.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.movilsecure_v.modelo.PerfilAdultoMayor
import com.example.movilsecure_v.modelo.basedatos.AppDatabase
import com.example.movilsecure_v.modelo.repositorio.RepositorioPerfil
import com.example.movilsecure_v.vista.FormPerfilUI
import com.example.movilsecure_v.viewmodel.PerfilVM
import com.example.movilsecure_v.viewmodel.PerfilUiState
import com.example.movilsecure_v.viewmodel.PerfilViewModelFactory

@Composable
fun PerfilScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    // --- INYECCIÓN DE DEPENDENCIAS ---
    // Creamos la factory con el repositorio y el dao, igual que en ZonasFrecuentes
    val factory = PerfilViewModelFactory(
        RepositorioPerfil(AppDatabase.getDatabase(context).perfilDao())
    )
    // Pasamos la factory al viewModel composable
    val viewModel: PerfilVM = viewModel(factory = factory)

    // El controlador ahora recibe el viewModel ya creado
    val formPerfilUI = remember { FormPerfilUI(viewModel) }
    val uiState by viewModel.uiState.collectAsState()
    
    // La lista de perfiles ahora viene de un Flow separado en el ViewModel
    val perfiles by viewModel.perfilesRegistrados.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.mensajeError) {
        uiState.mensajeError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMensajeErrorMostrado()
        }
    }

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
                perfiles = perfiles, // Pasamos la lista de perfiles
                onEliminarPerfil = { viewModel.eliminarPerfil(it.id) }
            )
        }
    }
}

@Composable
fun VistaPrincipalPerfil(
    modifier: Modifier = Modifier,
    formUI: FormPerfilUI,
    perfiles: List<PerfilAdultoMayor>,
    onEliminarPerfil: (PerfilAdultoMayor) -> Unit
) {
    Column(modifier = modifier.padding(16.dp)) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Perfiles de Adultos Mayores", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Gestiona la información de los adultos mayores bajo tu cuidado")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = formUI::mostrarOpcionRegistrar, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Registrar")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Registrar Datos")
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        if (perfiles.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aún no hay perfiles registrados.")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(perfiles, key = { it.id }) { perfil ->
                    TarjetaPerfil(
                        perfil = perfil,
                        onEliminar = { onEliminarPerfil(perfil) }
                    )
                }
            }
        }
    }
}

@Composable
fun TarjetaPerfil(
    perfil: PerfilAdultoMayor,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(perfil.nombre, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                IconButton(onClick = { /* TODO: Lógica de editar */ }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = onEliminar) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Nac: ${perfil.fechaNacimiento}")
                Text(perfil.sexo)
                Text("Sangre: ${perfil.tipoDeSangre}")
            }
            Divider(modifier = Modifier.padding(vertical = 12.dp))
            InfoItem(icon = Icons.Default.MedicalServices, title = "Historial médico", detail = perfil.historialMedico)
            InfoItem(icon = Icons.Default.Medication, title = "Medicamentos actuales", detail = perfil.medicamentosActuales)
            InfoItem(icon = Icons.Default.Warning, title = "Alergias", detail = perfil.alergias)
        }
    }
}

@Composable
fun InfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, detail: String) {
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

@Composable
fun DialogoExito(mensaje: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
        title = { Text("Perfil registrado exitosamente") },
        text = { Text("Los datos del adulto mayor han sido guardados correctamente.") },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Continuar")
            }
        }
    )
}

@Composable
private fun FormularioPerfil(
    modifier: Modifier = Modifier,
    formUI: FormPerfilUI,
    uiState: PerfilUiState
) {
     Column(
        modifier = modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Registrar Perfil", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(value = uiState.nombre, onValueChange = formUI.viewModel::onNombreChange, label = { Text("Nombre Completo *") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = uiState.fechaNacimiento, onValueChange = formUI.viewModel::onFechaNacimientoChange, label = { Text("Fecha de Nacimiento *") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = uiState.sexo, onValueChange = formUI.viewModel::onSexoChange, label = { Text("Sexo *") }, modifier = Modifier.fillMaxWidth())
        Text("Información Médica", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(value = uiState.tipoDeSangre, onValueChange = formUI.viewModel::onTipoDeSangreChange, label = { Text("Tipo de sangre *") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = uiState.historialMedico, onValueChange = formUI.viewModel::onHistorialMedicoChange, label = { Text("Historial médico") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = uiState.medicamentosActuales, onValueChange = formUI.viewModel::onMedicamentosChange, label = { Text("Medicamentos actuales") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = uiState.alergias, onValueChange = formUI.viewModel::onAlergiasChange, label = { Text("Alergias") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = formUI::cancelarRegistro, modifier = Modifier.weight(1f)) {
                Text("Cancelar")
            }
            Button(onClick = formUI::validarYGuardar, modifier = Modifier.weight(1f)) {
                Text("Guardar")
            }
        }
    }
}
