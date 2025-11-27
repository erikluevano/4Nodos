// ruta: app/src/main/java/com/example/movilsecure_v/modelo/entidades/RegistroHistorial.kt
package com.example.movilsecure_v.modelo.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa la tabla del historial de navegación en la base de datos.
 * Cada registro corresponde a una navegación iniciada por el usuario.
 */
@Entity(tableName = "historial_navegacion")
data class RegistroHistorial(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val origen: String,
    val destino: String,
    // Guardamos la fecha como String en formato ISO para que se pueda ordenar correctamente.
    // Ejemplo: "2024-11-20T14:30:00"
    val fecha: String
)