// Archivo: app/src/main/java/com/example/movilsecure_v/viewmodel/PerfilVM.kt
package com.example.movilsecure_v.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilsecure_v.modelo.entidades.Medicamento
import com.example.movilsecure_v.modelo.entidades.PerfilAdultoMayor
import com.example.movilsecure_v.modelo.repositorio.RepositorioMedicamentos
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
    val medicamentosActuales: List<String> = emptyList(),
    val alergias: String = "",

    // Control para UI
    val menuSangreAbierto: Boolean = false,
    val listaTiposDeSangre: List<String> = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"),
    val menuSexoAbierto: Boolean = false,
    val listaSexos: List<String> = listOf("Masculino", "Femenino", "Otro"),
    val mostrandoFormulario: Boolean = false,
    val idPerfilEditando: String? = null,
    val mostrandoFormularioMedicamento: Boolean = false,

    // Eventos únicos para la UI
    val mensajeConfirmacion: String? = null,
    val mensajeError: String? = null
)

class PerfilVM(
    private val repositorio: RepositorioPerfil,
    private val servicio: ServicioPerfil,
    // CAMBIO: Añadir el repositorio de medicamentos para poder guardar
    private val repositorioMedicamentos: RepositorioMedicamentos
) : ViewModel() {

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
    fun onAlergiasChange(alergias: String) { _uiState.update { it.copy(alergias = alergias) } }
    fun onMenuSangreDismissRequest(abierto: Boolean) { _uiState.update { it.copy(menuSangreAbierto = abierto) } }

    fun onMenuSexoDismissRequest(abierto: Boolean) {
        _uiState.update { it.copy(menuSexoAbierto = abierto) }
    }

    fun onSexoSeleccionado(seleccion: String) {
        val nuevoValorSexo = if (seleccion == "Otro") "" else seleccion
        _uiState.update { it.copy(sexo = nuevoValorSexo, menuSexoAbierto = false) }
    }

    fun onEliminarMedicamento(nombreMedicamento: String) {
        _uiState.update {
            it.copy(medicamentosActuales = it.medicamentosActuales - nombreMedicamento)
        }
    }

    fun onMostrarFormularioMedicamento() {
        _uiState.update { it.copy(mostrandoFormularioMedicamento = true) }
    }

    fun onOcultarFormularioMedicamento() {
        _uiState.update { it.copy(mostrandoFormularioMedicamento = false) }
    }

    /**
     * CAMBIO CLAVE: Guarda el medicamento en la DB y actualiza el perfil.
     */
    fun guardarMedicamentoYActualizarPerfil(medicamento: Medicamento) {
        viewModelScope.launch {
            // 1. Guardar el medicamento en su propia tabla a través de su repositorio
            repositorioMedicamentos.GuardarDatosMedicamento(medicamento)

            // 2. Añadir el nombre a la lista del perfil actual
            _uiState.update {
                it.copy(
                    medicamentosActuales = it.medicamentosActuales + medicamento.nombre,
                    mostrandoFormularioMedicamento = false // Cierra el diálogo
                )
            }
        }
    }

    // --- Flujo Principal ---

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
                medicamentosActuales = perfil.medicamentosActuales.split(",").map { med -> med.trim() }.filter { med -> med.isNotEmpty() },
                alergias = perfil.alergias,
                idPerfilEditando = perfil.id,
                mostrandoFormulario = true
            )
        }
    }

    fun registrarOActualizarPerfil() {
        val currentState = _uiState.value
        val perfil = PerfilAdultoMayor(
            id = currentState.idPerfilEditando ?: UUID.randomUUID().toString(),
            nombre = currentState.nombre.trim(),
            fechaNacimiento = currentState.fechaNacimiento.trim(),
            sexo = currentState.sexo.trim(),
            historialMedico = currentState.historialMedico.trim(),
            tipoDeSangre = currentState.tipoDeSangre,
            medicamentosActuales = currentState.medicamentosActuales.joinToString(separator = ", "),
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
                medicamentosActuales = emptyList(),
                alergias = "",
                idPerfilEditando = null
            )
        }
    }

    fun calcularEdad(fechaNacimiento: String): Int? {
        return try {
            val formatter = DateTimeFormatter.ofPattern("d/M/yyyy")
            val fechaNac = LocalDate.parse(fechaNacimiento, formatter)
            Period.between(fechaNac, LocalDate.now()).years
        } catch (e: Exception) {
            null
        }
    }
}
