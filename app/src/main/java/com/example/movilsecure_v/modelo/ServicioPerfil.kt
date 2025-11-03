
package com.example.movilsecure_v.modelo

import com.example.movilsecure_v.modelo.repositorio.RepositorioPerfil

// NOTA: Con la arquitectura actual donde el ViewModel habla directamente con el Repositorio,
// esta clase de Servicio se ha vuelto  algo redundante. Por ahora la corregimos para que el
// proyecto compile, pero en el futuro podría ser eliminada para simplificar el código.
class ServicioPerfil(private val repositorioPerfil: RepositorioPerfil) {

    suspend fun registrarPerfil(perfil: PerfilAdultoMayor) {
        repositorioPerfil.registrarPerfil(perfil)
    }
}
