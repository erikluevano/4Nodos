package com.example.movilsecure_v.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilsecure_v.modelo.entidades.Cita
import com.example.movilsecure_v.modelo.repositorio.RepositorioCitas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.text.ParseException

class CitasViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = RepositorioCitas(application)

    // este es el atributo -citas que pedia el diagrama
    private val _citas = MutableStateFlow<List<Cita>>(emptyList())
    val citas: StateFlow<List<Cita>> = _citas.asStateFlow()

    // este es el atributo -citaActual que pedia el diagrama
    private var citaActual: Cita? = null

    private val _uiState = MutableStateFlow<UiState?>(null)
    val uiState: StateFlow<UiState?> = _uiState.asStateFlow()

    private val _filtroActual = MutableStateFlow(FiltroCitas.PROXIMAS)
    val filtroActual: StateFlow<FiltroCitas> = _filtroActual.asStateFlow()

    init {
        // aqui llamo al metodo al iniciar
        EnviarListaCitas()
    }

    fun EnviarFormularioRegistro(fechaStr: String, horaStr: String, lugar: String, motivo: String) {
        if (!VerificarCamposObligatoriosEstenLlenos(fechaStr, horaStr, lugar)) {

            return
        }
        val fechaDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(fechaStr)!!

        val nuevaCita = Cita(fecha = fechaDate, hora = horaStr, lugar = lugar, motivo = motivo)
        GuardarDatosCita(nuevaCita)
    }

    fun GuardarDatosCita(cita: Cita) {
        // aqui guardo la cita en el repositorio y actualizo todo
        viewModelScope.launch {
            repositorio.GuardarDatosCita(cita)
            citaActual = cita // actualizo la cita actual

            // Determino que filtro activar despues de guardar la cita.
            val cal = Calendar.getInstance()
            cal.time = Date() // Fecha y hora actual
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val inicioDelDia = cal.time

            // Si la fecha de la cita es anterior al inicio del día de hoy, es una cita antigua.
            if (cita.fecha.before(inicioDelDia)) {
                _filtroActual.value = FiltroCitas.ANTIGUAS
            } else {
                _filtroActual.value = FiltroCitas.PROXIMAS
            }

            refrescarCitas() // Ahora refrescaro la lista correcta (próximas o antiguas)
            _uiState.value = UiState.Exito("¡Cita registrada con éxito!")
        }
    }

    fun posponerCita(citaOriginal: Cita, nuevaFechaStr: String, nuevaHoraStr: String) {
        // 1. Validar que los campos no estén vacíos y tengan el formato correcto.
        if (nuevaFechaStr.isBlank() || nuevaHoraStr.isBlank()) {
            _uiState.value = UiState.Error("Debes proporcionar una fecha y una hora.")
            return
        }
        val nuevaFechaDate = validarYConvertirFecha(nuevaFechaStr)
        if (nuevaFechaDate == null) {
            // El mensaje de error ya se estableció en la función de validación
            return
        }
        if (!validarHora(nuevaHoraStr)) {
            // El mensaje de error ya se estableció
            return
        }

        // 2. Combinar fecha y hora para poder compararlas
        val formatoCompleto = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        try {
            val formatoFechaOriginal = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fechaOriginalStr = formatoFechaOriginal.format(citaOriginal.fecha)

            val originalCombinedDateTime = formatoCompleto.parse("$fechaOriginalStr ${citaOriginal.hora}")!!
            val newCombinedDateTime = formatoCompleto.parse("$nuevaFechaStr $nuevaHoraStr")!!

            // 3. Validar que la nueva fecha/hora sea posterior a la original
            if (!newCombinedDateTime.after(originalCombinedDateTime)) {
                _uiState.value = UiState.Error("La nueva fecha y hora deben ser posteriores a la cita original.")
                return
            }

        } catch (e: ParseException) {
            _uiState.value = UiState.Error("Error interno al comparar las fechas.")
            return
        }


        // 4. Si la validación es exitosa, proceder a guardar.
        val citaActualizada = citaOriginal.copy(
            fecha = nuevaFechaDate,
            hora = nuevaHoraStr
        )

        viewModelScope.launch {
            try {
                repositorio.guardarActualizacion(citaActualizada)
                refrescarCitas() // Actualizar la lista en la UI
                _uiState.value = UiState.Exito("¡Cita pospuesta con éxito!")
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error al posponer la cita. Inténtalo de nuevo.")
            }
        }
    }

    fun ObtenerCitasRegistradas(): List<Cita> {
        // este metodo devuelve la lista actual de citas que tengo en el viewmodel
        return _citas.value
    }

    fun EnviarListaCitas() {
        ObtenerCitasRegistradas()
        refrescarCitas()
    }

    fun AplicarFiltradoCitas(criterio: String) {
        val nuevoFiltro = when (criterio.uppercase(Locale.ROOT)) {
            "PROXIMAS" -> FiltroCitas.PROXIMAS
            "ANTIGUAS" -> FiltroCitas.ANTIGUAS
            else -> _filtroActual.value // si no, me quedo como estaba
        }
        _filtroActual.value = nuevoFiltro
        refrescarCitas()
    }

    /**
     * Verifica que los campos obligatorios del formulario no estén vacíos y tengan el formato correcto.
     * Actualiza el _uiState con un mensaje de error si la validación falla.
     * @return `true` si todos los campos son válidos, `false` en caso contrario.
     */
    fun VerificarCamposObligatoriosEstenLlenos(fechaStr: String, horaStr: String, lugar: String): Boolean {
        if (lugar.isBlank()) {
            _uiState.value = UiState.Error("El campo 'Lugar' es obligatorio.")
            return false
        }

        if (validarYConvertirFecha(fechaStr) == null) {
            return false
        }

        if (!validarHora(horaStr)) {
            return false
        }


        return true
    }

    private fun validarYConvertirFecha(fechaStr: String): Date? {
        if (!fechaStr.matches("""\d{2}/\d{2}/\d{4}""".toRegex())) {
            _uiState.value = UiState.Error("Formato de fecha inválido. Usa DD/MM/AAAA.")
            return null
        }
        val parts = fechaStr.split("/")
        val dia = parts[0].toIntOrNull()
        val mes = parts[1].toIntOrNull()
        val anio = parts[2].toIntOrNull()

        if (dia == null || mes == null || anio == null || dia !in 1..31 || mes !in 1..12 || anio !in 1875..2125) {
            _uiState.value = UiState.Error("Valores de fecha fuera de rango.")
            return null
        }

        return try {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { isLenient = false }.parse(fechaStr)
        } catch (e: Exception) {
            _uiState.value = UiState.Error("La fecha ingresada no es válida.")
            null
        }
    }

    private fun validarHora(horaStr: String): Boolean {
        if (!horaStr.matches("""\d{2}:\d{2}""".toRegex())) {
            _uiState.value = UiState.Error("Formato de hora inválido. Usa HH:MM (24h).")
            return false
        }
        val parts = horaStr.split(":")
        val hora = parts[0].toIntOrNull()
        val minuto = parts[1].toIntOrNull()

        if (hora == null || minuto == null || hora !in 0..23 || minuto !in 0..59) {
            _uiState.value = UiState.Error("Valores de hora fuera de rango.")
            return false
        }
        return true
    }

    fun cambiarFiltro(nuevoFiltro: FiltroCitas) {
        AplicarFiltradoCitas(nuevoFiltro.name)
    }

    fun consumirUiState() {
        _uiState.value = null
    }

    private fun refrescarCitas() {
        viewModelScope.launch {
            _citas.value = when (_filtroActual.value) {
                FiltroCitas.PROXIMAS -> repositorio.RetornarListaCitas()
                FiltroCitas.ANTIGUAS -> repositorio.ObtenerCitasAntiguas()
            }
        }
    }

    fun eliminarCita(cita: Cita) {
        viewModelScope.launch {
            repositorio.guardarEliminacion(cita)
            refrescarCitas()
        }
    }
}

sealed class UiState(val mensaje: String) {
    class Exito(mensaje: String) : UiState(mensaje)
    class Error(mensaje: String) : UiState(mensaje)
}

enum class FiltroCitas {
    PROXIMAS, ANTIGUAS
}
