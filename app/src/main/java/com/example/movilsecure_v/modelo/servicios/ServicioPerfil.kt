package com.example.movilsecure_v.modelo.servicios

import com.example.movilsecure_v.modelo.entidades.PerfilAdultoMayor
import com.example.movilsecure_v.modelo.repositorio.RepositorioPerfil

/**
 * Contiene la lógica de negocio relacionada con los perfiles.
 * El ViewModel se comunica con este servicio para realizar operaciones de escritura o complejas.
 */
class ServicioPerfil(private val repositorio: RepositorioPerfil) {

    /**
     * Procesa y registra un perfil. Por ahora, solo delega al repositorio,
     * pero en el futuro podría incluir validaciones complejas, notificaciones, etc.
     */
    suspend fun registrarPerfil(perfil: PerfilAdultoMayor) {
        // Lógica de negocio podría ir aquí.
        repositorio.registrarPerfil(perfil)
    }
}