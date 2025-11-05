
package com.example.movilsecure_v.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilsecure_v.modelo.PerfilAdultoMayor
import com.example.movilsecure_v.modelo.repositorio.RepositorioPerfil
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PerfilUiState(
    // Estado del formulario
    val nombre: String = "",
    val fechaNacimiento: String = "",
    val sexo: String = "",
    val historialMedico: String = "",
    val tipoDeSangre: String = "",
    val medicamentosActuales: String = "",
    val alergias: String = "",

    // Control para UI del Dropdown de tipo de sangre
    val menuSangreAbierto: Boolean = false,
    val listaTiposDeSangre: List<String> = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"),

    // Estado general de la pantalla
    val mostrandoFormulario: Boolean = false,
    val idPerfilEditando: String? = null,

    // Estado para eventos únicos
    val mensajeConfirmacion: String? = null,
    val mensajeError: String? = null
)

class PerfilVM(private val repositorio: RepositorioPerfil) : ViewModel() {

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
    fun onTipoDeSangreChange(sangre: String) { _uiState.update { it.copy(tipoDeSangre = sangre, menuSangreAbierto = false) } }
    fun onMedicamentosChange(medicamentos: String) { _uiState.update { it.copy(medicamentosActuales = medicamentos) } }
    fun onAlergiasChange(alergias: String) { _uiState.update { it.copy(alergias = alergias) } }
    
    fun onMenuSangreDismissRequest(abierto: Boolean) {
        _uiState.update { it.copy(menuSangreAbierto = abierto) }
    }

    fun onMostrarFormulario() {
        limpiarCamposFormulario()
        _uiState.update { it.copy(mostrandoFormulario = true, idPerfilEditando = null) }
    }

    fun onOcultarFormulario() {
        limpiarCamposFormulario()
        _uiState.update { it.copy(mostrandoFormulario = false, idPerfilEditando = null) }
    }
    
    fun iniciarEdicion(perfil: PerfilAdultoMayor) {
        _uiState.update {
            it.copy(
                nombre = perfil.nombre,
                fechaNacimiento = perfil.fechaNacimiento,
                sexo = perfil.sexo,
                historialMedico = perfil.historialMedico,
                tipoDeSangre = perfil.tipoDeSangre,
                medicamentosActuales = perfil.medicamentosActuales,
                alergias = perfil.alergias,
                idPerfilEditando = perfil.id,
                mostrandoFormulario = true
            )
        }
    }

    fun onMensajeErrorMostrado() { _uiState.update { it.copy(mensajeError = null) } }
    fun onMensajeConfirmacionMostrado() { _uiState.update { it.copy(mensajeConfirmacion = null) } }
    
    fun registrarOActualizarPerfil() {
        val currentState = _uiState.value
        // --- VALIDACIÓN MEJORADA ---
        if (currentState.nombre.isBlank() ||
            currentState.fechaNacimiento.isBlank() ||
            currentState.sexo.isBlank() ||
            currentState.tipoDeSangre.isBlank() ||
            currentState.medicamentosActuales.isBlank() ||
            currentState.alergias.isBlank()) {
            _uiState.update { it.copy(mensajeError = "Por favor, complete todos los campos obligatorios (*).") }
            return
        }

        val perfil = PerfilAdultoMayor(
            id = currentState.idPerfilEditando ?: java.util.UUID.randomUUID().toString(),
            nombre = currentState.nombre,
            fechaNacimiento = currentState.fechaNacimiento,
            sexo = currentState.sexo,
            historialMedico = currentState.historialMedico, // Campo opcional
            tipoDeSangre = currentState.tipoDeSangre,
            medicamentosActuales = currentState.medicamentosActuales,
            alergias = currentState.alergias
        )

        viewModelScope.launch {
            try {
                repositorio.registrarPerfil(perfil)
                val mensaje = if (currentState.idPerfilEditando != null) "Perfil actualizado" else "Perfil registrado"
                _uiState.update { it.copy(mensajeConfirmacion = "$mensaje exitosamente.") }
                onOcultarFormulario()
            } catch (e: Exception) {
                _uiState.update { it.copy(mensajeError = "No se pudo guardar el perfil.") }
            }
        }
    }

    fun eliminarPerfil(perfilId: String) {
        viewModelScope.launch { repositorio.eliminarPerfil(perfilId) }
    }

    private fun limpiarCamposFormulario() {
        _uiState.update {
            it.copy(
                nombre = "", fechaNacimiento = "", sexo = "",
                historialMedico = "", tipoDeSangre = "",
                medicamentosActuales = "", alergias = "",
                idPerfilEditando = null
            )
        }
    }
}
