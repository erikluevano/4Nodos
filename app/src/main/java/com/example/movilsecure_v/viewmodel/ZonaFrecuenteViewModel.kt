// Archivo: viewmodel/ZonaFrecuenteViewModel.kt

package com.example.movilsecure_v.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.movilsecure_v.model.entities.ZonaFrecuente
import com.example.movilsecure_v.model.repository.ZonaFrecuenteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar toda la lógica de negocio y los datos de las Zonas Frecuentes.
 *
 * @param repository El repositorio que provee acceso a los datos de las zonas.
 */
class ZonaFrecuenteViewModel(private val repository: ZonaFrecuenteRepository) : ViewModel() {

    // --- ESTADO OBSERVABLE PARA LA UI ---

    /**
     * Un StateFlow que emite la lista actual de todas las zonas frecuentes.
     * La UI observará este flujo para actualizarse automáticamente cuando los datos cambien.
     */
    val todasLasZonas: StateFlow<List<ZonaFrecuente>> = repository.todasLasZonas
        .stateIn(
            scope = viewModelScope, // El ámbito de vida del ViewModel
            // Inicia el flujo cuando la UI está visible y lo mantiene por 5s para evitar reinicios en cambios de config.
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList() // Provee una lista vacía como valor inicial
        )


    // --- ACCIONES DEL USUARIO ---

    /**
     * Inserta una nueva zona frecuente en la base de datos.
     * Se ejecuta en una corrutina para no bloquear el hilo principal.
     */
    fun agregarZona(nombre: String, direccion: String?, lat: Double, lon: Double, nota: String?) {
        viewModelScope.launch {
            val nuevaZona = ZonaFrecuente(
                nombreZona = nombre,
                direccionZona = direccion,
                latitud = lat,
                longitud = lon,
                notaZona = nota
            )
            repository.insertar(nuevaZona)
        }
    }

    /**
     * Actualiza una zona frecuente existente en la base de datos.
     */
    fun actualizarZona(zona: ZonaFrecuente) {
        viewModelScope.launch {
            repository.actualizar(zona)
        }
    }

    /**
     * Elimina una zona frecuente de la base de datos.
     */
    fun eliminarZona(zona: ZonaFrecuente) {
        viewModelScope.launch {
            repository.eliminar(zona)
        }
    }
}

/**
 * Factory para crear una instancia de ZonaFrecuenteViewModel con sus dependencias (el repositorio).
 * Esto es necesario porque el ViewModel tiene un constructor que no está vacío.
 */
class ZonaFrecuenteViewModelFactory(private val repository: ZonaFrecuenteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ZonaFrecuenteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ZonaFrecuenteViewModel(repository) as T
        }
        throw IllegalArgumentException("Clase de ViewModel desconocida")
    }
}