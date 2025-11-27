package com.example.movilsecure_v.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilsecure_v.modelo.entidades.PerfilAdultoMayor
import com.example.movilsecure_v.modelo.repositorio.RepositorioPerfil
import com.example.movilsecure_v.modelo.servicios.ServicioPerfil
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.UUID

data class PerfilUiState(
    // Estado del formulario
    val nombre: String = "",
    val fechaNacimiento: String = "",
    val sexo: String = "",
    val historialMedico: String = "",
    val tipoDeSangre: String = "",
    val medicamentosActuales: String = "",
    val alergias: String = "",

    // Control para UI
    val menuSangreAbierto: Boolean = false,
    val listaTiposDeSangre: List<String> = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"),
    val mostrandoFormulario: Boolean = false,
    val idPerfilEditando: String? = null,

    // Eventos únicos para la UI
    val mensajeConfirmacion: String? = null,
    val mensajeError: String? = null
)

class PerfilVM(
    private val repositorio: RepositorioPerfil,
    private val servicio: ServicioPerfil
) : ViewModel() {

    // Flujo de datos para leer la lista de perfiles
    val perfilesRegistrados: StateFlow<List<PerfilAdultoMayor>> = repositorio.todosLosPerfiles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    // --- MANEJADORES DE EVENTOS DE LA UI ---

    fun onNombreChange(nombre: String) { _uiState.update { it.copy(nombre = nombre) } }
    fun onFechaNacimientoChange(fecha: String) { _uiState.update { it.copy(fechaNacimiento = fecha) } }
    fun onSexoChange(sexo: String) { _uiState.update { it.copy(sexo = sexo) } }
    fun onHistorialMedicoChange(historial: String) { _uiState.update { it.copy(historialMedico = historial) } }
    fun onTipoDeSangreChange(sangre: String) { _uiState.update { it.copy(tipoDeSangre = sangre, menuSangreAbierto = false) } }
    fun onMedicamentosChange(medicamentos: String) { _uiState.update { it.copy(medicamentosActuales = medicamentos) } }
    fun onAlergiasChange(alergias: String) { _uiState.update { it.copy(alergias = alergias) } }
    fun onMenuSangreDismissRequest(abierto: Boolean) { _uiState.update { it.copy(menuSangreAbierto = abierto) } }

    fun onMostrarFormulario() {
        limpiarCamposFormulario()
        _uiState.update { it.copy(mostrandoFormulario = true, idPerfilEditando = null) }
    }

    fun onOcultarFormulario() {
        limpiarCamposFormulario()
        _uiState.update { it.copy(mostrandoFormulario = false, idPerfilEditando = null) }
    }

    fun onMensajeErrorMostrado() { _uiState.update { it.copy(mensajeError = null) } }
    fun onMensajeConfirmacionMostrado() { _uiState.update { it.copy(mensajeConfirmacion = null) } }

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

    /**
     * Orquesta la validación y el guardado del perfil.
     * Esta es la acción principal llamada por la UI.
     */
    fun registrarOActualizarPerfil() {
        val currentState = _uiState.value
        val perfil = PerfilAdultoMayor(
            id = currentState.idPerfilEditando ?: UUID.randomUUID().toString(),
            nombre = currentState.nombre.trim(),
            fechaNacimiento = currentState.fechaNacimiento.trim(),
            sexo = currentState.sexo.trim(),
            historialMedico = currentState.historialMedico.trim(),
            tipoDeSangre = currentState.tipoDeSangre,
            medicamentosActuales = currentState.medicamentosActuales.trim(),
            alergias = currentState.alergias.trim()
        )

        if (!perfil.validarDatos()) {
            _uiState.update { it.copy(mensajeError = "Por favor, complete todos los campos obligatorios (*).") }
            return
        }

        viewModelScope.launch {
            try {
                servicio.registrarPerfil(perfil)
                val mensaje = if (currentState.idPerfilEditando != null) "Perfil actualizado" else "Perfil registrado"
                _uiState.update { it.copy(mensajeConfirmacion = "$mensaje exitosamente.") }
                onOcultarFormulario()
            } catch (e: Exception) {
                _uiState.update { it.copy(mensajeError = "No se pudo guardar el perfil.") }
            }
        }
    }

    fun eliminarPerfil(perfilId: String) {
        viewModelScope.launch {
            try {
                repositorio.eliminarPerfil(perfilId)
                _uiState.update { it.copy(mensajeConfirmacion = "Perfil eliminado exitosamente.") }
            } catch (e: Exception) {
                // CAMBIO: Añadir mensaje de error si la eliminación falla.
                _uiState.update { it.copy(mensajeError = "No se pudo eliminar el perfil.") }
            }
        }
    }

    private fun limpiarCamposFormulario() {
        _uiState.update { currentState ->
            currentState.copy(
                nombre = "",
                fechaNacimiento = "",
                sexo = "",
                historialMedico = "",
                tipoDeSangre = "",
                medicamentosActuales = "",
                alergias = "",
                idPerfilEditando = null
            )
        }
    }

    fun calcularEdad(fechaNacimiento: String): Int? {
        return try {
            // Gracias al API Desugaring, este código ahora es seguro.
            val formatter = DateTimeFormatter.ofPattern("d/M/yyyy")
            val fechaNac = LocalDate.parse(fechaNacimiento, formatter)
            Period.between(fechaNac, LocalDate.now()).years
        } catch (e: Exception) {
            // Si el formato de la fecha es incorrecto, retorna null.
            null
        }
    }
}