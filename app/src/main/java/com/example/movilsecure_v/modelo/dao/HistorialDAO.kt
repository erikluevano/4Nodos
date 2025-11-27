// ruta: app/src/main/java/com/example/movilsecure_v/modelo/dao/HistorialDAO.kt
package com.example.movilsecure_v.modelo.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.movilsecure_v.modelo.entidades.RegistroHistorial

@Dao
interface HistorialDAO {

    /**
     * Inserta un nuevo registro de navegación en la base de datos.
     * Si ya existe, lo reemplaza (aunque con ID autogenerado, no debería pasar).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarRegistro(registro: RegistroHistorial)

    /**
     * Consulta todos los registros del historial, ordenados por fecha descendente
     * para mostrar los más recientes primero, como lo solicita el caso de uso.
     */
    @Query("SELECT * FROM historial_navegacion ORDER BY fecha DESC")
    suspend fun getRegistros(): List<RegistroHistorial>

    /**
     * (Opcional) Borra todos los registros del historial. Podría ser útil en el futuro.
     */
    @Query("DELETE FROM historial_navegacion")
    suspend fun borrarTodoElHistorial()
}