package com.example.movilsecure_v.model.repository

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.movilsecure_v.model.entities.ZonaFrecuente
import kotlinx.coroutines.flow.Flow

@Dao
interface ZonaFrecuenteDao {

    // Inserta una nueva zona. Si ya existe, la reemplaza.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(zona: ZonaFrecuente)

    // Actualiza una zona existente
    @Update
    suspend fun update(zona: ZonaFrecuente)

    // Borra una zona
    @Delete
    suspend fun delete(zona: ZonaFrecuente)

    // Obtiene una zona específica por su ID
    @Query("SELECT * FROM zona_frecuente WHERE id = :id")
    fun getZonaById(id: String): Flow<ZonaFrecuente?>

    // Obtiene todas las zonas frecuentes, ordenadas por nombre
    // Flow permite que la UI se actualice automáticamente cuando los datos cambian
    @Query("SELECT * FROM zona_frecuente ORDER BY nombre_zona ASC")
    fun getAllZonas(): Flow<List<ZonaFrecuente>>
}