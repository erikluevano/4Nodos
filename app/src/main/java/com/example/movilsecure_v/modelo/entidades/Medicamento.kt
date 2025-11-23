package com.example.movilsecure_v.modelo.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa la tabla de medicamentos en la base de datos.
 */
@Entity(tableName = "medicamentos")
data class Medicamento(
    @PrimaryKey(autoGenerate = true)
    val ID: Int = 0,
    val nombre: String,
    val tipoMedicamento: String,
    val horaInicio: String,
    val frecuencia: String,
    // AÑADIDO: Campo para guardar el estado de las notificaciones
    val notificacionesActivas: Boolean = true 
)
