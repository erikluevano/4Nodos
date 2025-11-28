// ruta: app/src/main/java/com/example/movilsecure_v/vista/ui/MapaUI.kt
package com.example.movilsecure_v.vista.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.movilsecure_v.BuildConfig
import com.example.movilsecure_v.modelo.entidades.PlaceDetails
import com.example.movilsecure_v.modelo.entidades.RegistroHistorial
import com.example.movilsecure_v.viewmodel.HistorialViewModel
import com.example.movilsecure_v.viewmodel.MapViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MapaUI(modifier: Modifier = Modifier, mapViewModel: MapViewModel = viewModel()) {
    val historialViewModel: HistorialViewModel = viewModel(factory = HistorialViewModel.Factory)
    val historialUiState by historialViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                hasLocationPermission = true
            } else {
                Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }
    )
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    historialUiState.error?.let {
        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        historialViewModel.EnviarMensajeError()
    }

    var query by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("hospital") }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var selectedHistoryItem by remember { mutableStateOf<RegistroHistorial?>(null) }

    val zacatecas = LatLng(22.7709, -102.5833)
    val places by mapViewModel.places
    val selectedCardPlace by mapViewModel.selectedCardPlace
    val selectedPlaceForRoute by mapViewModel.selectedPlaceForRoute

    LaunchedEffect(Unit) {
        mapViewModel.nearbySearchPlaces(
            apiKey = BuildConfig.MAPS_API_KEY,
            type = "hospital",
            location = zacatecas,
            radius = 10000
        )
    }

    if (showHistoryDialog) {
        MuestraLista(
            historyList = historialUiState.historial,
            onDismiss = { showHistoryDialog = false },
            onItemSelected = { historyItem ->
                selectedHistoryItem = historyItem
                showHistoryDialog = false
            }
        )
    }
    selectedHistoryItem?.let {
        MostrarDetalle(
            historyItem = it,
            onDismiss = { selectedHistoryItem = null },
            onStartNavigation = {
                val gmmIntentUri = "google.navigation:q=${it.destino}".toUri()
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                context.startActivity(mapIntent)
            }
        )
    }
    selectedPlaceForRoute?.let { place ->
        DialogoRuta(
            place = place,
            onClose = { mapViewModel.clearSelectedPlaceForRoute() },
            onStartNavigation = {
                coroutineScope.launch {
                    val origenAddress = mapViewModel.getCurrentLocationAddress(context)
                    historialViewModel.activarNavegacion(
                        origen = origenAddress,
                        destino = place.name
                    )
                    val gmmIntentUri =
                        "google.navigation:q=${place.location.latitude},${place.location.longitude}".toUri()
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    context.startActivity(mapIntent)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BarraBusqueda(
                modifier = Modifier.weight(1f),
                query = query,
                onQueryChange = { newQuery -> query = newQuery },
                onSearch = {
                    mapViewModel.clearSelectedCardPlace()
                    if (query.isNotBlank()) {
                        selectedType = ""
                        mapViewModel.textSearchPlaces(
                            apiKey = BuildConfig.MAPS_API_KEY,
                            query = query,
                            location = zacatecas,
                            radius = 10000
                        )
                    }
                }
            )
            IconButton(onClick = { showHistoryDialog = true }) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Historial de navegación"
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        FichasFiltros(
            selectedType = selectedType,
            onTypeSelected = { type ->
                selectedType = type
                mapViewModel.clearSelectedCardPlace()
                query = ""
                mapViewModel.nearbySearchPlaces(
                    apiKey = BuildConfig.MAPS_API_KEY,
                    type = type,
                    location = zacatecas,
                    radius = 10000
                )
            }
        )
        Spacer(modifier = Modifier.height(12.dp))

        MapaInteractivo(
            places = places,
            onPOIClick = { placeId ->
                query = ""
                mapViewModel.getPlaceDetailsById(BuildConfig.MAPS_API_KEY, placeId)
            },
            isMyLocationEnabled = hasLocationPermission
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedCardPlace != null) {
            CartaLocalizacion(
                place = selectedCardPlace!!,
                onViewRoute = { mapViewModel.selectPlaceForRoute(selectedCardPlace!!) }
            )
        } else if (places.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(places, key = { it.id }) { place ->
                    CartaLocalizacion(
                        place = place,
                        onViewRoute = { mapViewModel.selectPlaceForRoute(place) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BarraBusqueda(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Buscar un establecimiento...") },
        trailingIcon = {
            IconButton(onClick = onSearch) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Buscar"
                )
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() })
    )
}

@Composable
fun CartaLocalizacion(
    place: PlaceDetails,
    onViewRoute: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = place.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Normal
            )

            FilaInformacion(icon = Icons.Default.LocationOn, text = place.address)
            FilaInformacion(icon = Icons.Default.Schedule, text = place.isOpen)
            place.rating?.let {
                FilaInformacion(icon = Icons.Default.Star, text = "Calificación: $it")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onViewRoute,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ver ruta")
                }

                OutlinedButton(
                    onClick = {
                        place.phoneNumber?.let {
                            val intent = Intent(Intent.ACTION_DIAL, "tel:$it".toUri())
                            context.startActivity(intent)
                        } ?: Toast.makeText(context, "Número no disponible", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Llamar")
                }
            }
        }
    }
}

@Composable
private fun FilaInformacion(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


private val types = listOf(
    "hospital" to "Hospitales",
    "doctor" to "Clínicas",
    "pharmacy" to "Farmacias"
)

@Composable
fun FichasFiltros(selectedType: String, onTypeSelected: (String) -> Unit) {
    LazyRow(modifier = Modifier.padding(start = 4.dp, end = 4.dp)) {
        items(types) { (id, label) ->
            Button(
                onClick = { onTypeSelected(id) },
                colors = if (selectedType == id) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors(),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(label)
            }
        }
    }
}

@Composable
fun MapaInteractivo(
    modifier: Modifier = Modifier,
    places: List<PlaceDetails>,
    onPOIClick: (placeId: String) -> Unit,
    isMyLocationEnabled: Boolean
) {
    val zacatecas = LatLng(22.7709, -102.5833)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(zacatecas, 12f)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        GoogleMap(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(mapToolbarEnabled = false),
            onPOIClick = { poi -> onPOIClick(poi.placeId) },
            properties = MapProperties(isMyLocationEnabled = isMyLocationEnabled)
        ) {
            places.forEach { place ->
                Marker(
                    state = MarkerState(position = place.location),
                    title = place.name,
                    snippet = place.address
                )
            }
        }
    }
}

@Composable
fun DialogoRuta(
    place: PlaceDetails,
    onClose: () -> Unit,
    onStartNavigation: () -> Unit
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(place.location, 15f)
    }

    Dialog(onDismissRequest = onClose) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                EncabezadoHistorial(locationName = place.name, onClose = onClose)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Confirma la ubicación para iniciar la navegación.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState
                    ) {
                        Marker(
                            state = MarkerState(position = place.location),
                            title = place.name,
                            snippet = place.address
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                BotonesDialogo(onClose = onClose, onStartNavigation = onStartNavigation)
            }
        }
    }
}

@Composable
private fun EncabezadoHistorial(locationName: String, onClose: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.NearMe,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Ruta a $locationName",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onClose) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
        }
    }
}

@Composable
private fun BotonesDialogo(onClose: () -> Unit, onStartNavigation: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(onClick = onClose, modifier = Modifier.weight(1f)) {
            Text("Cerrar")
        }
        Button(onClick = onStartNavigation, modifier = Modifier.weight(1.8f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NearMe,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Iniciar navegación")
            }
        }
    }
}


@Composable
fun MuestraLista(
    historyList: List<RegistroHistorial>,
    onDismiss: () -> Unit,
    onItemSelected: (RegistroHistorial) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Historial de navegación"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Historial de navegación",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Selecciona una ruta para ver los detalles.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(20.dp))

                if (historyList.isEmpty()) {
                    Text(
                        text = "No hay historial para mostrar.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp)
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(historyList) { historyItem ->
                            HistoryItemCard(
                                historyItem = historyItem,
                                onItemClick = { onItemSelected(historyItem) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(historyItem: RegistroHistorial, onItemClick: () -> Unit) {
    val (formattedDate, formattedTime) = try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = parser.parse(historyItem.fecha)!!
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormatter = SimpleDateFormat("HH:mm 'hrs'", Locale.getDefault())
        dateFormatter.format(date) to timeFormatter.format(date)
    } catch (e: Exception) {
        historyItem.fecha to "" // Fallback
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.TripOrigin,
                    contentDescription = "Origen",
                    tint = Color.Green
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Origen", style = MaterialTheme.typography.bodySmall)
            }
            Text(text = historyItem.origen, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Destino",
                    tint = Color.Red
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Destino", style = MaterialTheme.typography.bodySmall)
            }
            Text(text = historyItem.destino, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Fecha", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = formattedDate, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = "Hora", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = formattedTime, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Icon(Icons.Default.ArrowForwardIos, contentDescription = "Ver detalles")
            }
        }
    }
}

@Composable
fun MostrarDetalle(
    historyItem: RegistroHistorial,
    onDismiss: () -> Unit,
    onStartNavigation: () -> Unit
) {
    val (formattedDate, formattedTime) = try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = parser.parse(historyItem.fecha)!!
        val dateFormatter = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("es", "ES"))
        val timeFormatter = SimpleDateFormat("HH:mm 'hrs'", Locale.getDefault())
        dateFormatter.format(date) to timeFormatter.format(date)
    } catch (e: Exception) {
        historyItem.fecha to "" // Fallback
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Detalles de la navegación"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Detalles de la navegación",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Información completa de tu ruta guardada.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(20.dp))

                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Punto de origen", style = MaterialTheme.typography.bodySmall)
                        Text(historyItem.origen, fontWeight = FontWeight.Bold)
                    }
                }
                Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 4.dp))

                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE4EC))) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Punto de destino", style = MaterialTheme.typography.bodySmall)
                        Text(historyItem.destino, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Fecha y hora del registro",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = "Fecha",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(formattedDate, style = MaterialTheme.typography.bodyLarge)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = "Hora",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(formattedTime, style = MaterialTheme.typography.bodyLarge)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Volver")
                    }
                    Button(onClick = onStartNavigation, modifier = Modifier.weight(1.8f)) {
                        Icon(Icons.Default.NearMe, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Iniciar navegación")
                    }
                }
            }
        }
    }
}