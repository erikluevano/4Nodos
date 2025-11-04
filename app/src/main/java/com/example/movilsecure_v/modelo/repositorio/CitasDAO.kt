package com.example.movilsecure_v.modelo.repositorio

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.movilsecure_v.modelo.Cita

@Dao
interface CitasDAO {

    /**
     * Inserta una nueva cita en la base de datos. Si la cita ya existe, la reemplaza.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun InsertarDatosCita(cita: Cita)

    /**
     * Obtiene todas las citas de la base de datos, ordenadas por fecha ascendente.
     * @return Una lista de todas las citas.
     */
    @Query("SELECT * FROM Cita ORDER BY fecha ASC")
    suspend fun ConsultarCitasRegistradas(): List<Cita>
}
