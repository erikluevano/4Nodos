package com.example.movilsecure_v.modelo.entidades

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
    fun validarDatos(): Boolean {
        // El historial médico es opcional, los demás campos no.
        return nombre.isNotBlank() &&
                fechaNacimiento.isNotBlank() &&
                sexo.isNotBlank() &&
                tipoDeSangre.isNotBlank() &&
                medicamentosActuales.isNotBlank() &&
                alergias.isNotBlank()
    }
}