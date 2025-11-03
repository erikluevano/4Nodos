
package com.example.movilsecure_v.modelo.repositorio

import com.example.movilsecure_v.modelo.PerfilAdultoMayor
import kotlinx.coroutines.flow.Flow

class RepositorioPerfil(private val perfilDao: PerfilDao) {

    val todosLosPerfiles: Flow<List<PerfilAdultoMayor>> = perfilDao.getAllPerfiles()

    suspend fun registrarPerfil(perfil: PerfilAdultoMayor) {
        perfilDao.insertPerfil(perfil)
    }

    suspend fun eliminarPerfil(perfilId: String) {
        perfilDao.deletePerfilById(perfilId)
    }
}
