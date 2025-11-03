
package com.example.movilsecure_v.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilsecure_v.modelo.PerfilAdultoMayor
import com.example.movilsecure_v.modelo.repositorio.RepositorioPerfil
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// El UiState ya no necesita mantener la lista de perfiles,
// la obtendremos directamente del Flow del repositorio.
data class PerfilUiState(
    val nombre: String = "",
    val fechaNacimiento: String = "",
    val sexo: String = "",
    val historialMedico: String = "",
    val tipoDeSangre: String = "",
    val medicamentosActuales: String = "",
    val alergias: String = "",
    val mostrandoFormulario: Boolean = false,
    val mensajeConfirmacion: String? = null,
    val mensajeError: String? = null
)

class PerfilVM(private val repositorio: RepositorioPerfil) : ViewModel() {

    // Este es el Flow que viene directamente de la base de datos
    val perfilesRegistrados: StateFlow<List<PerfilAdultoMayor>> = repositorio.todosLosPerfiles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    fun onNombreChange(nombre: String) { _uiState.update { it.copy(nombre = nombre) } }
    fun onFechaNacimientoChange(fecha: String) { _uiState.update { it.copy(fechaNacimiento = fecha) } }
    fun onSexoChange(sexo: String) { _uiState.update { it.copy(sexo = sexo) } }
    fun onHistorialMedicoChange(historial: String) { _uiState.update { it.copy(historialMedico = historial) } }
    fun onTipoDeSangreChange(sangre: String) { _uiState.update { it.copy(tipoDeSangre = sangre) } }
    fun onMedicamentosChange(medicamentos: String) { _uiState.update { it.copy(medicamentosActuales = medicamentos) } }
    fun onAlergiasChange(alergias: String) { _uiState.update { it.copy(alergias = alergias) } }

    fun onMostrarFormulario() { _uiState.update { it.copy(mostrandoFormulario = true) } }
    fun onOcultarFormulario() {
        _uiState.update { it.copy(mostrandoFormulario = false) }
        limpiarCamposFormulario()
    }
    fun onMensajeErrorMostrado() { _uiState.update { it.copy(mensajeError = null) } }
    fun onMensajeConfirmacionMostrado() { _uiState.update { it.copy(mensajeConfirmacion = null) } }

    fun registrarPerfil() {
        val currentState = _uiState.value
        val perfilValido = currentState.nombre.isNotBlank() &&
                           currentState.fechaNacimiento.isNotBlank() &&
                           currentState.sexo.isNotBlank()

        if (!perfilValido) {
            _uiState.update { it.copy(mensajeError = "Por favor, complete todos los campos obligatorios.") }
            return
        }

        val nuevoPerfil = PerfilAdultoMayor(
            nombre = currentState.nombre,
            fechaNacimiento = currentState.fechaNacimiento,
            sexo = currentState.sexo,
            historialMedico = currentState.historialMedico,
            tipoDeSangre = currentState.tipoDeSangre,
            medicamentosActuales = currentState.medicamentosActuales,
            alergias = currentState.alergias
        )

        viewModelScope.launch {
            try {
                repositorio.registrarPerfil(nuevoPerfil)
                _uiState.update {
                    it.copy(
                        mostrandoFormulario = false,
                        mensajeConfirmacion = "Perfil registrado exitosamente"
                    )
                }
                limpiarCamposFormulario()
            } catch (e: Exception) {
                _uiState.update { it.copy(mensajeError = "No se pudo completar el registro.") }
            }
        }
    }
    
    fun eliminarPerfil(perfilId: String) {
        viewModelScope.launch {
            repositorio.eliminarPerfil(perfilId)
        }
    }

    private fun limpiarCamposFormulario() {
        _uiState.update {
            it.copy(
                nombre = "", fechaNacimiento = "", sexo = "",
                historialMedico = "", tipoDeSangre = "",
                medicamentosActuales = "", alergias = ""
            )
        }
    }
}
