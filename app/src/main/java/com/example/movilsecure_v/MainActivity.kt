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
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.movilsecure_v.ui.theme.MovilSecure_VTheme
import com.example.movilsecure_v.viewmodel.MedicamentosViewModel
import com.example.movilsecure_v.vista.ui.CitasUI
import com.example.movilsecure_v.vista.ui.MapaUI
import com.example.movilsecure_v.vista.ui.MedicamentosUI
import com.example.movilsecure_v.vista.ui.PerfilScreen
import com.example.movilsecure_v.vista.ui.SeleccionarUbicacionScreen
import com.example.movilsecure_v.vista.ui.ZonasFrecuentesUI


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

@OptIn(ExperimentalMaterial3Api::class) // Anotación necesaria para TopAppBar
@Composable
fun MovilSecure_VApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val medicamentosViewModel: MedicamentosViewModel = viewModel(factory = MedicamentosViewModel.Factory)

    val showNavigationAndHeader = AppDestinations.entries.any { it.route == currentRoute && it.showInBottomBar }

    // --- CAMBIO PRINCIPAL: Usamos Scaffold para un control total del layout ---
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        // 1. AÑADIMOS EL ENCABEZADO (TopAppBar) ESTÁTICO
        topBar = {
            if (showNavigationAndHeader) {
                TopAppBar(
                    title = {
                        Text(
                            "MovilSecure",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        // 2. USAMOS NavigationBar PARA LA BARRA INFERIOR (soluciona el layout)
        bottomBar = {
            if (showNavigationAndHeader) {
                NavigationBar {
                    AppDestinations.entries.filter { it.showInBottomBar }.forEach { destination ->
                        NavigationBarItem(
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) }, // El texto se ajusta y centra correctamente
                            selected = currentRoute == destination.route,
                            onClick = { navigateTo(navController, destination.route) }
                        )
                    }
                }
            }
        }
    ) { scaffoldPadding ->
        // El contenido de la app (NavHost) se coloca aquí, con el padding correcto
        // para no quedar debajo de las barras superior e inferior.
        AppNavHost(
            navController = navController,
            medicamentosViewModel = medicamentosViewModel,
            modifier = Modifier.padding(scaffoldPadding)
        )
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    medicamentosViewModel: MedicamentosViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppDestinations.HOME.route,
        modifier = modifier // El modifier con el padding se aplica aquí
    ) {
        composable(route = AppDestinations.HOME.route) {
            MapaUI(modifier = Modifier.fillMaxSize())
        }
        composable(route = AppDestinations.FAVORITES.route) {
            ZonasFrecuentesUI(navController = navController, modifier = Modifier.fillMaxSize())
        }
        composable(route = AppDestinations.CITAS.route) {
            CitasUI(modifier = Modifier.fillMaxSize())
        }
        composable(route = AppDestinations.MEDICAMENTOS.route) {
            MedicamentosUI(
                viewModel = medicamentosViewModel,
                modifier = Modifier.fillMaxSize()
            )
        }
        composable(route = AppDestinations.PROFILE.route) {
            PerfilScreen(modifier = Modifier.fillMaxSize())
        }
        composable(route = "seleccionarUbicacion") {
            SeleccionarUbicacionScreen(
                onCancelar = { navController.popBackStack() },
                onUbicacionSeleccionada = { resultado ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("ubicacion_seleccionada", resultado)
                    navController.popBackStack()
                }
            )
        }
    }
}

fun navigateTo(navController: NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

enum class AppDestinations(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val showInBottomBar: Boolean = true
) {
    HOME("mapa", "Mapa", Icons.Default.Map),
    FAVORITES("zonas_frecuentes", "    Zonas    Frecuentes", Icons.Outlined.Star),
    CITAS("citas", "Citas", Icons.Default.Event),
    MEDICAMENTOS("medicamentos", "Medicamentos", Icons.Default.Medication),
    PROFILE("perfil", "Perfil", Icons.Default.AccountBox),
    MEDICAMENTO_DETAIL("medicamento_detalle", "Detalle", Icons.Default.Medication, showInBottomBar = false)
}