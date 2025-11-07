package com.example.movilsecure_v

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.movilsecure_v.ui.theme.MovilSecure_VTheme
import com.example.movilsecure_v.vista.ui.CitasUI // Importamos la nueva pantalla
import com.example.movilsecure_v.vista.ui.MapaScreen
import com.example.movilsecure_v.vista.ui.PerfilScreen
import com.example.movilsecure_v.vista.ui.SeleccionarUbicacionScreen
import com.example.movilsecure_v.vista.ui.ZonasFrecuentesScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MovilSecure_VTheme {
                MovilSecure_VApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Necesario para TopAppBar
@Composable
fun MovilSecure_VApp() {

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val currentDestination = AppDestinations.entries.find { it.route == currentRoute }
    val currentTitle = currentDestination?.label?.replace("\n", " ")?.trim() ?: ""

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach { destination ->
                item(
                    icon = { Icon(destination.icon, contentDescription = destination.label) },
                    label = { Text(destination.label) },
                    selected = currentRoute == destination.route,
                    onClick = {
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "MovilSecure",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = AppDestinations.HOME.route,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                composable(route = AppDestinations.HOME.route) {
                    MapaScreen(modifier = Modifier.fillMaxSize())
                }
                composable(route = AppDestinations.FAVORITES.route) {
                    ZonasFrecuentesScreen(navController = navController, modifier = Modifier.fillMaxSize())
                }
                composable(route = AppDestinations.CITAS.route) {
                    CitasUI(modifier = Modifier.fillMaxSize())
                }
                composable(route = AppDestinations.PROFILE.route) {
                    PerfilScreen(modifier = Modifier.fillMaxSize())
                }
                composable(route = "seleccionarUbicacion") {
                    SeleccionarUbicacionScreen(
                        onCancelar = { navController.popBackStack() },
                        onUbicacionSeleccionada = { resultado ->
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("ubicacion_seleccionada", resultado)
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}


enum class AppDestinations(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    HOME("mapa", "Mapa", Icons.Default.Map),
    FAVORITES("zonas_frecuentes", "     Zonas\nFrecuentes", Icons.Outlined.Star),
    CITAS("citas", "Citas", Icons.Default.Event), // Nuevo destino
    PROFILE("perfil", "Perfil", Icons.Default.AccountBox),
}

@Composable
fun PlaceholderScreen(title: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
    }
}