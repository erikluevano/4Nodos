// ruta: app/src/main/java/com/example/movilsecure_v/modelo/repositorio/HistorialRepositorio.kt
package com.example.movilsecure_v.modelo.repositorio

import com.example.movilsecure_v.modelo.dao.HistorialDAO
import com.example.movilsecure_v.modelo.entidades.RegistroHistorial

/**
 * Repositorio para gestionar las operaciones de datos del historial de navegación.
 * Proporciona una API limpia para que el ViewModel acceda a los datos.
 */
class HistorialRepositorio(private val historialDAO: HistorialDAO) {

    /**
     * Obtiene la lista completa del historial de navegación desde la base de datos.
     * La lista ya viene ordenada por el DAO.
     */
    suspend fun obtenerHistorial(): List<RegistroHistorial> {
        return historialDAO.getRegistros()
    }


    suspend fun guardarRegistro(registro: RegistroHistorial) {
        historialDAO.insertarRegistro(registro)
    }
}