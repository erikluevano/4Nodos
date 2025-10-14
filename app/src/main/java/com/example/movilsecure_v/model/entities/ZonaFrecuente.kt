package com.example.movilsecure_v.model.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "zona_frecuente")
data class ZonaFrecuente(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(), // UUID se guarda como String

    @ColumnInfo(name = "nombre_zona")
    val nombreZona: String,

    @ColumnInfo(name = "direccion_zona")
    val direccionZona: String?, // Puede ser nulo según el esquema (text)

    @ColumnInfo(name = "coordenada_latitud")
    val latitud: Double,

    @ColumnInfo(name = "coordenada_longitud")
    val longitud: Double,

    @ColumnInfo(name = "nota_zona")
    val notaZona: String? // Puede ser nulo según el esquema (text)
)