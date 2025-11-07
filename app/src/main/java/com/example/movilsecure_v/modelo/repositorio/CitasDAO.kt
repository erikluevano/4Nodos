package com.example.movilsecure_v.modelo.repositorio

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.movilsecure_v.modelo.entidades.Cita

@Dao
interface CitasDAO {

    /**
     * Inserta una nueva cita en la base de datos. Si la cita ya existe, la reemplaza.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun InsertarDatosCita(cita: Cita)


    /**
     * Actualiza una cita existente en la base de datos.
     */
    @Update
    suspend fun actualizarDatosCita(cita: Cita)

    /**
     * Obtiene todas las citas de la base de datos, ordenadas por fecha ascendente.
     * @return Una lista de todas las citas.
     */
    @Query("SELECT * FROM Cita ORDER BY fecha ASC")
    suspend fun ConsultarCitasRegistradas(): List<Cita>

    @Delete
    suspend fun eliminarDatosCita(cita: Cita)
}
