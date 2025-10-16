package com.example.movilsecure_v

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.movilsecure_v.ui.theme.MovilSecure_VTheme
import com.example.movilsecure_v.view.screens.MapaScreen
import com.example.movilsecure_v.view.screens.SeleccionarUbicacionScreen
//import com.example.movilsecure_v.view.screens.UbicacionResult
import com.example.movilsecure_v.view.screens.ZonasFrecuentesScreen


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

@Composable
fun MovilSecure_VApp() {
    // CAMBIO 1: Crear el NavController, que será la fuente de verdad para la navegación
    val navController = rememberNavController()
    // Obtenemos la ruta actual para saber qué ítem de la barra de navegación resaltar
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            // CAMBIO 2: El onClick ahora navega usando el navController
            AppDestinations.entries.forEach { destination ->
                item(
                    icon = { Icon(destination.icon, contentDescription = destination.label) },
                    label = { Text(destination.label) },
                    // CAMBIO 3: El ítem se selecciona si su ruta coincide con la ruta actual
                    selected = currentRoute == destination.route,
                    onClick = {
                        navController.navigate(destination.route) {
                            // Lógica para evitar apilar la misma pantalla múltiples veces
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) {
        // CAMBIO 4: El 'when' se reemplaza por un NavHost que contiene todas las pantallas
        NavHost(
            navController = navController,
            startDestination = AppDestinations.HOME.route, // La ruta inicial
            modifier = Modifier.fillMaxSize()
        ) {
            // --- Destinos de la barra de navegación ---
            composable(route = AppDestinations.HOME.route) {
                MapaScreen(modifier = Modifier.fillMaxSize())
            }
            composable(route = AppDestinations.FAVORITES.route) {
                // Pasamos el navController a la pantalla que lo necesita
                ZonasFrecuentesScreen(navController = navController, modifier = Modifier.fillMaxSize())
            }
            composable(route = AppDestinations.PROFILE.route) {
                PlaceholderScreen("Perfil del Cuidador")
            }

            // --- Otros destinos (que no están en la barra de navegación) ---
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

// CAMBIO 5: Añadimos una propiedad 'route' al enum para que sea más robusto
enum class AppDestinations(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    HOME("mapa", "Mapa", Icons.Default.Map),
    FAVORITES("zonas_frecuentes", "Zonas Frecuentes", Icons.Outlined.Star),
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