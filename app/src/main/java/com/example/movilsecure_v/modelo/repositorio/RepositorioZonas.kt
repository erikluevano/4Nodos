// Archivo: data/ZonaFrecuenteRepository.kt

package com.example.movilsecure_v.modelo.repositorio

import com.example.movilsecure_v.modelo.entidades.ZonaFrecuente
import kotlinx.coroutines.flow.Flow

// El repositorio necesita el DAO para acceder a la base de datos.
// Se lo pasamos en el constructor (esto facilita las pruebas y la inyección de dependencias).
class RepositorioZonas(private val zonaFrecuenteDao: ZonaFrecuenteDao) {

    // El Flow se expone directamente. La UI observará este Flow para obtener actualizaciones.
    val todasLasZonas: Flow<List<ZonaFrecuente>> = zonaFrecuenteDao.getAllZonas()

    fun getZona(id: String): Flow<ZonaFrecuente?> {
        return zonaFrecuenteDao.getZonaById(id)
    }

    // El repositorio usa suspend para llamar a las funciones suspend del DAO.
    suspend fun insertar(zona: ZonaFrecuente) {
        zonaFrecuenteDao.insert(zona)
    }

    suspend fun actualizar(zona: ZonaFrecuente) {
        zonaFrecuenteDao.update(zona)
    }

    suspend fun eliminar(zona: ZonaFrecuente) {
        zonaFrecuenteDao.delete(zona)
    }
}