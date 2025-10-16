package com.example.movilsecure_v.view.components.map

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange, // Esto solo actualiza el texto
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Buscar un establecimiento...") },
        trailingIcon = {
            IconButton(onClick = onSearch) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Buscar"
                )
            }
        },
        singleLine = true, // Evita el salto de línea
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search // Muestra un botón de "Buscar" en el teclado
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onSearch() } // Llama a onSearch cuando se presiona el botón del teclado
        )
    )
}