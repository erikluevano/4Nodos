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
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.movilsecure_v.ui.theme.MovilSecure_VTheme
import com.example.movilsecure_v.viewmodel.MedicamentosViewModel
import com.example.movilsecure_v.vista.ui.CitasUI
import com.example.movilsecure_v.vista.ui.DetalleMedicamentoScreen
import com.example.movilsecure_v.vista.ui.MapaScreen
import com.example.movilsecure_v.vista.ui.MedicamentosUI
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

@Composable
fun MovilSecure_VApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val medicamentosViewModel: MedicamentosViewModel = viewModel(factory = MedicamentosViewModel.Factory)

    val showNavigationSuite = AppDestinations.entries.any { it.route == currentRoute && it.showInBottomBar }

    Scaffold(modifier = Modifier.fillMaxSize()) { scaffoldPadding ->
        if (showNavigationSuite) {
            NavigationSuiteScaffold(
                navigationSuiteItems = {
                    AppDestinations.entries.filter { it.showInBottomBar }.forEach { destination ->
                        item(
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) },
                            selected = currentRoute == destination.route,
                            onClick = { navigateTo(navController, destination.route) }
                        )
                    }
                }
            ) {
                // CORREGIDO: Se usa el padding del Scaffold principal
                AppNavHost(navController = navController, medicamentosViewModel = medicamentosViewModel, modifier = Modifier.padding(scaffoldPadding))
            }
        } else {
             AppNavHost(navController = navController, medicamentosViewModel = medicamentosViewModel, modifier = Modifier.padding(scaffoldPadding))
        }
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
        modifier = modifier
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
        composable(route = AppDestinations.MEDICAMENTOS.route) {
            MedicamentosUI(
                viewModel = medicamentosViewModel,
                onMedicamentoClick = { id -> navController.navigate("${AppDestinations.MEDICAMENTO_DETAIL.route}/$id") },
                modifier = Modifier.fillMaxSize()
            )
        }
        composable(route = AppDestinations.PROFILE.route) {
            PerfilScreen(modifier = Modifier.fillMaxSize())
        }
        composable(
            route = "${AppDestinations.MEDICAMENTO_DETAIL.route}/{medicamentoId}",
            arguments = listOf(navArgument("medicamentoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val medicamentoId = requireNotNull(backStackEntry.arguments?.getInt("medicamentoId"))
            DetalleMedicamentoScreen(
                viewModel = medicamentosViewModel,
                onBack = { 
                    medicamentosViewModel.limpiarDetalle()
                    navController.popBackStack() 
                },
                medicamentoId = medicamentoId, 
                modifier = Modifier.fillMaxSize()
            )
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
    FAVORITES("zonas_frecuentes", "Zonas Frecuentes", Icons.Outlined.Star),
    CITAS("citas", "Citas", Icons.Default.Event),
    MEDICAMENTOS("medicamentos", "Medicamentos", Icons.Default.Medication),
    PROFILE("perfil", "Perfil", Icons.Default.AccountBox),
    MEDICAMENTO_DETAIL("medicamento_detalle", "Detalle", Icons.Default.Medication, showInBottomBar = false)
}
