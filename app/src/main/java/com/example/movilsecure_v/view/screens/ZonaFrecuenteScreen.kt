package com.example.movilsecure_v.view.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.movilsecure_v.model.database.AppDatabase
import com.example.movilsecure_v.model.entities.PlaceDetails
import com.example.movilsecure_v.model.entities.UbicacionResult
import com.example.movilsecure_v.model.entities.ZonaFrecuente
import com.example.movilsecure_v.model.repository.ZonaFrecuenteRepository
import com.example.movilsecure_v.view.components.map.RouteDialog
import com.example.movilsecure_v.view.components.zonasfrecuentes.*
import com.example.movilsecure_v.viewmodel.ZonaFrecuenteViewModel
import com.example.movilsecure_v.viewmodel.ZonaFrecuenteViewModelFactory
import com.google.android.gms.maps.model.LatLng

@Composable
fun ZonasFrecuentesScreen(modifier: Modifier = Modifier, navController: NavHostController) {
    val context = LocalContext.current
    val factory = ZonaFrecuenteViewModelFactory(ZonaFrecuenteRepository(AppDatabase.getDatabase(context).zonaFrecuenteDao()))
    val viewModel: ZonaFrecuenteViewModel = viewModel(factory = factory)

    val zonas by viewModel.todasLasZonas.collectAsState()

    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var showEditDialog by rememberSaveable { mutableStateOf(false) }
    var modoEdicion by rememberSaveable { mutableStateOf(false) }
    var zonaAEditarId by rememberSaveable { mutableStateOf<String?>(null) }
    var ubicacionRecibida by remember { mutableStateOf<UbicacionResult?>(null) }
    var zonaParaRuta by remember { mutableStateOf<ZonaFrecuente?>(null) }

    val navBackStackEntry = navController.currentBackStackEntry
    val savedStateHandle = navBackStackEntry?.savedStateHandle

    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.get<UbicacionResult>("ubicacion_seleccionada")?.let {
            ubicacionRecibida = it
            if (modoEdicion) showEditDialog = true else showAddDialog = true
            savedStateHandle.remove<UbicacionResult>("ubicacion_seleccionada")
        }
    }

    if (showAddDialog) {
        CrearZonaFrecuenteDialog(
            ubicacionInicial = ubicacionRecibida,
            onDismissRequest = { showAddDialog = false; ubicacionRecibida = null },
            onSeleccionarUbicacionClick = { showAddDialog = false; navController.navigate("seleccionarUbicacion") },
            onGuardarZona = { nombre, direccion, lat, lon, nota ->
                viewModel.agregarZona(nombre, direccion, lat, lon, nota)
                showAddDialog = false
                ubicacionRecibida = null
                Toast.makeText(context, "$nombre guardado", Toast.LENGTH_SHORT).show()
            }
        )
    }

    val zonaAEditar = zonaAEditarId?.let { id -> zonas.find { it.id == id } }
    if (showEditDialog && zonaAEditar != null) {
        EditarZonaFrecuenteDialog(
            zona = zonaAEditar,
            ubicacionInicial = ubicacionRecibida,
            onDismissRequest = { showEditDialog = false; zonaAEditarId = null; ubicacionRecibida = null },
            onSeleccionarUbicacionClick = { showEditDialog = false; navController.navigate("seleccionarUbicacion") },
            onGuardarCambios = { zonaActualizada ->
                viewModel.actualizarZona(zonaActualizada)
                showEditDialog = false
                zonaAEditarId = null
                ubicacionRecibida = null
                Toast.makeText(context, "${zonaActualizada.nombreZona} actualizado", Toast.LENGTH_SHORT).show()
            }
        )
    }

    zonaParaRuta?.let {
        RouteDialog(
            place = it.toPlaceDetails(),
            onClose = { zonaParaRuta = null },
            onStartNavigation = {
                val gmmIntentUri = "google.navigation:q=${it.latitud},${it.longitud}".toUri()
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).setPackage("com.google.android.apps.maps")
                context.startActivity(mapIntent)
                zonaParaRuta = null
            }
        )
    }

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
        ZonasHeaderCard(onAddZoneClick = { modoEdicion = false; zonaAEditarId = null; ubicacionRecibida = null; showAddDialog = true })
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 12.dp)) {
            items(items = zonas, key = { it.id }) { zona ->
                ZonaFrecuenteCard(
                    zona = zona,
                    onVerRuta = { zonaParaRuta = it },
                    onModificar = { modoEdicion = true; zonaAEditarId = it.id; ubicacionRecibida = null; showEditDialog = true },
                    onEliminar = { viewModel.eliminarZona(it); Toast.makeText(context, "${it.nombreZona} eliminada", Toast.LENGTH_SHORT).show() }
                )
            }
        }
    }
}

private fun ZonaFrecuente.toPlaceDetails(): PlaceDetails {
    return PlaceDetails(
        id = this.id,
        name = this.nombreZona,
        address = this.direccionZona ?: "Dirección no disponible",
        location = LatLng(this.latitud, this.longitud),
        isOpen = "", // No aplica para zonas frecuentes
        rating = null, // No aplica
        phoneNumber = null // No aplica
    )
}
