package com.example.movilsecure_v.view.components.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

@Composable
fun MapPlaceholder(count: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(Color(0xFFECEFF1))
    ) {
        Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.align(Alignment.Center))
        Text(text = "Mapa interactivo — mostrando $count lugares", modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp))
    }
}