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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.example.movilsecure_v.ui.theme.MovilSecure_VTheme
import com.example.movilsecure_v.view.screens.MapaScreen
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
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = { Icon(it.icon, contentDescription = it.label) },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            when (currentDestination) {
                // Llama a la nueva MapaScreen importada, pasándole el padding
                AppDestinations.HOME -> MapaScreen(modifier = Modifier.padding(innerPadding))
                AppDestinations.FAVORITES -> ZonasFrecuentesScreen(modifier = Modifier.padding(innerPadding))
                AppDestinations.PROFILE -> PlaceholderScreen("Perfil del Cuidador", innerPadding)
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Mapa", Icons.Default.Map),
    FAVORITES("Zonas Frecuentes", Icons.Outlined.Star),
    PROFILE("Perfil", Icons.Default.AccountBox),
}

// Pantallas simuladas para otras secciones
@Composable
fun PlaceholderScreen(title: String, innerPadding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
    }
}

@Preview(showBackground = true)
@Composable
fun MovilSecurePreview() {
    MovilSecure_VTheme {
        MovilSecure_VApp()
    }
}
