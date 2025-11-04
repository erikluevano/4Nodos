package com.example.movilsecure_v.modelo.repositorio

import android.app.Application
import com.example.movilsecure_v.modelo.Cita
import com.example.movilsecure_v.modelo.basedatos.AppDatabase
import java.util.Date

class RepositorioCitas(application: Application) {

    private val citasDao = AppDatabase.getDatabase(application).citasDao()

    suspend fun GuardarDatosCita(cita: Cita) {
        citasDao.InsertarDatosCita(cita)
    }

    /**
     * Obtiene solo las citas futuras (incluyendo el día de hoy) y las ordena de la más cercana a la más lejana.
     */
    suspend fun RetornarListaCitas(): List<Cita> {
        val hoy = Date()
        // Usamos calendar para poner la hora a 00:00:00 y que incluya las citas de hoy
        val cal = java.util.Calendar.getInstance()
        cal.time = hoy
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        val inicioDelDia = cal.time

        return citasDao.ConsultarCitasRegistradas()
            .filter { !it.fecha.before(inicioDelDia) } // Filtra para que la fecha no sea anterior a hoy
            .sortedBy { it.fecha } // Ordena de más cercana a más lejana
    }

    /**
     * Obtiene TODAS las citas y las ordena de la más antigua a la más reciente.
     */
    suspend fun ObtenerCitasAntiguas(): List<Cita> {
        return citasDao.ConsultarCitasRegistradas().sortedBy { it.fecha }
    }
}
