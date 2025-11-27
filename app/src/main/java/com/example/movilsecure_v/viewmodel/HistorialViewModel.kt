package com.example.movilsecure_v.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.movilsecure_v.modelo.basedatos.AppDatabase
import com.example.movilsecure_v.modelo.entidades.RegistroHistorial
import com.example.movilsecure_v.modelo.repositorio.HistorialRepositorio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Estado de la UI para el Historial
data class HistorialUiState(
    val historial: List<RegistroHistorial> = emptyList(),
    val error: String? = null,
    val isLoading: Boolean = false
)

class HistorialViewModel(private val repositorio: HistorialRepositorio) : ViewModel() {

    private val _uiState = MutableStateFlow(HistorialUiState())
    val uiState: StateFlow<HistorialUiState> = _uiState

    init {

        cargarHistorial()
    }

    fun cargarHistorial() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val historial = repositorio.obtenerHistorial()
                onResultado(historial)
            } catch (e: Exception) {
                e.printStackTrace()
                onCargaHistorialFallida("Error al cargar la información")
            }
        }
    }

    fun activarNavegacion(origen: String, destino: String) {
        viewModelScope.launch {
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val fechaActual = dateFormat.format(Date())

                val nuevoRegistro = RegistroHistorial(
                    origen = origen,
                    destino = destino,
                    fecha = fechaActual
                )
                repositorio.guardarRegistro(nuevoRegistro)
                cargarHistorial()
            } catch (e: Exception) {
                e.printStackTrace()
                onCargaHistorialFallida("Error al guardar el registro")
            }
        }
    }

    private fun onResultado(lista: List<RegistroHistorial>) {
        _uiState.value = HistorialUiState(historial = lista)
    }

    /**
     * Metodo privado para manejar un fallo en la carga o guardado.
     * Cumple con 'onCargaHistorialFallida' y 'enviarMensajeError'.
     */
    private fun onCargaHistorialFallida(error: String) {
        _uiState.value = _uiState.value.copy(error = error, isLoading = false)
    }

    /**
     * Permite a la UI notificar que el mensaje de error ya se mostró.
     */
    fun EnviarMensajeError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    // --- Factory para la inyección de dependencias ---
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val repositorio = HistorialRepositorio(AppDatabase.getDatabase(application).historialDAO())
                return HistorialViewModel(repositorio) as T
            }
        }
    }
}