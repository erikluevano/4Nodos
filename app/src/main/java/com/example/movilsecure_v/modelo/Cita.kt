package com.example.movilsecure_v.modelo

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Modelo de datos que representa una cita médica.
 * Ahora es una entidad de Room para la persistencia de datos.
 */
@Entity(tableName = "Cita")
data class Cita(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fecha: Date,
    val hora: String,
    val lugar: String,
    val motivo: String // Será una cadena vacía si es opcional
)
