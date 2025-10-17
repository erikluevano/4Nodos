package com.example.movilsecure_v.view.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.movilsecure_v.model.entities.UbicacionResult
import com.example.movilsecure_v.model.database.AppDatabase
import com.example.movilsecure_v.model.entities.ZonaFrecuente
import com.example.movilsecure_v.model.repository.ZonaFrecuenteRepository
import com.example.movilsecure_v.view.components.zonasfrecuentes.CrearZonaFrecuenteDialog
import com.example.movilsecure_v.view.components.zonasfrecuentes.EditarZonaFrecuenteDialog
import com.example.movilsecure_v.view.components.zonasfrecuentes.ZonaFrecuenteCard
import com.example.movilsecure_v.view.components.zonasfrecuentes.ZonasHeaderCard
import com.example.movilsecure_v.viewmodel.ZonaFrecuenteViewModel
import com.example.movilsecure_v.viewmodel.ZonaFrecuenteViewModelFactory

@Composable
fun ZonasFrecuentesScreen(modifier: Modifier = Modifier, navController: NavHostController) {
    val context = LocalContext.current
    val factory = ZonaFrecuenteViewModelFactory(
        ZonaFrecuenteRepository(
            AppDatabase.getDatabase(context).zonaFrecuenteDao()
        )
    )
    val viewModel: ZonaFrecuenteViewModel = viewModel(factory = factory)

    val zonas by viewModel.todasLasZonas.collectAsState()

    // --- ESTADO QUE DEBE SOBREVIVIR A LA NAVEGACIÓN ---
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var showEditDialog by rememberSaveable { mutableStateOf(false) }
    var modoEdicion by rememberSaveable { mutableStateOf(false) }
    var zonaAEditarId by rememberSaveable { mutableStateOf<String?>(null) }

    // --- ESTADO TEMPORAL ---
    var ubicacionRecibida by remember { mutableStateOf<UbicacionResult?>(null) }

    val navBackStackEntry = navController.currentBackStackEntry
    val savedStateHandle = navBackStackEntry?.savedStateHandle

    // Efecto para manejar el resultado de la pantalla de selección de ubicación
    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.get<UbicacionResult>("ubicacion_seleccionada")?.let { resultado ->
            ubicacionRecibida = resultado
            if (modoEdicion) {
                showEditDialog = true // Reabrir el diálogo de edición si estábamos en ese modo
            } else {
                showAddDialog = true
            }
            savedStateHandle.remove<UbicacionResult>("ubicacion_seleccionada")
        }
    }

    // Diálogo para AÑADIR
    if (showAddDialog) {
        CrearZonaFrecuenteDialog(
            ubicacionInicial = ubicacionRecibida,
            onDismissRequest = {
                showAddDialog = false
                ubicacionRecibida = null
            },
            onSeleccionarUbicacionClick = {
                showAddDialog = false
                navController.navigate("seleccionarUbicacion")
            },
            onGuardarZona = { nombre, direccion, lat, lon, nota ->
                viewModel.agregarZona(nombre, direccion, lat, lon, nota)
                showAddDialog = false
                ubicacionRecibida = null
                Toast.makeText(context, "$nombre se ha guardado exitosamente", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Buscamos la zona a editar usando el ID guardado
    val zonaAEditar = zonaAEditarId?.let { id -> zonas.find { it.id == id } }

    // Diálogo para EDITAR
    if (showEditDialog && zonaAEditar != null) {
        EditarZonaFrecuenteDialog(
            zona = zonaAEditar,
            ubicacionInicial = ubicacionRecibida,
            onDismissRequest = {
                showEditDialog = false
                zonaAEditarId = null
                ubicacionRecibida = null
            },
            onSeleccionarUbicacionClick = {
                showEditDialog = false // Cerramos el diálogo antes de navegar
                navController.navigate("seleccionarUbicacion")
            },
            onGuardarCambios = { zonaActualizada ->
                viewModel.actualizarZona(zonaActualizada)
                showEditDialog = false
                zonaAEditarId = null
                ubicacionRecibida = null
                Toast.makeText(context, "${zonaActualizada.nombreZona} se ha actualizado correctamente", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // --- UI de la pantalla principal ---
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        ZonasHeaderCard(
            onAddZoneClick = {
                modoEdicion = false
                zonaAEditarId = null
                ubicacionRecibida = null
                showAddDialog = true
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            items(
                items = zonas,
                key = { zona -> zona.id }
            ) { zona ->
                ZonaFrecuenteCard(
                    zona = zona,
                    onVerRuta = {
                        Toast.makeText(context, "Viendo ruta para ${it.nombreZona}", Toast.LENGTH_SHORT).show()
                    },
                    onModificar = { zonaSeleccionada ->
                        modoEdicion = true
                        zonaAEditarId = zonaSeleccionada.id
                        ubicacionRecibida = null
                        showEditDialog = true
                    },
                    onEliminar = {
                        viewModel.eliminarZona(it)
                        Toast.makeText(context, "${it.nombreZona} ha sido eliminada exitosamente", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}