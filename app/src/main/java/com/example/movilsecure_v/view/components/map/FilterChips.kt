package com.example.movilsecure_v.view.components.map

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val types = listOf(
    "all" to "Todos",
    "hospital" to "Hospitales",
    "clinic" to "Clínicas",
    "pharmacy" to "Farmacias"
)

@Composable
fun FilterChips(selectedType: String, onTypeSelected: (String) -> Unit) {
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
