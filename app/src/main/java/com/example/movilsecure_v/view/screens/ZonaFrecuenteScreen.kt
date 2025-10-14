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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.movilsecure_v.model.database.AppDatabase
import com.example.movilsecure_v.model.entities.ZonaFrecuente // NUEVO: Importar ZonaFrecuente
import com.example.movilsecure_v.model.repository.ZonaFrecuenteRepository
import com.example.movilsecure_v.view.components.zonasfrecuentes.CrearZonaFrecuenteDialog
import com.example.movilsecure_v.view.components.zonasfrecuentes.EditarZonaFrecuenteDialog
import com.example.movilsecure_v.view.components.zonasfrecuentes.ZonaFrecuenteCard
import com.example.movilsecure_v.view.components.zonasfrecuentes.ZonasHeaderCard
import com.example.movilsecure_v.viewmodel.ZonaFrecuenteViewModel
import com.example.movilsecure_v.viewmodel.ZonaFrecuenteViewModelFactory

@Composable
fun ZonasFrecuentesScreen(modifier: Modifier = Modifier) {
    // --- 1. CONFIGURACIÓN DEL VIEWMODEL ---
    val context = LocalContext.current
    val factory = ZonaFrecuenteViewModelFactory(
        ZonaFrecuenteRepository(
            AppDatabase.getDatabase(context).zonaFrecuenteDao()
        )
    )
    val viewModel: ZonaFrecuenteViewModel = viewModel(factory = factory)

    // --- 2. OBSERVACIÓN DEL ESTADO ---
    val zonas by viewModel.todasLasZonas.collectAsState()

    // --- 3. ESTADO PARA CONTROLAR LA VISIBILIDAD DE LOS DIÁLOGOS ---
    var showAddDialog by remember { mutableStateOf(false) }
    // NUEVO: Estados para controlar el diálogo de edición
    var showEditDialog by remember { mutableStateOf(false) }
    var zonaAEditar by remember { mutableStateOf<ZonaFrecuente?>(null) }


    // --- 4. DISEÑO DE LA PANTALLA ---
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Componente de Cabecera
        ZonasHeaderCard(
            onAddZoneClick = {
                showAddDialog = true
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de Zonas Frecuentes
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
                    // NUEVO: Lógica para el botón de modificar
                    onModificar = { zonaSeleccionada ->
                        // Guardamos la zona que se va a editar y mostramos el diálogo
                        zonaAEditar = zonaSeleccionada
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

    // --- 5. LÓGICA PARA MOSTRAR LOS DIÁLOGOS ---

    // Diálogo para AÑADIR una nueva zona
    if (showAddDialog) {
        CrearZonaFrecuenteDialog(
            onDismissRequest = { showAddDialog = false },
            onGuardarZona = { nombre, direccion, lat, lon, nota ->
                viewModel.agregarZona(nombre, direccion, lat, lon, nota)
                showAddDialog = false
                Toast.makeText(context, "$nombre se ha guardado exitosamente", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // NUEVO: Diálogo para EDITAR una zona existente
    // Se mostrará solo si showEditDialog es true y tenemos una zona seleccionada
    if (showEditDialog && zonaAEditar != null) {
        EditarZonaFrecuenteDialog(
            zona = zonaAEditar!!, // Pasamos la zona a editar al diálogo (el '!!' es seguro aquí por el 'if')
            onDismissRequest = {
                showEditDialog = false
                zonaAEditar = null // Limpiamos la zona seleccionada al cerrar el diálogo
            },
            onGuardarCambios = { zonaActualizada ->
                // Pasamos la zona con los datos modificados al ViewModel
                viewModel.actualizarZona(zonaActualizada)
                showEditDialog = false
                zonaAEditar = null // Limpiamos la zona seleccionada al guardar
                Toast.makeText(context, "${zonaActualizada.nombreZona} se ha actualizado correctamente", Toast.LENGTH_SHORT).show()
            }
        )
    }
}