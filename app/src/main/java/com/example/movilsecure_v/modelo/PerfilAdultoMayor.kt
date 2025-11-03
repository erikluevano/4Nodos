
package com.example.movilsecure_v.modelo

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "perfiles_adulto_mayor")
data class PerfilAdultoMayor(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val nombre: String,
    val fechaNacimiento: String,
    val sexo: String,
    val historialMedico: String,
    val tipoDeSangre: String,
    val medicamentosActuales: String,
    val alergias: String
) {
    // La función validarDatos se usaba en el ViewModel,
    // por lo que ya no es necesaria dentro de la entidad.
    // Se puede mantener si se le da otro uso, pero para la persistencia no es requerida.
}
