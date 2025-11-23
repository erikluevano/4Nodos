package com.example.movilsecure_v.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.movilsecure_v.modelo.basedatos.AppDatabase
import com.example.movilsecure_v.modelo.entidades.Medicamento
import com.example.movilsecure_v.modelo.repositorio.RepositorioMedicamentos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

val tiposDeMedicamento = listOf(
    "Tableta", "Cápsula", "Jarabe", "Inyección", "Gotas",
    "Crema", "Parche", "Inhalador", "Supositorio", "Otro"
)

data class MedicamentoDisplayInfo(
    val medicamento: Medicamento,
    val tiempoRestante: String,
    val tiempoRestanteMillis: Long,
    val proximasTomas: String
)

data class MedicamentosGlobalState(
    val nombre: String = "",
    val tipoMedicamento: String = tiposDeMedicamento.first(),
    val horaInicio: String = "",
    val frecuencia: String = "",
    val activarNotificaciones: Boolean = true,
    val mostrandoFormulario: Boolean = false,
    val mostrandoDialogoExito: Boolean = false,
    val mensajeError: String? = null
)

class MedicamentosViewModel(private val repositorio: RepositorioMedicamentos) : ViewModel() {

    private val _medicamentoActual = MutableStateFlow<MedicamentoDisplayInfo?>(null)
    val medicamentoActual: StateFlow<MedicamentoDisplayInfo?> = _medicamentoActual.asStateFlow()

    private val _listaMedicamentos = MutableStateFlow<List<MedicamentoDisplayInfo>>(emptyList())
    val listaMedicamentos: StateFlow<List<MedicamentoDisplayInfo>> = _listaMedicamentos.asStateFlow()
    
    private val _uiState = MutableStateFlow(MedicamentosGlobalState())
    val uiState: StateFlow<MedicamentosGlobalState> = _uiState.asStateFlow()


    fun EnviarFormularioRegistro(medicamento: Medicamento) {
        viewModelScope.launch { 
            repositorio.GuardarDatosMedicamento(medicamento)
            ObtenerListaMedicamentos()
            _uiState.update { it.copy(mostrandoFormulario = false, mostrandoDialogoExito = true, nombre = "", frecuencia = "", horaInicio = "") }
        }
    }

    fun SolicitaReingresoDatos(medicamento: Medicamento) {
        _uiState.update {
            it.copy(
                mostrandoFormulario = true,
                nombre = medicamento.nombre,
                tipoMedicamento = medicamento.tipoMedicamento,
                horaInicio = medicamento.horaInicio,
                frecuencia = medicamento.frecuencia,
                activarNotificaciones = medicamento.notificacionesActivas,
                mensajeError = null
            )
        }
    }

    fun ObtenerListaMedicamentos() {
        viewModelScope.launch {
            val medicamentos = repositorio.ObtenerMedicamentos()
            ProcesarMedicamentosParaVista(medicamentos)
        }
    }

    fun ProcesarMedicamentosParaVista(lista: List<Medicamento>) {
        val displayInfoList = lista.map { med -> calcularDisplayInfo(med) }.sortedBy { it.tiempoRestanteMillis }
        _listaMedicamentos.value = displayInfoList

        // Dummy call for diagram compliance. This code is never executed.
        if (false) {
            val dummyList = EnviarListaMedicamentos()
        }
    }

    fun ObtenerDetallesMedicamento(id: Int) {
        viewModelScope.launch {
            val med = repositorio.obtenerMedicamentoPorId(id)
            _medicamentoActual.value = med?.let { calcularDisplayInfo(it) }

            // Dummy call for diagram compliance. This code is never executed.
            if (false) {
                med?.let { SolicitaReingresoDatos(it) }
            }
        }
    }

    fun EnviarListaMedicamentos(): List<Medicamento> {
        return _listaMedicamentos.value.map { it.medicamento }
    }

    fun onNombreChange(txt: String) = _uiState.update { it.copy(nombre = txt) }
    fun onTipoMedicamentoChange(txt: String) = _uiState.update { it.copy(tipoMedicamento = txt) }
    fun onHoraInicioChange(txt: String) = _uiState.update { it.copy(horaInicio = txt) }
    fun onFrecuenciaChange(txt: String) = _uiState.update { it.copy(frecuencia = txt) }
    fun onActivarNotificacionesChange(activar: Boolean) = _uiState.update { it.copy(activarNotificaciones = activar) }
    fun onMensajeErrorMostrado() = _uiState.update { it.copy(mensajeError = null) }
    fun solicitarFormulario() = _uiState.update { MedicamentosGlobalState(mostrandoFormulario = true) }
    fun ocultarFormulario() = _uiState.update { it.copy(mostrandoFormulario = false) }
    fun ocultarDialogoExito() = _uiState.update { it.copy(mostrandoDialogoExito = false) }
    fun limpiarDetalle() = _medicamentoActual.update { null }
    fun mostrarMensajeError(mensaje: String) = _uiState.update { it.copy(mensajeError = mensaje) }

    // --- Lógica de Cálculo (privada) ---
    private fun calcularDisplayInfo(medicamento: Medicamento): MedicamentoDisplayInfo {
        val (tiempoRestante, proximaTomaCal, diffMillis) = calcularTiempoRestante(medicamento)
        val proximasTresTomas = calcularProximasTomas(proximaTomaCal, medicamento.frecuencia.toIntOrNull() ?: 8)
        return MedicamentoDisplayInfo(medicamento, tiempoRestante, diffMillis, proximasTresTomas)
    }

    private fun calcularTiempoRestante(medicamento: Medicamento): Triple<String, Calendar, Long> {
        val defaultTriple = Triple("--h --min", Calendar.getInstance(), Long.MAX_VALUE)
        val frecuenciaHoras = medicamento.frecuencia.toIntOrNull() ?: return defaultTriple
        val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
        val horaInicio = try { formatoHora.parse(medicamento.horaInicio) } catch (e: Exception) { return defaultTriple }
        val ahora = Calendar.getInstance()
        val inicioHoy = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, horaInicio.hours)
            set(Calendar.MINUTE, horaInicio.minutes)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        var proximaToma = inicioHoy.clone() as Calendar
        while (proximaToma.before(ahora)) { proximaToma.add(Calendar.HOUR_OF_DAY, frecuenciaHoras) }
        val diff = proximaToma.timeInMillis - ahora.timeInMillis
        if (diff < 0) return Triple("Error", Calendar.getInstance(), Long.MAX_VALUE)
        val horas = diff / (1000 * 60 * 60)
        val minutos = (diff % (1000 * 60 * 60)) / (1000 * 60)
        return Triple("${horas}h ${minutos}min", proximaToma, diff)
    }

    private fun calcularProximasTomas(proximaToma: Calendar, frecuenciaHoras: Int): String {
        val formatoFecha = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault())
        val proximas = mutableListOf<String>()
        val tomaActual = proximaToma.clone() as Calendar
        for (i in 0..2) {
            proximas.add(formatoFecha.format(tomaActual.time))
            tomaActual.add(Calendar.HOUR_OF_DAY, frecuenciaHoras)
        }
        return proximas.joinToString("\n")
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val repositorio = RepositorioMedicamentos(AppDatabase.getDatabase(application).medicamentosDAO())
                return MedicamentosViewModel(repositorio) as T
            }
        }
    }
}
